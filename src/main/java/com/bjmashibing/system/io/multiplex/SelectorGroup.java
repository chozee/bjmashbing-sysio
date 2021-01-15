package com.bjmashibing.system.io.multiplex;

import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    Follower[] fs = null;
    ServerSocketChannel server = null;

    public SelectorGroup(int thdNum) {
        fs = new Follower[thdNum];

        for (int i = 0; i < thdNum; i++) {
            fs[i] = new Follower();
        }
    }

    /**
     * 把需要关注的FD关联到不同selector线程上
     */
    public void selector(Channel channel) throws Exception {
        Follower f = next();

        ServerSocketChannel server = (ServerSocketChannel) channel;
        server.register(f.selector, SelectionKey.OP_ACCEPT);
    }

    public Follower next() {
        return fs[xid.incrementAndGet() % fs.length];
    }
}
