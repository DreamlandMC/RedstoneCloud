package de.redstonecloud.api.netty.server.handler;

import de.redstonecloud.api.netty.server.NettyServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {
    public NettyServer nettyServer;

    public ExceptionHandler(NettyServer s) {
        this.nettyServer = s;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Netty exception: " + cause.getLocalizedMessage());

        // Determine the name of the offending channel
        AtomicReference<String> name = new AtomicReference<>("");
        nettyServer.getChannelCache().forEach((ch, idfk) -> {
            if (nettyServer.getChannelCache().get(ch).equals(ctx.channel())) {
                name.set(ch);
            }
        });

        System.out.println("Exception sent from " + name.get());

        // Check the type of exception and take action accordingly
        if (cause instanceof DecoderException) {
            // Log and potentially close connection on invalid decoding
            System.out.println("DecoderException occurred, closing channel for " + name.get());
            ctx.close();
        } else if (cause instanceof IOException) {
            // Handle IOExceptions separately, often caused by network errors
            System.out.println("IOException occurred, closing channel for " + name.get());
            ctx.close();
        } else {
            // Log other types of exceptions but keep the channel alive if possible
            System.out.println("General error occurred: " + cause.getMessage());
        }
    }
}