package de.redstonecloud.api.netty.packet;

import de.pierreschwang.nettypacket.Packet;
import de.pierreschwang.nettypacket.event.EventRegistry;
import de.pierreschwang.nettypacket.registry.SimplePacketRegistry;
import de.pierreschwang.nettypacket.response.RespondingPacket;
import de.redstonecloud.api.netty.NettyHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

public class CustomPacketHandler extends SimpleChannelInboundHandler<Packet> {
    private final EventRegistry eventRegistry;
    private Map<Channel, Integer> invalidPacketCount = new HashMap<>();

    public CustomPacketHandler(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
    }

    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        int pId = NettyHelper.Holder.getRegistry().getPacketId(packet.getClass());

        if(!NettyHelper.Holder.getRegistry().containsPacketId(pId)) {
            System.out.println("Got invalid packet: " + pId);

            // Increment invalid packet count for this channel
            invalidPacketCount.put(ctx.channel(), invalidPacketCount.getOrDefault(ctx.channel(), 0) + 1);

            // Check if the client has sent too many invalid packets
            if (invalidPacketCount.get(ctx.channel()) > 5) {
                System.out.println("Closing connection due to too many invalid packets");
                ctx.close();
            }

            return;
        }

        // Reset the invalid packet count on valid packets
        invalidPacketCount.put(ctx.channel(), 0);
        RespondingPacket.callReceive(packet);
        this.eventRegistry.invoke(packet, ctx);
    }
}
