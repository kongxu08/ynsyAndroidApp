package com.ynsy.ynsyandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.ynsy.ynsyandroidapp.finger.FingerLoginActivity;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.UrlManager;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 应用启动界面
 */
public class AppStart extends AppCompatActivity {

    Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = this;

        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View view = View.inflate(this, R.layout.activity_app_start, null);
        setContentView(view);

        // 渐变展示启动屏
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
        //每天更新一次通讯录
        try {
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            today = sdf.parse(sdf.format(today));
            long tipsUpdateValue = (long)SPUtils.get(this,"tipsUpdateTXL",0l);
            /*if(today.getTime()>tipsUpdateValue){*/
                new Thread(run).start();
                SPUtils.put(this,"tipsUpdateTXL",today.getTime());
/*            }else{
                animation.setDuration(800);
                view.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        redirectTo();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                });
            }*/
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    /**
     * 跳转到...
     */
    private void redirectTo() {
        String username = SPUtils.get(this,"username","").toString();
        if(username.equals("")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this, FingerLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(UrlManager.appTXLUrl)
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                String body = "";
                //成功
                if (response.isSuccessful()) {
                    body = response.body().string();
                    JSONObject rJson = new JSONObject(body);
                    SPUtils.put(activity,"TXL",rJson);
                    mHandler.sendEmptyMessage(0);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    redirectTo();
                    break;
            }
        }
    };
}
