package com.kun.messengerclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class ClientWithHandlerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String CLIENT = "Client";
    private Button connect_handler;

    private ServiceConnection serviceConnection;
    private Messenger serverMessenger;
    private Messenger messenger;

    private boolean hasBindService = false;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connect_handler =  findViewById(R.id.connect_handler);
        connect_handler.setOnClickListener(this);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serverMessenger = new Messenger(service);
                communicate();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                serverMessenger = null;
            }
        };

        //接收服务端的数据
        messenger = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.i(CLIENT, "客户端收到 '" + msg.getData().getString("message") + "'");
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString("message", "OK, bye bye~");
                message.setData(bundle);
                Log.i(CLIENT,"客服端给服务端发送 '" + message.getData().getString("message") + "'");
                message.what = 2;
                if (serverMessenger != null){
                    try {
                        serverMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //给服务端发送数据
    private void communicate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        Message message = Message.obtain();
        Bundle msg = new Bundle();
        msg.putString("message", "我是客户端" + simpleDateFormat.format(System.currentTimeMillis()));
        message.setData(msg);
        Log.i(CLIENT, "客服端'" + message.getData().getString("message") + "'");
        message.what = 1;
        message.replyTo = messenger;
        if (serverMessenger != null){
            try {
                serverMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connect_handler:
                if (!hasBindService) {
                    Intent intent = new Intent();
                    intent.setClassName("com.kun.messengerserver", "com.kun.messengerserver.ServerWithHandler");
                    bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                    hasBindService = true;
                }else{
                    if (serverMessenger == null){
                        return;
                    }
                    communicate();
                }
                break;

             default:
                 break;
        }
    }

    @Override
    protected void onDestroy() {
        if (serverMessenger != null) {
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }
}
