package de.redstonecloud.cloud.netty;

import de.redstonecloud.cloud.RedstoneCloud;
import de.redstonecloud.cloud.logger.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.atomic.AtomicReference;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.getInstance().debug("Netty exception: " + cause.getLocalizedMessage());

        AtomicReference<String> name = new AtomicReference<>("");

        RedstoneCloud.getInstance().getNettyServer().getChannelCache().forEach((ch, idfk) -> {
            if(RedstoneCloud.getInstance().getNettyServer().getChannelCache().get(ch).equals(ctx.channel())){
                name.set(ch);
            }
        });

        System.out.println("Exceptoion sent from " + name.get());
    }
}