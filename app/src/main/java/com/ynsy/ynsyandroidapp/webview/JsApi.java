package com.ynsy.ynsyandroidapp.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.webkit.JavascriptInterface;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AndroidShare;
import com.ynsy.ynsyandroidapp.util.Base64Util;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.StringHelper;

import org.json.JSONException;
import org.json.JSONObject;

import wendu.dsbridge.CompletionHandler;

/**
 * Created by du on 16/12/31.
 */

public class JsApi {
    Context context;
    private JsApi(){}
    public JsApi(Context ctx){
        context = ctx;
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

    @JavascriptInterface
    public String getLoginUserId(Object obj) {
        return "0d8adc8eed4845a69ec37e3689cd622f";
    }

    @JavascriptInterface
    public String getLoginUserInfo(Object obj) {
        String msg = SPUtils.get(context, "userInfo", "{}").toString();
        return msg;
    }

    @JavascriptInterface
    public String getLoginToken(Object obj) {
        String msg = SPUtils.get(context, "token", "").toString();
        return msg;
    }
}