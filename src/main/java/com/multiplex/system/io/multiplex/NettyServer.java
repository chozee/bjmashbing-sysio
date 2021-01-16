package com.multiplex.system.io.multiplex;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author chozee on 2021-01-16 18:01.
 */
public class NettyServer {
    public static void main(String[] args) {
        try {
            nettyServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void nettyServer() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap bs = new ServerBootstrap();
        ChannelFuture connFu = bs.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new NettyClient.MyInHandler());
                    }
                })
                .bind("localhost", 9090);

        connFu.sync().channel().closeFuture().sync();
    }

    public static class MyAcceptHandler extends ChannelInboundHandlerAdapter {
        private final EventLoopGroup group;
        private final ChannelHandler handler;

        public MyAcceptHandler(EventLoopGroup group, ChannelHandler handler) {
            this.group = group;
            this.handler = handler;
        }

        /**
         * server 的read事件需要做的事情:
         *  1. 设置client读的handle
         *  2. 绑定client到selector
         *
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            SocketChannel clientChannel = (SocketChannel) msg;
            ChannelPipeline pipeline = clientChannel.pipeline();
            pipeline.addLast(handler);

            group.register(clientChannel);
        }
    }
}
