package com.multiplex.system.io.multiplex;

import io.netty.channel.*;

/**
 * @author chozee on 2021-01-16 20:46.
 */
    @ChannelHandler.Sharable
public abstract class InitHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
        Channel channel = channelHandlerContext.channel();
        ChannelPipeline pipeline = channel.pipeline();
        buildHandler(channel);

        System.out.println("init handler register finshed ... ");
        pipeline.remove(this);// 过桥 用过删除 没啥用了

    }

    /**
     * 创建你自己需要设置的handler
     * @return
     */
    public abstract void buildHandler(Channel ch);

    }
