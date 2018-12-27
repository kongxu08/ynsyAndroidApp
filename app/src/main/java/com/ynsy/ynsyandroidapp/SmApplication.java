package com.ynsy.ynsyandroidapp;

import android.app.Application;
import android.content.Context;

import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

/**
 * 全局变量类
 */
public class SmApplication extends Application {
    public static String deviceToken;
    private Context ctx;
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this.getApplicationContext();
        //初始化友盟推送
        UMConfigure.init(this, "5c2191cff1f55661a1000823", "UMENG_CHANNEL", UMConfigure.DEVICE_TYPE_PHONE, "efb4a1130b7d1a06305f7890b7c68184");
        PushAgent mPushAgent = PushAgent.getInstance(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                L.i("初始化友盟推送成功：" + deviceToken);
                SmApplication.deviceToken = deviceToken;
                SPUtils.put(ctx,"deviceToken",deviceToken);
            }

            @Override
            public void onFailure(String s, String s1) {
                L.i("初始化友盟推送失败s：" + s + " s1：" + s1);
            }
        });
    }
}
