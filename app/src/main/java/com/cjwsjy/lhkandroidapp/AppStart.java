package com.cjwsjy.lhkandroidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.cjwsjy.lhkandroidapp.util.L;
import com.cjwsjy.lhkandroidapp.util.T;
import com.cjwsjy.lhkandroidapp.webview.CommonDWebView;
import com.ynsy.ynsyandroidapp.R;

import org.json.JSONException;
import org.json.JSONObject;

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

//        final Intent intent = new Intent(this,NoticeService.class);
//        startService(intent);

        // 渐变展示启动屏
        AlphaAnimation animation = new AlphaAnimation(0.1f, 1.0f);
        //每天更新一次通讯录
/*        try {
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            today = sdf.parse(sdf.format(today));
            long tipsUpdateValue = (long)SPUtils.get(this,"tipsUpdateTXL",0l);
            if(today.getTime()>tipsUpdateValue){
                new Thread(run).start();
            }else {*/
                animation.setDuration(800);
                view.startAnimation(animation);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation arg0) {
                        String param = "{'openUrl':'http://125.67.17.34:8108/ysbapp/Menu/getlogo','closeWebView':'http://125.67.17.34:8108/ysbapp/Menu/getBd/'}";
                        try {

                            JSONObject paramJson = new JSONObject(param);
                            String openUrl = paramJson.getString("openUrl");

                            Intent intent = new Intent(AppStart.this, CommonDWebView.class);
                            intent.putExtra("url", openUrl);//设置参数

                            if(paramJson.has("closeWebView")){
                                String closeWebView = paramJson.getString("closeWebView");
                                String[] closeWebViews = closeWebView.split(" ");
                                intent.putExtra("closeWebViews",closeWebViews);
                            }

                            startActivity(intent);
                            activity.finish();
                        } catch (JSONException e) {
                            T.showLong(activity,"openDWebView,参数错误");
                            L.e("openDWebView,参数错误");
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                });
/*            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        /*
            */
    }
    /**
     * 跳转到...
     */
/*    private void redirectTo() {
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
    }*/

/*    Runnable run = new Runnable() {
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

                    Date today = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    today = sdf.parse(sdf.format(today));
                    SPUtils.put(activity,"tipsUpdateTXL",today.getTime());

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
    };*/
}
