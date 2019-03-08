package com.ynsy.ynsyandroidapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.finger.FingerLoginActivity;
import com.ynsy.ynsyandroidapp.util.L;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class NoticeService extends Service {

    private final static int NOTICE_SERVICE_ID = 1001;


    Notification notification;

    private static Thread uploadGpsThread;

    private boolean isrun = true;


    @Override
    public void onCreate() {
        super.onCreate();
        L.i("NoticeService onCreat");
    }

    @SuppressLint("WrongConstant")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.i("NoticeService onStartCommand");
        PendingIntent p_intent = PendingIntent.getActivity(this,0, new Intent(this, FingerLoginActivity.class), 0);
        notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ac3)
                .setWhen(System.currentTimeMillis())
                .setTicker("GPS测试")
                .setContentTitle("云南省水院")
                .setOngoing(true)
                .setPriority(PRIORITY_MAX)
                .setContentIntent(p_intent)
                .setAutoCancel(false)
                .build();
        /*使用startForeground,如果id为0，那么notification将不会显示*/
        startForeground(NOTICE_SERVICE_ID, notification);

        //2.开启线程（或者需要定时操作的事情）
        if(uploadGpsThread == null){
            uploadGpsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //这里用死循环就是模拟一直执行的操作
                    while (isrun){
                        //你需要执行的任务
                        try {
                            Thread.sleep(10000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

