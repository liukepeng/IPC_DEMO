package com.kun.messengerserver;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author liukepeng
 * @date 2019/3/4
 */
public class ServerWithHandler extends Service {
    private static final String SERVER = "Server";
    Messenger messenger;

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        messenger = new Messenger(new MessengerHandler());
    }

    private static class MessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.i(SERVER, "接收到客户端的消息：'" + msg.getData().getString("message") + "'");
                    Messenger client = msg.replyTo;
                    if (client != null) {
                        Message reply = Message.obtain();
                        Bundle message = new Bundle();
                        message.putString("message", "我是服务端，接收到你的消息拉");
                        Log.i(SERVER, "服务端也给客户端发了消息");
                        reply.setData(message);
                        try {
                            client.send(reply);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    Log.i(SERVER,"接收到客户端的消息 '" + msg.getData().getString("message")+"'");
                    Log.i(SERVER,"客户端关闭了连接, bye~");
                    break;
                default:
                    break;
            }
        }
    }
}
