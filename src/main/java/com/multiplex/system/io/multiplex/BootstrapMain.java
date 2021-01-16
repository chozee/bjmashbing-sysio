package com.multiplex.system.io.multiplex;

import com.multiplex.netty.NettyBootStrap;

public class BootstrapMain {

    public static void main(String[] args) {
        try {
            NettyBootStrap.client();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        SelectorGroup g = new SelectorGroup(1);
//        try {
//            g.bingServer(9999);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
