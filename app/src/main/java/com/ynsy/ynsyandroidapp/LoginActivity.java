package com.ynsy.ynsyandroidapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Xml;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.PermissionsUtils;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.StringHelper;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;
import com.ynsy.ynsyandroidapp.webview.CommonWebView;
import com.hsinfo.encrypt.Decrypt;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private Activity activity;

    private Button loginBtn;
    private EditText edit_user;
    private EditText edit_password;

    private String username;
    private String pwd;

    private JSONObject userInfoJson;

    private View loadingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activity = this;

        loadingView = LoadingUtil.creatLoadingView(activity);

        edit_user = findViewById(R.id.et_usertel);
        edit_password = findViewById(R.id.et_password);

        edit_password.setOnKeyListener(onKey);

        loginBtn = findViewById(R.id.btn_login);
        // 登录
        loginBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                login();
            }
        });

        //两个日历权限和一个数据读写权限
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//        PermissionsUtils.showSystemSetting = false;//是否支持显示系统设置权限设置窗口跳转
        //这里的this不是上下文，是Activity对象！
        PermissionsUtils.getInstance().chekPermissions(this, permissions, permissionsResult);
    }

    //创建监听权限的接口对象
    PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
        @Override
        public void passPermissons() {
//            Toast.makeText(LoginActivity.this, "权限通过，可以做其他事情!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void forbitPermissons() {
//            finish();
            T.showLong(activity, "权限申请不通过");
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //就多一个参数this
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    View.OnKeyListener onKey = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int code = event.getAction();
            //回车键
            if (keyCode == KeyEvent.KEYCODE_ENTER && code == KeyEvent.ACTION_UP) {
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    //隐藏键盘
                    imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                }
                loginBtn.callOnClick();
                return true;
            }
            return false;
        }
    };

    //登陆
    private void login() {
        username = edit_user.getText().toString().trim();
        pwd = edit_password.getText().toString().trim();
        if(StringHelper.isEmpty(username)){
            T.showShort(activity,"请输入用户名");
            return;
        }
        if(StringHelper.isEmpty(pwd)){
            T.showShort(activity,"请输入密码");
            return;
        }
        loadingView.setVisibility(View.VISIBLE);
        new Thread(run).start();
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();

            try {
                pwd = Decrypt.Encrypt(pwd, Decrypt.SECRETKEY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //取致远token
            Request request = new Request.Builder()
                    .url(UrlManager.seeyonToken)
                    .get()
                    .build();
            String body = "";
            String token = null;
            try {
                Response response = client.newCall(request).execute();
                //成功则请求致远认证
                if (response.isSuccessful()) {
                    body = response.body().string();
                    JSONObject rJson = new JSONObject(body);
                    token = rJson.getString("id");
                    String authenticationUrl = UrlManager.seeyonHostAuthentication;
                    authenticationUrl += username;
                    authenticationUrl = authenticationUrl + "?token=" + token + "&password=" + pwd;
                    Request rst = new Request.Builder()
                            .url(authenticationUrl)
                            .get()
                            .build();
                    Response rps = client.newCall(rst).execute();
                    //认证成功则
                    if (rps.isSuccessful()) {
                        JSONObject jsonMsg = new JSONObject(rps.body().string());
                        if (jsonMsg.getBoolean("result")) {
                           //取当前登陆用户对象
/*                            RequestBody requestBody = new FormBody.Builder()
                                    .add("userName", username)
                                    .add("password", pwd)
                                    .add("mobileLogin", "true")
                                    .build();
                            Request oaRequest = new Request.Builder().url(UrlManager.getUserInfoUrl).post(requestBody).build();
                            Response oaResponse = client.newCall(oaRequest).execute();
                            if(oaResponse.isSuccessful()){
                                String userStr = oaResponse.body().string();
                                L.d(userStr);
                                JSONObject userJson = new JSONObject(userStr);
                                userInfoJson=userJson.getJSONObject("body").getJSONObject("map").getJSONObject("userInfo");
                                handler.sendEmptyMessage(1);
                            }*/
                            handler.sendEmptyMessage(1);
                        } else {
                            //认证失败
                            String name = jsonMsg.getString("message");
                            Message message = handler.obtainMessage();
                            message.what = 3;
                            message.obj = name;
                            handler.sendMessage(message);
                        }
                    } else {
                        handler.sendEmptyMessage(0);
                    }
                } else {
                    handler.sendEmptyMessage(0);
                }
            } catch (Exception e) {
                L.i(e.getMessage());
                if (e.getMessage().contains("failed to connect to")) {
                    handler.sendEmptyMessage(2);
                } else {
                    handler.sendEmptyMessage(0);
                }

            }


        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    loadingView.setVisibility(View.GONE);
                    T.showLong(activity, "登陆失败");

                    break;
                case 1:
                    loadingView.setVisibility(View.GONE);

//                    if (userInfoJson == null) {
//                        T.showLong(activity, "获取用户数据失败");
//                        break;
//                    }
                    SPUtils.put(activity, "username", username);
                    SPUtils.put(activity, "password", pwd);
//                    SPUtils.put(activity, "userInfo", userInfoJson);

                    //跳转
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, CommonWebView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", UrlManager.appRemoteHomePageUrl);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    finish();
                    break;
                case 2:
                    loadingView.setVisibility(View.GONE);
                    T.showLong(activity, "连接服务器超时");
                    break;
                case 3:
                    loadingView.setVisibility(View.GONE);
                    T.showLong(activity, msg.obj.toString());
                    break;
            }
        }
    };

/*    private JSONObject getUserInfo(OkHttpClient client, String username) {
        JSONObject jsonObject = null;
        try {
            //MediaType  设置Content-Type 标头中包含的媒体类型值
            FormBody formBody = new FormBody.Builder().add("userName", username).build();
            Request request = new Request.Builder()
                    .url(UrlManager.getUserInfoUrl)//请求的url
                    .post(formBody)
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                jsonObject = new JSONObject(response.body().string());
                jsonObject = jsonObject.getJSONObject("body").getJSONObject("map").getJSONObject("userInfo");
            } else {
                L.i(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }*/


    /**
     * 在屏幕上添加一个转动的小菊花（传说中的Loading），默认为隐藏状态
     * 注意：务必保证此方法在setContentView()方法后调用，否则小菊花将会处于最底层，被屏幕其他View给覆盖
     *
     * @param activity                    需要添加菊花的Activity
     * @param customIndeterminateDrawable 自定义的菊花图片，可以为null，此时为系统默认菊花
     * @return {ProgressBar}    菊花对象
     */
    private ProgressBar createProgressBar(Activity activity, Drawable customIndeterminateDrawable) {
        // activity根部的ViewGroup，其实是一个FrameLayout
        FrameLayout rootContainer = (FrameLayout) activity.findViewById(android.R.id.content);
        // 给progressbar准备一个FrameLayout的LayoutParams
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置对其方式为：屏幕居中对其
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;

        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(lp);
        // 自定义小菊花
        if (customIndeterminateDrawable != null) {
            progressBar.setIndeterminateDrawable(customIndeterminateDrawable);
        }
        // 将菊花添加到FrameLayout中
        rootContainer.addView(progressBar);
        return progressBar;
    }


}
