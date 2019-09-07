package com.ligongzzz.acoj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //Connect Thread
    private TextToSpeech textToSpeech;
    ConnectThread connectThread;
    MsgReadThread msgReadThread = null;
    String ip;
    int port;
    Boolean connectedToServer = false;
    Boolean receivedCall = false;
    Boolean timeSet = false;
    Boolean checking = false;
    Boolean shownConnected = false;
    String tmpUsername = null, tmpPassword = null;

    //To Get Username and Password.
    public Boolean getUserData(){
        SharedPreferences sharedPreferences = getSharedPreferences("userData",MODE_PRIVATE);
        tmpUsername = sharedPreferences.getString("username",null);
        tmpPassword = sharedPreferences.getString("password",null);

        return tmpUsername != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData",MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("shownTip",false)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("欢迎使用");
            builder.setMessage("本程序仅供学习和交流使用，请不要用作其它用途！");
            builder.setPositiveButton("好",null);
            builder.show();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("shownTip",true);
            editor.commit();
        }

        //Set TTS
        TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i==TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA||result == TextToSpeech.LANG_NOT_SUPPORTED){
                        toastMsg("TTS异常：不支持的语言。");
                    }
                }
            }
        };

        textToSpeech = new TextToSpeech(this,onInitListener);

        if(!connectedToServer||!connectThread.socket.isConnected()) {
            ip = "120.27.241.203";
            port = 6666;
            connectToServer();
            if(!timeSet) {
                timer.schedule(task, 0, 10000);
                timeSet = true;
            }
        }
        else{
            try {
                checkConnect(false);
            } catch (InterruptedException e) {
                Toast.makeText(this, "检查连接时出现问题。", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        //GetUserData
        if(getUserData()){
            ((Button)findViewById(R.id.button_connect)).setText(tmpUsername);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("未设置账户");
            builder.setMessage("为了跟踪提交您的代码，请您尽快点击右上角的“账户”按钮设置您的OJ账户和密码，以便提交。");
            builder.setPositiveButton("好",null);
            builder.show();
        }
    }

    public void connectToServer(){
        if(connectThread!=null) {
            connectThread.clear();
        }
        try {
            connectThread = new ConnectThread(ip, port, this);
            connectThread.start();
        }
        catch (Exception e){
            e.printStackTrace();
            toastMsg(e.toString());
        }

        try {
            Thread.sleep(500);
        }
        catch (Exception e){
            e.printStackTrace();
            toastMsg(e.toString());
        }
        try {
            if (connectThread != null && connectThread.socket.isConnected()) {
                toastMsg("已经成功连接到服务器。");
                connectedToServer = true;
                //Login
                SendThread sendThread = new SendThread(connectThread.outputStream, "~@Tourist:Login");
                sendThread.start();

                //Start Listen Thread
                msgReadThread = new MsgReadThread();
                msgReadThread.start();

                if(!shownConnected){
                    ((TextView)findViewById(R.id.text_name)).setText("已经连接到服务器");
                    shownConnected = true;
                }
            } else {
                toastMsg("连接出现问题");
            }
        }
        catch (Exception e){
            e.printStackTrace();
            toastMsg("连接出现问题");
        }
    }

    public void onConnectClick(View view){
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivityForResult(intent,1000);
    }

    public void onSendClick(View view){
        if (!connectedToServer){
            Toast.makeText(this,"您尚未连接到服务器，不能提交。",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!getUserData()){
            toastMsg("您尚未登录账户，不能提交。");
            return;
        }
        final String todo = ((EditText)findViewById(R.id.Promblem_Code)).getText().toString();
        if(todo.length()!=4){
            Toast.makeText(this,"请输入正确的题目编号！",Toast.LENGTH_SHORT).show();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("确认提交");
            builder.setMessage("将使用当前的账号为您提交第"+todo+"题，提交可能会产生一些错误，您确认要提交吗？");

            builder.setPositiveButton("确认提交", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    while(true) {
                        if(checking){
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            String toSend = "TODO:"+todo+tmpUsername+"~@$"+tmpPassword;
                            SendThread sendThread = new SendThread(connectThread.outputStream, toSend);
                            sendThread.start();
                            Toast.makeText(getApplicationContext(),"已经为您发送提交请求",Toast.LENGTH_SHORT).show();
                            ((EditText)findViewById(R.id.Promblem_Code)).clearFocus();
                            break;
                        }
                    }
                }
            });

            builder.show();
        }
    }

    public void checkConnect(Boolean showToast) throws InterruptedException {
        if(!connectedToServer||connectThread.socket==null||!connectThread.socket.isConnected()){
            connectedToServer = false;
            toastMsg("已经断线，正在重新连接......");
            connectToServer();
        }
        else {
            CheckConnectThread checkConnectThread = new CheckConnectThread(showToast);
            checkConnectThread.start();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            Bundle bundle = data.getExtras();
            SharedPreferences sharedPreferences = getSharedPreferences("userData",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("username",bundle.getString("username"));
            editor.putString("password",bundle.getString("password"));
            editor.commit();

            ((Button)findViewById(R.id.button_connect)).setText(bundle.getString("username"));

            toastMsg("账户信息保存成功！");
        }
    }

    //Handler to update messages.
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            try{
                Bundle receive = msg.getData();
                String toSend = receive.getString("msg");
                if(toSend.startsWith("welcome")){
                    String id = "ACSJTU_001";
                    String name = "name";
                    NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
                    Notification notification = null;
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(),
                            0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_MAX);
                    notificationManager.createNotificationChannel(mChannel);
                    notification = new Notification.Builder(MainActivity.this)
                            .setChannelId(id)
                            .setContentTitle("连接成功")
                            .setContentText("您已经成功连接到服务器")
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_launcher_foreground).build();
                    textToSpeech.speak("已经连接到服务器",TextToSpeech.QUEUE_FLUSH,null);
                    notificationManager.notify(1, notification);
                }
                else if(toSend.startsWith("FEEDBACK")){
                    receivedCall = true;
                }
                else if(toSend.startsWith("submit_success")){
                    Toast.makeText(MainActivity.this,"提交已经成功",Toast.LENGTH_SHORT).show();
                }
                else if(toSend.startsWith("submit_fail")){
                    Toast.makeText(MainActivity.this,"提交出现异常",Toast.LENGTH_SHORT).show();
                }
                else {
                    String[] msgStr = toSend.split("!@#");
                    ((TextView) findViewById(R.id.text_name)).setText(msgStr[0]);
                    ((TextView) findViewById(R.id.text_problem)).setText(msgStr[1]);
                    ((TextView) findViewById(R.id.text_result)).setText(msgStr[2]);
                    ((EditText)findViewById(R.id.Promblem_Code)).setText(msgStr[1]);

                    String id = "ACSJTU_001";
                    String name = "name";
                    NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(NOTIFICATION_SERVICE);
                    Notification notification = null;
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplication(),
                            0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
                    NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_MAX);
                    notificationManager.createNotificationChannel(mChannel);
                    notification = new Notification.Builder(MainActivity.this)
                            .setChannelId(id)
                            .setContentTitle("检查到新的提交")
                            .setContentText(msgStr[0]+"提交了"+msgStr[1]+"题目，结果是"+msgStr[2])
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.ic_launcher_foreground).build();
                    textToSpeech.speak(msgStr[0]+"提交了第"+msgStr[1]+"题，提交的结果为"+msgStr[2],
                            TextToSpeech.QUEUE_FLUSH,null);
                    if(msgStr[2].startsWith("正确")){
                        ((LinearLayout)findViewById(R.id.linear)).setBackgroundColor(Color.rgb(0,180,80));
                    }
                    else if(msgStr[2].startsWith("超过")){
                        ((LinearLayout)findViewById(R.id.linear)).setBackgroundColor(Color.rgb(0xff,0x66,0x00));
                    }
                    else if(msgStr[2].startsWith("编译错误")){
                        ((LinearLayout)findViewById(R.id.linear)).setBackgroundColor(Color.rgb(0x00,0x66,0xff));
                    }
                    else if(msgStr[2].startsWith("运行时错误")){
                        ((LinearLayout)findViewById(R.id.linear)).setBackgroundColor(Color.rgb(0xcc,0x00,0x99));
                    }
                    else{
                        ((LinearLayout)findViewById(R.id.linear)).setBackgroundColor(Color.rgb(200,0,0));
                    }
                    notificationManager.notify(1, notification);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void toastMsg(String toToast){
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("toast",toToast);
        msg.setData(bundle);

        toastHandler.sendMessage(msg);
    }

    Handler toastHandler = new Handler(){
        public void handleMessage(Message msg){
            try{
                Bundle bundle = msg.getData();
                String toToast = bundle.getString("toast");
                Toast.makeText(MainActivity.this,toToast,Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Handler timeHandler = new Handler(){
        public void handleMessage(Message msg){
            try{
                Bundle bundle = msg.getData();
                String curTime = bundle.getString("time");
                ((TextView)findViewById(R.id.connect_info)).setText(curTime);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            try {
                checkConnect(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    //byte的合并
    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length+bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }

    public static int returnActualLength(byte[] data) {
        int i = 0;
        for (; i < data.length; i++) {
            if (data[i] == '\0')
                break;
        }
        return i;
    }

    public void parseMsg(byte[] bt){
        try {
            int realLength = returnActualLength(bt);

            if(realLength <= 0){
                return;
            }

            String raw = new String(bt, "utf-8");

            Bundle bundle = new Bundle();
            bundle.putString("msg",raw);

            Message msg = new Message();
            msg.setData(bundle);

            receivedCall = true;

            handler.sendMessage(msg);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    class MsgReadThread extends Thread{
        public void run(){
            try{
                //Raw Data
                byte[] rawData = new byte[1024];
                while(true) {
                    final byte[] buffer = new byte[1024];
                    InputStream inputStream = connectThread.socket.getInputStream();
                    final int len = inputStream.read(buffer);

                    if(len == -1){
                        connectedToServer = false;
                        checkConnect(false);
                    }

                    parseMsg(buffer);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class CheckConnectThread extends Thread{
        private Boolean showToast;

        public CheckConnectThread(Boolean showtoast){
            showToast = showtoast;
        }

        public void run(){
            try{
                if(checking){
                    return;
                }
                receivedCall = false;
                checking = true;
                for(int i=0;i<5;++i){
                    SendThread sendThread = new SendThread(connectThread.outputStream, "HELLO-CALL");
                    sendThread.start();
                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if(receivedCall){
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("与服务器最后通信时间: HH:mm:ss");
                        String sim = dateFormat.format(date);

                        Message message = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("time", sim);
                        message.setData(bundle);

                        timeHandler.sendMessage(message);
                        if (showToast) {
                            toastMsg("无需重复连接！");
                        }
                        break;
                    }
                    else if(i==4){
                        toastMsg("与服务器通信失败。");
                        connectToServer();
                    }
                }
                checking = false;
            }
            catch (Exception e){
                e.printStackTrace();
                toastMsg(e.toString());
            }
        }
    }
}
