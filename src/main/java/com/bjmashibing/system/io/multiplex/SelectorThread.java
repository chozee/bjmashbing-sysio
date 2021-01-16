package com.bjmashibing.system.io.multiplex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import static java.nio.channels.SelectionKey.OP_ACCEPT;
import static java.nio.channels.SelectionKey.OP_READ;

public class SelectorThread implements Runnable{
    Selector selector = null;
    LinkedBlockingDeque<Channel> channels = new LinkedBlockingDeque<>();
    SelectorGroup group = null;

    public SelectorThread(SelectorGroup group) {
        try {
            selector = Selector.open();
            this.group = group;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Selector getSelector() {
        return selector;
    }

    public SelectorThread setSelector(Selector selector) {
        this.selector = selector;
        return this;
    }

    public SelectorThread(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("select before thd:"  + Thread.currentThread().getName() + ", keys count:" + selector.keys().size());
                int num = selector.select(500l);
                // 阻塞 如果想做其它事情要被打断才可以, 所以在分配任务的时候记得调用wakeup
                System.out.println("select after  thd:"  + Thread.currentThread().getName() + ", keys count:" + selector.keys().size());

                if (num > 0) {
                    doEvent();
                }

                // 处理新分配的channel
                doAssignChannel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doAssignChannel() throws InterruptedException, ClosedChannelException {
        if (!channels.isEmpty()) {
            for (Channel channel : channels) {
                Channel c = channels.take();

                if (c instanceof ServerSocketChannel) {
                    // 服务端刚进来只关注accept事件
                    ServerSocketChannel serv = (ServerSocketChannel)c;
                    serv.register(selector, OP_ACCEPT);
                } else if (c instanceof SocketChannel) {
                    // 客户端进来先关注读事件, 写事件必须要先读再有
                    // 读要设置buffer
                    SocketChannel cli = (SocketChannel)c;
                    ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                    cli.register(selector, OP_READ, buffer);
                }
            }
        }
    }

    private void doEvent() throws Exception {
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iter = keys.iterator();

        while (iter.hasNext()) {
            iter.remove();

            SelectionKey key = iter.next();
            if (key.isAcceptable()) {
                transferNewTask(key);
            } else if (key.isReadable()) {
                read(key);
            } else if (key.isWritable()) {
                write(key);
            }
        }
    }

    public void transferNewTask(SelectionKey key) throws Exception {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();

        group.assignTask(client);
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
