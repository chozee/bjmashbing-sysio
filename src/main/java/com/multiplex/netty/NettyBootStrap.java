package com.multiplex.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static java.nio.channels.SelectionKey.OP_READ;

/**
 * netty boot strap
 *
 * @author chozee on 2021-01-16 15:03.
 */
public class NettyBootStrap {
    public static void main(String[] args) {
        try {
            serverUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void serverUp() throws IOException {
        ServerSocketChannel serv = ServerSocketChannel.open();
        serv.configureBlocking(false);
        serv.socket().bind(new InetSocketAddress("192.168.50.126", 9090));

        Selector selector = Selector.open();
        serv.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server up.. ");
        while (true) {
            selector.select();

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();

                keys.remove();

                if (!key.isValid()) continue;

                if (key.isAcceptable()) {
                    accept(selector, key);
                }else if (key.isReadable()) {
                    serverRead(key);
                }
            }

        }
    }

    private static void serverRead(SelectionKey key) {
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buf = ByteBuffer.allocate(128);
            channel.read(buf);

            System.out.println("Hi client, Im server. i got message " + new String(buf.array()).trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void accept(Selector select, SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);

        client.register(select, OP_READ);

    }

    /**
     *
     * @throws Exception
     */
    public static void client() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(1);
        NioSocketChannel client = new NioSocketChannel();
        group.register(client);

        ChannelFuture conn = client.connect(new InetSocketAddress("192.168.50.126", 9090));
        ChannelFuture connSync = conn.sync();

        ByteBuf buf = Unpooled.copiedBuffer("hello12".getBytes());
        ChannelFuture channelFuture = client.writeAndFlush(buf);
        channelFuture.sync();

        connSync.channel().closeFuture().sync();

        System.in.read();
        System.out.println("client over... ");
    }
}
