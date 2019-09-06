package com.ligongzzz.acoj;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.Socket;

public class ConnectThread extends Thread{
    Socket socket = null;
    OutputStream outputStream = null;
    String errStr = null;
    Context mainContext = null;
    String ip;
    int port;

    public ConnectThread(String ip_str,int port_val,Context context){
        ip = ip_str;
        port = port_val;
        mainContext = context;
    }

    public void run(){
        System.out.println(Thread.currentThread().getName()+":start the thread.");
        try {
            socket = new Socket(ip,port);
            outputStream = socket.getOutputStream();
        }
        catch (Exception e){
            e.printStackTrace();
            errStr = e.getMessage();
        }
    }

    public void clear(){
        try {
            if (socket != null) {
                socket.close();
            }
            socket = null;
            errStr = null;
            outputStream = null;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
