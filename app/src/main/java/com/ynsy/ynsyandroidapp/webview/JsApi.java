package com.ynsy.ynsyandroidapp.webview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.Environment;
import android.view.Gravity;
import android.webkit.JavascriptInterface;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AndroidShare;
import com.ynsy.ynsyandroidapp.util.Base64Util;
import com.ynsy.ynsyandroidapp.util.StringHelper;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import wendu.dsbridge.CompletionHandler;

/**
 * Created by du on 16/12/31.
 */

public class JsApi {
    Context context;
    CommonDWebView activity;
    private JsApi(){}
    public JsApi(CommonDWebView activity){
        this.activity = activity;
        context = activity.getApplicationContext();
    }
    @JavascriptInterface
    public void callShare(String msg) {
        if(StringHelper.isEmpty(msg)){
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.appdownload);
            AndroidShare as =new AndroidShare(context);
            //分享到微信好友
            as.shareMsg("",null,null,"","",AndroidShare.DRAWABLE,bitmap);
        }else{
            Bitmap bitmap = Base64Util.base64ToFile(msg);
            if(bitmap!=null){
                AndroidShare as =new AndroidShare(context);
                //分享到微信好友
                as.shareMsg("",null,null,"","",AndroidShare.DRAWABLE,bitmap);
            }
        }
    }

    @JavascriptInterface
    public String testSyn(Object msg)  {
        return msg + "［syn call］";
    }

    @JavascriptInterface
    public void testAsyn(Object msg, CompletionHandler<String> handler){
        handler.complete(msg+" [ asyn call]");
    }

    @JavascriptInterface
    public String testNoArgSyn(Object arg) throws JSONException {
        return  "testNoArgSyn called [ syn call]";
    }

    @JavascriptInterface
    public void testNoArgAsyn(Object arg, CompletionHandler<String> handler) {
        handler.complete( "testNoArgAsyn   called [ asyn call]");
    }

    //@JavascriptInterface
    //without @JavascriptInterface annotation can't be called
    public String testNever(Object arg) throws JSONException {
        JSONObject jsonObject= (JSONObject) arg;
        return jsonObject.getString("msg") + "[ never call]";
    }

    @JavascriptInterface
    public void callProgress(Object args, final CompletionHandler<Integer> handler) {

        new CountDownTimer(11000, 1000) {
            int i=10;
            @Override
            public void onTick(long millisUntilFinished) {
                //setProgressData can be called many times util complete be called.
                handler.setProgressData((i--));

            }
            @Override
            public void onFinish() {
                //complete the js invocation with data; handler will be invalid when complete is called
                handler.complete(0);

            }
        }.start();
    }

    //应用更新
    @JavascriptInterface
    public void updateApp(Object obj) {
        activity.checkVer();
    }

    //下载附件
    @JavascriptInterface
    public void openFile(Object obj) {
        UrlManager.appDownLoadUrl=obj.toString();
        new Thread(activity.downLoadRun).start();
    }

    @JavascriptInterface
    public void openDWebView(Object obj) {
        try {
            JSONObject paramJson = new JSONObject(obj.toString());
            String openUrl = paramJson.getString("openUrl");

            Intent intent = new Intent(context, CommonDWebView.class);
            intent.putExtra("url", openUrl);//设置参数

            if(paramJson.has("closeUrl")){
                String closeWebView = paramJson.getString("closeUrl");
                String[] closeWebViews = closeWebView.split(" ");
                intent.putExtra("closeWebViews",closeWebViews);
            }

            if(paramJson.has("orientation")){
                String orientation = paramJson.getString("orientation");
                intent.putExtra("orientation",orientation);
            }

            if(paramJson.has("nav")){
                String nav = paramJson.getString("nav");
                intent.putExtra("nav",nav);
            }

            context.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //开始录音
    //结束录音
    //获取已存在的录音地址，不存在则返回空
    //删除录音
    //播放录音
    //停止播放
}