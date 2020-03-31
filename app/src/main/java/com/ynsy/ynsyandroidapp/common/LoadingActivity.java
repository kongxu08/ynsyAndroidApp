package com.ynsy.ynsyandroidapp.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;

import com.ynsy.ynsyandroidapp.LoginActivity;
import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.finger.FingerLoginActivity;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;
import com.ynsy.ynsyandroidapp.webview.CommonWebView;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoadingActivity extends AppCompatActivity {
    Activity activity;
    private JSONObject userInfoJson;
    private String zhbgToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        activity = this;

        //每天更新一次通讯录
        try {
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            today = sdf.parse(sdf.format(today));
            long tipsUpdateValue = (long)SPUtils.get(this,"tipsUpdateTXL",0l);
            if(today.getTime()>tipsUpdateValue){
                new Thread(run).start();
            }else {
                new Thread(runNoTXL).start();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
    Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                String username = SPUtils.get(activity,"username","").toString();
                String pwd = SPUtils.get(activity,"password","").toString();
                RequestBody requestBody = new FormBody.Builder()
                        .add("userName", username)
                        .add("password", pwd)
                        .build();
                Request oaRequest = new Request.Builder().url(UrlManager.getUserInfoUrl).post(requestBody).build();
                Response oaResponse = client.newCall(oaRequest).execute();
                if(oaResponse.isSuccessful()){
                    String userStr = oaResponse.body().string();
                    JSONObject userJson = new JSONObject(userStr);
                    if(userJson.getBoolean("success")){
                        userInfoJson=userJson.getJSONObject("body").getJSONObject("userInfo");
                        zhbgToken=userJson.getJSONObject("body").getString("token");
                        SPUtils.put(activity, "userInfo", userInfoJson);
                        SPUtils.put(activity, "token", zhbgToken);
                    }else{
                        //认证失败
                        String name = userInfoJson.getString("msg");
                        Message message = handler.obtainMessage();
                        message.what = 3;
                        message.obj = name;
                        handler.sendMessage(message);
                        return;
                    }
                }else{
                    handler.sendEmptyMessage(0);
                    return;
                }

                FormBody formBody = new FormBody.Builder().build();
                Request request = new Request.Builder()
                        .url(UrlManager.appTXLUrl)
                        .addHeader("token",zhbgToken)
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                String body = "";
                //成功
                if (response.isSuccessful()) {
                    body = response.body().string();
                    JSONObject rJson = new JSONObject(body);
                    Object TXL = rJson.getJSONObject("body").get("phoneBookJson");
                    SPUtils.put(activity,"TXL",TXL);

                    Date today = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    today = sdf.parse(sdf.format(today));
                    SPUtils.put(activity,"tipsUpdateTXL",today.getTime());
                }
                handler.sendEmptyMessage(200);
            }catch (Exception e){
                L.i(e.getMessage());
                if (e.getMessage().contains("failed to connect to")) {
                    handler.sendEmptyMessage(2);
                } else {
                    Message message = handler.obtainMessage();
                    message.what = 3;
                    message.obj = e.getMessage();
                    handler.sendMessage(message);
                }
            }

        }
    };

    Runnable runNoTXL = new Runnable() {
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();

                String username = SPUtils.get(activity,"username","").toString();
                String pwd = SPUtils.get(activity,"password","").toString();
                RequestBody requestBody = new FormBody.Builder()
                        .add("userName", username)
                        .add("password", pwd)
                        .build();
                Request oaRequest = new Request.Builder().url(UrlManager.getUserInfoUrl).post(requestBody).build();
                Response oaResponse = client.newCall(oaRequest).execute();
                if(oaResponse.isSuccessful()){
                    String userStr = oaResponse.body().string();
                    JSONObject userJson = new JSONObject(userStr);
                    if(userJson.getBoolean("success")){
                        userInfoJson=userJson.getJSONObject("body").getJSONObject("userInfo");
                        zhbgToken=userJson.getJSONObject("body").getString("token");
                        SPUtils.put(activity, "userInfo", userInfoJson);
                        SPUtils.put(activity, "token", zhbgToken);
                        handler.sendEmptyMessage(200);
                        return;
                    }else{
                        //认证失败
                        String name = userInfoJson.getString("msg");
                        Message message = handler.obtainMessage();
                        message.what = 3;
                        message.obj = name;
                        handler.sendMessage(message);
                        return;
                    }
                }else{
                    handler.sendEmptyMessage(2);
                }
            }catch (Exception e){
                L.i(e.getMessage());
                if (e.getMessage().contains("failed to connect to")) {
                    handler.sendEmptyMessage(2);
                } else {
                    Message message = handler.obtainMessage();
                    message.what = 3;
                    message.obj = e.getMessage();
                    handler.sendMessage(message);
                }
            }

        }
    };

    /**
     * 跳转到...
     */
    private void redirectTo() {
        //跳转
        Intent intent = new Intent();
        intent.setClass(LoadingActivity.this, CommonWebView.class);
        Bundle bundle = new Bundle();
        bundle.putString("url", UrlManager.appRemoteHomePageUrl);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    /**
     * 跳转到...
     */
    private void redirectToLogin() {
        //跳转
        Intent intent = new Intent();
        intent.setClass(LoadingActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    Handler handler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
        super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    T.showLongError(activity, "服务器无响应",true);
                    redirectToLogin();
                    break;
                case 2:
                    T.showLongError(activity, "连接服务器超时",true);
                    redirectToLogin();
                    break;
                case 3:
                    T.showLongError(activity, "认证失败",true);
                    redirectToLogin();
                    break;
                case 4:
                    T.showLongError(activity, "获取通讯录失败",true);
                    redirectToLogin();
                    break;
                case 200:
                    redirectTo();
                    break;
            }
        }
    };
}
