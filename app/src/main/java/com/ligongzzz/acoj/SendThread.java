package com.ligongzzz.acoj;

import java.io.OutputStream;

public class SendThread extends Thread{
    OutputStream outputStream = null;
    String msg = null;

    SendThread(OutputStream opStream,String toSend){
        outputStream = opStream;
        msg = toSend;
    }

    public void run(){
        try{
            outputStream.write(msg.getBytes("utf-8"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
