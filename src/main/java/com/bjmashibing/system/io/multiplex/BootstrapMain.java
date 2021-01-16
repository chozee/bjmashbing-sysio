package com.bjmashibing.system.io.multiplex;

import java.io.IOException;

public class BootstrapMain {

    public static void main(String[] args) {
        SelectorGroup g = new SelectorGroup(1);
        try {
            g.bingServer(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
