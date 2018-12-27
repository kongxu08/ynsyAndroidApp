package com.ynsy.ynsyandroidapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.ynsy.ynsyandroidapp.finger.FingerLoginActivity;
import com.ynsy.ynsyandroidapp.util.SPUtils;

/**
 * 应用启动界面
 */
public class AppStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View view = View.inflate(this, R.layout.activity_app_start, null);
        setContentView(view);

        // 渐变展示启动屏
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
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
}
