package com.bjmashibing.system.io.multiplex;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class BootstrapMain {
    ServerSocketChannel server = null;
    Selector selector = null;

    String ip = "localhost";

    public void bind(int port) {
    }
    public void start() throws Exception {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);

        server.bind(new InetSocketAddress("", 9090));


        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

    }
}
