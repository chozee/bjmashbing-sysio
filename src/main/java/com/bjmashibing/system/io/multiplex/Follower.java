package com.bjmashibing.system.io.multiplex;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Follower implements Runnable{
    Selector selector = null;

    public Follower() {
    }

    public Selector getSelector() {
        return selector;
    }

    public Follower setSelector(Selector selector) {
        this.selector = selector;
        return this;
    }

    public Follower(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int num = selector.select(500l);

                if (num > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();

                    while (iter.hasNext()) {
                        iter.remove();

                        SelectionKey key = iter.next();
                        if (key.isAcceptable()) {
                            doAccept(key);
                        } else if (key.isReadable()) {
                            read(key);
                        } else if (key.isWritable()) {
                            write(key);
                        }
                    }
                }else {
                    System.out.println("non event. ");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();

        // 如果只是处理accept,那么如下代码就放到其他线程中处理
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public void read(SelectionKey key) throws Exception {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();
        SocketChannel client = (SocketChannel) key.channel();

        while (true) {
            int num = client.read(buffer);

            if (num > 0) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    client.write(buffer);
                }

                buffer.clear();
            } else if (num == 0) {
                break;
            } else {
            }
        }
    }

    public void write(SelectionKey key) {

    }
}
