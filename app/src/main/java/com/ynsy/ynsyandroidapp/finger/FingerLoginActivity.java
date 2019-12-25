package com.ynsy.ynsyandroidapp.finger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.ynsy.ynsyandroidapp.LoginActivity;
import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.common.LoadingActivity;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.UrlManager;
import com.ynsy.ynsyandroidapp.webview.CommonWebView;
import com.hsinfo.encrypt.Decrypt;

import java.lang.ref.WeakReference;

public class FingerLoginActivity extends AppCompatActivity {

    private FingerprintManagerCompat manager;
    private CancellationSignal cancel;
    private FingerprintManagerCompat.AuthenticationCallback callback;
    private MyHandler handler = new MyHandler(this);

    private FingerDialogFragment fingerDialogFragment;

    private EditText et_password;
    private Button btn_login;

    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity=this;

        setContentView(R.layout.activity_finger_login);

        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        et_password.setOnKeyListener(onKey);
        // 登录
        btn_login.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                login();
            }
        });

        cancel = new CancellationSignal();

        manager = FingerprintManagerCompat.from(this);
        if (!manager.isHardwareDetected()) {
            //是否支持指纹识别
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("没有指纹传感器");
            builder.setCancelable(true);
            builder.create().show();
        } else if (!manager.hasEnrolledFingerprints()) {
            //是否已注册指纹
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("没有注册指纹");
            builder.setCancelable(true);
            builder.create().show();
        } else {

            //弹出指纹验证提示框
            fingerDialogFragment = new FingerDialogFragment();
            fingerDialogFragment.show(getSupportFragmentManager(),"fingerDialogFragment");

            try {
                callback = new FingerprintManagerCompat.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        super.onAuthenticationError(errMsgId, errString);
                        //验证错误时，回调该方法。当连续验证5次错误时，将会走onAuthenticationFailed()方法
                        handler.obtainMessage(1, errMsgId, 0).sendToTarget();
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        //验证成功时，回调该方法。fingerprint对象不能再验证
                        handler.obtainMessage(2).sendToTarget();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        //验证失败时，回调该方法。fingerprint对象不能再验证并且需要等待一段时间才能重新创建指纹管理对象进行验证
                        handler.obtainMessage(3).sendToTarget();
                    }
                };

                //这里去新建一个结果的回调，里面回调显示指纹验证的信息
                manager.authenticate(null, 0, cancel, callback, handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //绑定回车键盘
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
                btn_login.callOnClick();
                return true;
            }
            return false;
        }
    };

    //登陆
    private void login() {
        String password = et_password.getText().toString().trim();
        try{
            password = Decrypt.Encrypt(password,Decrypt.SECRETKEY);
        }catch (Exception e){
            e.printStackTrace();
        }
        String localPwd = SPUtils.get(activity,"password","").toString();
        if(password.equals(localPwd)){
            redirectTo();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("密码错误请重试");
            builder.setCancelable(true);
            builder.create().show();
        }

    }
    //跳转
    private void redirectTo(){
        Intent intent = new Intent();
        intent.setClass(FingerLoginActivity.this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }

    static class MyHandler extends Handler {
        WeakReference<FingerLoginActivity> mActivity;

        MyHandler(FingerLoginActivity activity) {
            mActivity = new WeakReference<FingerLoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FingerLoginActivity activity = mActivity.get();
            if (activity != null) {
                //todo 逻辑处理
                switch (msg.what) {
                    case 1:   //验证错误
                        //todo 界面处理
                        activity.handleErrorCode(msg.arg1);
                        break;
                    case 2:   //验证成功
                        //todo 界面处理
                        activity.handleCode(200);
                        break;
                    case 3:    //验证失败
                        //todo 界面处理
                        activity.handleCode(500);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }

        ;
    }

    //对应不同的错误，可以有不同的操作
    private void handleErrorCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                //todo 指纹传感器不可用，该操作被取消
                L.i("指纹传感器不可用，该操作被取消");
                fingerDialogFragment.setTextView("指纹传感器不可用，该操作被取消");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                //todo 当前设备不可用，请稍后再试
                L.i("当前设备不可用，请稍后再试");
                fingerDialogFragment.setTextView("当前设备不可用，请稍后再试");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                //todo 由于太多次尝试失败导致被锁，该操作被取消
                L.i("由于太多次尝试失败导致被锁，该操作被取消");
                fingerDialogFragment.setTextView("由于太多次尝试失败导致被锁，该操作被取消");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                //todo 没有足够的存储空间保存这次操作，该操作不能完成
                L.i("没有足够的存储空间保存这次操作，该操作不能完成");
                fingerDialogFragment.setTextView("没有足够的存储空间保存这次操作，该操作不能完成");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                //todo 操作时间太长，一般为30秒
                L.i("指纹传感器超时");
                fingerDialogFragment.setTextView("指纹传感器超时");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                //todo 传感器不能处理当前指纹图片
                L.i("传感器不能处理当前指纹图片");
                fingerDialogFragment.setTextView("传感器不能处理当前指纹图片");
                break;
        }
        fingerDialogFragment.setImageView(R.drawable.ic_fingerprint_error);
    }

    //对应不同的错误，可以有不同的操作
    private void handleCode(int code) {
        switch (code) {
            case 500:
                //todo 指纹传感器不可用，该操作被取消
                L.i("验证失败");
                fingerDialogFragment.setTextView("验证失败");
                fingerDialogFragment.setImageView(R.drawable.ic_fingerprint_error);
                break;
            case 200:
                //todo 当前设备不可用，请稍后再试
                L.i("验证成功");
                fingerDialogFragment.setTextView("验证成功");
                fingerDialogFragment.setImageView(R.drawable.ic_fingerprint_success);
                redirectTo();
                break;
        }
    }
}
