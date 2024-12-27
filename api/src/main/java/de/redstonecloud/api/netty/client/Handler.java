package de.redstonecloud.api.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Handler extends ChannelInboundHandlerAdapter {
    private NettyClient client;

    public Handler(NettyClient c) {
        this.client = c;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Channel inactive, attempting reconnection...");
        client.connect("127.0.0.1", client.getPort());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Exception caught in the client: " + cause.getMessage());
        ctx.close();  // Close the channel on exception
    }
}