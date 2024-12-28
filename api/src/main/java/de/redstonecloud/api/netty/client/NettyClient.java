package de.redstonecloud.api.netty.client;

import de.pierreschwang.nettypacket.Packet;
import de.pierreschwang.nettypacket.event.EventRegistry;
import de.pierreschwang.nettypacket.handler.PacketChannelInboundHandler;
import de.pierreschwang.nettypacket.handler.PacketDecoder;
import de.pierreschwang.nettypacket.handler.PacketEncoder;
import de.pierreschwang.nettypacket.registry.IPacketRegistry;
import de.pierreschwang.nettypacket.response.RespondingPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import de.redstonecloud.api.netty.packet.communication.ClientAuthPacket;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Getter
@Setter
@Accessors(chain = true)
public class NettyClient extends ChannelInitializer<Channel> {
    protected final Bootstrap bootstrap;
    protected final String clientId;
    protected final IPacketRegistry packetRegistry;
    protected final EventRegistry eventRegistry;

    @Setter(AccessLevel.NONE)
    protected Channel channel;

    protected EventLoopGroup worker = new NioEventLoopGroup();

    protected int port;

    private int retryCount = 0;

    public NettyClient(String clientId, IPacketRegistry packetRegistry, EventRegistry eventRegistry) {
        this.bootstrap = new Bootstrap()
                .option(ChannelOption.AUTO_READ, true)
                .channel(NioSocketChannel.class)
                .group(this.worker)
                .handler(this);

        this.clientId = clientId;
        this.packetRegistry = packetRegistry;
        this.eventRegistry = eventRegistry;
    }

    public void bind() {
        connect("127.0.0.1", this.port);
    }

    // New connect method with reconnection logic
    public void connect(String host, int port) {
        System.out.println("CONNECTING NETTY...");
        ChannelFuture future = this.bootstrap.connect(host, port);
        future.addListener((ChannelFutureListener) f -> {
            if (f.isSuccess()) {
                System.out.println("Connected to the server.");
                this.channel = f.channel();
                channel.config().setWriteBufferHighWaterMark(64 * 1024);
                channel.config().setWriteBufferLowWaterMark(32 * 1024);
                retryCount = 0; // Reset retry count on successful connection
                this.sendPacket(new ClientAuthPacket(this.clientId));
            } else {
                System.out.println("Failed to connect. Retrying...");
                retry();
            }
        });
    }

    // Reconnection logic
    private void retry() {
        System.out.println("Reconnecting...");
        connect("127.0.0.1", this.port);
    }

    @Override
    protected void initChannel(Channel channel) {
        channel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4)) // Handles framing
                .addLast(new LengthFieldPrepender(4))
                .addLast(new PacketDecoder(this.packetRegistry),
                        new PacketEncoder(this.packetRegistry),
                        new PacketChannelInboundHandler(this.eventRegistry),
                        new Handler(this),
                        new IdleStateHandler(60,30,0));

        this.channel = channel;

        this.sendPacket(new ClientAuthPacket(this.clientId));
    }

    public void sendPacket(Packet packet) {
        this.channel.writeAndFlush(packet).awaitUninterruptibly(5, TimeUnit.MILLISECONDS);
    }

    public <T extends Packet> void sendPacket(Packet packet, Consumer<T> callback, Class<T> clazz) {
        RespondingPacket<T> respondingPacket = new RespondingPacket<>(packet, clazz, callback);
        respondingPacket.send(this.channel);
    }
}
