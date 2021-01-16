package com.bjmashibing.system.io.multiplex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 每个组管理一个服务器关心的FD
 *
 * 存储一个组的线程和selector
 *  绑定FD
 *  轮训selector线程
 *
 */
public class SelectorGroup {
    private AtomicInteger xid = new AtomicInteger(0);
    SelectorThread[] fs = null;
    ServerSocketChannel server = null;


    public SelectorGroup(int thdNum) {
        fs = new SelectorThread[thdNum];

        for (int i = 0; i < thdNum; i++) {
            fs[i] = new SelectorThread(this);

            new Thread(fs[i]).start();
        }
    }

    public void bingServer(int port) throws IOException {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind((new InetSocketAddress(port)));

        SelectorThread next = selectThread();

        server.register(next.selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * 把需要关注的FD关联到不同selector线程上
     */
    public void assignTask(Channel channel) throws Exception {
        // 负载均衡 按策略选择线程
        SelectorThread f = selectThread();

        // 线程的任务队列 做业务
        f.channels.add(channel);

        // 由于线程被select阻塞, 这里如果要做事一定要打断
        f.selector.wakeup();
    }

    public SelectorThread selectThread() {
        return fs[xid.incrementAndGet() % fs.length];
    }
}
