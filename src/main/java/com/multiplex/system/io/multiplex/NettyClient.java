package com.multiplex.system.io.multiplex;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * netty lcient
 *
 * @author chozee on 2021-01-16 17:25.
 */
public class NettyClient {
    public static void main(String[] args) {

        try {
//            client();
            nettyClient();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void nettyClient() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        Bootstrap bs = new Bootstrap();
        ChannelFuture connFuture = bs.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(new MyInHandler());
                    }
                })
                .connect("localhost", 9090);

        Channel client = connFuture.sync().channel();
        clientSendMsg(client, "msg");

        client.closeFuture().sync();
    }

    private static void clientSendMsg(Channel channel, String msg) throws InterruptedException {
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        ChannelFuture sendFuture = channel.writeAndFlush(buf);
        sendFuture.sync();
    }


    public static void client() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        group.register(client);


        ChannelPipeline pipeline = client.pipeline();
        pipeline.addLast(new MyInHandler());


        ChannelFuture connFuture = client.connect(new InetSocketAddress("localhost", 9090));
        connFuture.sync();

        ByteBuf byteBuf = Unpooled.copiedBuffer("test\n".getBytes());
        ChannelFuture writeFuture = client.writeAndFlush(byteBuf);
        writeFuture.sync();



//        connFuture.channel().closeFuture().sync();
//        writeFuture.channel().closeFuture().sync();
        System.out.println("client over... ");
    }

    public static class MyInHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

            ByteBuf byteBuf = (ByteBuf) o;
            CharSequence charSequence = byteBuf.getCharSequence(0, byteBuf.readableBytes(), CharsetUtil.UTF_8);
            System.out.println(charSequence);


            channelHandlerContext.writeAndFlush(byteBuf);
        }
    }
}
