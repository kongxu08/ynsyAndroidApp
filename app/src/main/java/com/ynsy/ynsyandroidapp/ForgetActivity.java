package com.ynsy.ynsyandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hsinfo.encrypt.Decrypt;
import com.ynsy.ynsyandroidapp.common.LoadingActivity;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.StringHelper;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgetActivity extends AppCompatActivity {

    EditText et_phoneNumber;
    EditText et_fpw;
    EditText et_spw;
    EditText et_token;
    Button token;
    Button commit;
    View loadingView ;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        context=this;

        et_phoneNumber = findViewById(R.id.et_phone_number);
        et_fpw = findViewById(R.id.et_fpw);
        et_spw = findViewById(R.id.et_spw);
        et_token = findViewById(R.id.et_token);

        loadingView = LoadingUtil.creatLoadingView((Activity) context);

        token = findViewById(R.id.token_btn);
        commit = findViewById(R.id.commit_btn);
        token.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(StringHelper.isEmpty(et_phoneNumber.getText().toString().trim())){
                    T.showShortError(context,"请输入手机号",false);
                    return;
                }

                token.setEnabled(false);
                token.setBackgroundColor(Color.parseColor("#BDBDBD"));

                new CountDownTimer(60000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        token.setText("获取验证码(" + (millisUntilFinished / 1000+1) + ")");
                    }
                    @Override
                    public void onFinish() {
                        token.setText("获取验证码");
                        token.setBackgroundColor(Color.parseColor("#00A2E8"));
                        token.setEnabled(true);
                    }
                }.start();

                new Thread(getTokenRun).start();
            }
        });

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringHelper.isEmpty(et_phoneNumber.getText().toString().trim())){
                    T.showShortError(context,"请输入手机号",false);
                    return;
                }

                if(StringHelper.isEmpty(et_fpw.getText().toString().trim())){
                    T.showShortError(context,"请输入新密码",false);
                    return;
                }

                if(StringHelper.isEmpty(et_spw.getText().toString().trim())){
                    T.showShortError(context,"请再次输入新密码",false);
                    return;
                }

                if(StringHelper.isEmpty(et_token.getText().toString().trim())){
                    T.showShortError(context,"请输入验证码",false);
                    return;
                }

                if(!et_fpw.getText().toString().trim().equals(et_spw.getText().toString().trim())){
                    T.showShortError(context,"两次输入的密码不一致",false);
                    et_spw.setText("");
                    return;
                }


                String pattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[$@$!%*?])[A-Za-z\\d$@$!%*?&]{8,}";
                boolean isMatch = Pattern.matches(pattern, et_fpw.getText().toString().trim());
                if(!isMatch){
                    T.showShortError(context,"密码强度不满足要求",false);
                    et_fpw.setText("");
                    et_spw.setText("");
                    return;
                }

                loadingView.setVisibility(View.VISIBLE);
                commit.setBackgroundColor(Color.parseColor("#BDBDBD"));
                commit.setEnabled(false);
                new Thread(run).start();
            }
        });
    }

    Runnable run = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(10000,TimeUnit.MILLISECONDS).build();

            try {
                String mobile= Decrypt.Encrypt(et_phoneNumber.getText().toString().trim(), Decrypt.SECRETKEY);
                String pwd= Decrypt.Encrypt(et_fpw.getText().toString().trim(), Decrypt.SECRETKEY);
                String verifCode= Decrypt.Encrypt(et_token.getText().toString().trim(), Decrypt.SECRETKEY);
                //取当前登陆用户对象
                RequestBody requestBody = new FormBody.Builder()
                        .add("mobile", mobile)
                        .add("newPassword", pwd)
                        .add("verifCode", verifCode)
                        .build();
                Request oaRequest = new Request.Builder().url(UrlManager.Host+"/hr/a/commonApi/modifyPassword").post(requestBody).build();
                Response oaResponse = client.newCall(oaRequest).execute();
                if(oaResponse.isSuccessful()){
                    String userStr = oaResponse.body().string();
                    JSONObject jsonObject = new JSONObject(userStr);
                    if(jsonObject.getString("errorCode").equals("0")){
                        Message message = handler.obtainMessage();
                        message.what = 1;
                        message.obj = jsonObject.getString("msg");
                        handler.sendMessage(message);
                        return;
                    }else{
                        Message message = handler.obtainMessage();
                        message.what = 3;
                        message.obj = jsonObject.getString("msg");
                        handler.sendMessage(message);
                    }
                }else{
                    Message message = handler.obtainMessage();
                    message.what = 0;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                if (e.getMessage().contains("Failed to connect to")) {
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

    Runnable getTokenRun = new Runnable() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient();
            try {
                String mobile= Decrypt.Encrypt(et_phoneNumber.getText().toString().trim(), Decrypt.SECRETKEY);
                //取当前登陆用户对象
                RequestBody requestBody = new FormBody.Builder()
                        .add("mobile", mobile)
                        .build();
                Request oaRequest = new Request.Builder().url(UrlManager.Host+"/hr/a/commonApi/sendVerifCode").post(requestBody).build();
                Response oaResponse = client.newCall(oaRequest).execute();
                if(oaResponse.isSuccessful()){
                }else{
                }
            } catch (Exception e) {
                L.i(e.getMessage());
                if (e.getMessage().contains("Failed to connect to")) {
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

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    loadingView.setVisibility(View.GONE);
                    commit.setBackgroundColor(Color.parseColor("#00A2E8"));
                    commit.setEnabled(true);
                    T.showShortError(context, "服务器无响应",true);
                    break;
                case 1:
                    loadingView.setVisibility(View.GONE);
                    commit.setBackgroundColor(Color.parseColor("#00A2E8"));
                    commit.setEnabled(true);
                    T.showShortError(context, msg.obj.toString(),true);
                    finish();
                    break;
                case 2:
                    loadingView.setVisibility(View.GONE);
                    commit.setBackgroundColor(Color.parseColor("#00A2E8"));
                    commit.setEnabled(true);
                    T.showShortError(context, "连接服务器超时",true);
                    break;
                case 3:
                    loadingView.setVisibility(View.GONE);
                    commit.setBackgroundColor(Color.parseColor("#00A2E8"));
                    commit.setEnabled(true);
                    T.showShortError(context, msg.obj.toString(),true);
                    break;
            }
        }
    };
}
