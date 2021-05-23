package com.multiplex.system.io;

import java.io.*;
import java.net.Socket;

/**
 * @author: 马士兵教育
 * @create: 2020-05-17 16:18
 */
public class SocketClient {

    private static String SERV_IP = "10.211.55.103";
    private static int SERV_port = 9090;

    public static void main(String[] args) {

        try {
            Socket client = new Socket(SERV_IP,SERV_port);

            client.setSendBufferSize(20);
            client.setTcpNoDelay(true);
            OutputStream out = client.getOutputStream();

            InputStream in = System.in;
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            while(true){
                String line = reader.readLine();
                if(line != null ){
                    byte[] bb = line.getBytes();
                    for (byte b : bb) {
                        out.write(b);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
