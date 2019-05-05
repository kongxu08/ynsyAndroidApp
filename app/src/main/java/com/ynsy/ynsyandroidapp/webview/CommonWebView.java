package com.ynsy.ynsyandroidapp.webview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ynsy.ynsyandroidapp.LoginActivity;
import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AndroidShare;
import com.ynsy.ynsyandroidapp.util.BadgeNum;
import com.ynsy.ynsyandroidapp.util.Base64Util;
import com.ynsy.ynsyandroidapp.util.DeviceUtil;
import com.ynsy.ynsyandroidapp.util.DownloadUtil;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.StringHelper;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommonWebView extends AppCompatActivity {

    private Activity activity;

    ProgressDialog progressDialog;
    View loadingWebView;

    private WebView webView;
    private String url;

    private String username;

    private String[] closeWebViews;

    private String versionNumber;
    private String changeContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_webview);

        activity = this;
        loadingWebView = LoadingUtil.creatWebLoadingView(activity);

        username = SPUtils.get(activity,"username","").toString();

        Intent intent = getIntent();
        String openUrl = intent.getStringExtra("url");
        url = openUrl;

        //获取指定关闭webView的url
        closeWebViews = intent.getStringArrayExtra("closeWebViews");
        appendCloseWebUrl();

//        progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
        webView = (WebView) findViewById(R.id.webviews);

        webView.clearCache(true);//清除缓

        //允许JavaScript执行
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//设置缓存模式

        webView.addJavascriptInterface(this,"android");

        webView.setWebChromeClient(new WebChromeClient() {

        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 断网或者网络连接超时
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
                    //view.loadUrl("file://" + getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/cjwsjy/errorPage/no_network.html");
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.i("LOGTAG", "shouldInterceptRequest url=" + url + ";threadInfo" + Thread.currentThread());
                WebResourceResponse response = null;
                if (url.contains("判断条件")) {
                    try {
                        URL u = new URL(url.replace("file:///", "").replace("%22", ""));//把file://替换掉
                        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                        InputStream inputStream = conn.getInputStream();
                        response = new WebResourceResponse("image/png", "UTF-8", inputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return response;
            }

            @Override
            // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                int result = 0;
                String suffix = "";
                // 判断url链接中是否含有某个字段，如果有就执行指定的跳转（不执行跳转url链接），如果没有就加载url链接
                if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }

                //附件下载
                //判断url末尾是否有doc等附件
                result = parseurl(url);

                if (result == 1) {
                    Toast.makeText(CommonWebView.this, "开始下载附件...", Toast.LENGTH_SHORT).show();
                    result = url.lastIndexOf(".");
                    suffix = url.substring(result + 1, url.length());

                    view.stopLoading();
                    return true;
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String username = SPUtils.get(activity,"username","").toString();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("userName",username);
                    String txlStr =SPUtils.get(activity,"TXL","").toString();
                    JSONObject txlJson = new JSONObject(txlStr);
                    jsonObject.put("contactBook",txlJson);
                    jsonObject.put("softVersion",DeviceUtil.getReleaseVersion(activity));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                view.loadUrl("javascript:window._NativeActivate_('"+jsonObject.toString()+"')");
            }

        });
        //网页加载进度条
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if (newProgress == 100) {
                    loadingWebView.setVisibility(View.GONE);
                } else {
                    LoadingUtil.pg.setProgress(newProgress);//设置进度值
                    loadingWebView.setVisibility(View.VISIBLE);
                }
            }
        });
        webView.loadUrl(openUrl);

        //开启线程 保存友盟推送的deviceToken
        new Thread(){
            @Override
            public void run() {
                initUserDevice();
            }
        }.start();

        //检查版本更新
        try {
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            today = sdf.parse(sdf.format(today));
            long tipsUpdateValue = (long)SPUtils.get(activity,"tipsUpdateValue",0l);
            if(today.getTime()>tipsUpdateValue){
                checkVersion();
                SPUtils.put(activity,"tipsUpdateValue",today.getTime());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }
    //双击返回退出标识
    private static boolean isExit = false;
    /*
    * 双击返回退出
    * */
    @Override
    public void onBackPressed() {
        String webU = webView.getUrl();
        if(closeWebViews.length>5){
            super.onBackPressed();
        }else{
            for (String tempUrl : closeWebViews){
                if (webU.equals(tempUrl)){
                    exit();
                    return;
                }
            }
            webView.goBack();
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
            //利用handler延迟发送更改状态信息
            handler.sendEmptyMessageDelayed(0, 2000);
        } else {
//            finish();
//            System.exit(0);
            moveTaskToBack(false);
        }
    }

    //新开窗口webview
    @JavascriptInterface
    public void openWebView(String openUrl) {//对应js中xxx.openWebView("")
        L.i(openUrl);
        Intent intent = new Intent(CommonWebView.this, CommonWebView.class);
        intent.putExtra("url", openUrl);//设置参数,""
        startActivity(intent);
    }

    //注销
    @JavascriptInterface
    public void logOut() {
        //开启线程 删除友盟推送的deviceToken
        new Thread(){
            @Override
            public void run() {
                deleteUserDevice();
            }
        }.start();
        SPUtils.remove(this,"username");
        SPUtils.remove(this,"password");
        Intent intent = new Intent(CommonWebView.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //下载附件并打开PDF
    @JavascriptInterface
    public void openFile(String url) {
        L.i(url);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("下载中...");
        progressDialog.setMax(100);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.show();
        downFile(url);
    }

    //应用更新
    @JavascriptInterface
    public void updateApp() {
        checkVersion();
    }

    //设置桌面角标
    @JavascriptInterface
    public void setBadge(String num) {//对应js中xxx.openWebView("")
        int dbCount = Integer.parseInt(num);
        BadgeNum.setBadgeNum(activity, dbCount);
    }

    @JavascriptInterface
    public void callShare(String msg) {
        if(StringHelper.isEmpty(msg)){
            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.AppDownload);
            AndroidShare as =new AndroidShare(activity);
            //分享到微信好友
            as.shareMsg("",null,null,"","",AndroidShare.DRAWABLE,bitmap);
        }else{
            Bitmap bitmap = Base64Util.base64ToFile(msg);
            if(bitmap!=null){
                AndroidShare as =new AndroidShare(activity);
                //分享到微信好友
                as.shareMsg("",null,null,"","",AndroidShare.DRAWABLE,bitmap);
            }
        }
    }

    /**
     * 文件下载
     */
    private void downFile(String url) {
        DownloadUtil.get().download(url, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), url.substring(url.lastIndexOf("/")),
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        //下载完成进行相关逻辑操作
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0;
                        msg.obj=file;
                        mHandler.sendMessage(msg);
                    }
                    @Override
                    public void onDownloading(int progress) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = 11;
                        msg.arg1 = progress;
                        mHandler.sendMessage(msg);
                    }
                    @Override
                    public void onDownloadFailed(Exception e) {
                        //下载异常进行相关提示操作
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = e;
                        mHandler.sendMessage(msg);
                    }
                });
    }

    Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    T.showLong(activity,"下载完成");
                    if(progressDialog!=null){
                        progressDialog.dismiss();
                    }
                    File file = (File) msg.obj;
                    //下载完成后打开
                    Intent intent = new Intent("android.intent.action.VIEW");
                    Uri uri = null;
                    // 判断版本大于等于7.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri = FileProvider.getUriForFile(activity, "com.ynsy.ynsyandroidapp.FileProvider", file);
                    } else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        uri = Uri.fromFile(file);
                    }
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setDataAndType (uri, "application/pdf");
                    startActivity(Intent.createChooser(intent, "打开方式"));

                    break;
                case 1:
                    T.showLong(activity,"下载失败");
                    if(progressDialog!=null){
                        progressDialog.dismiss();
                    }
                    break;
                case 11:
                    if(progressDialog!=null){
                        progressDialog.setProgress(msg.arg1);
                    }
                    break;
                case 22:
                    /*AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setTitle("发现新版本")
                            .setMessage(changeContent)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    progressDialog = new ProgressDialog(activity);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setMessage("下载中...");
                                    progressDialog.setMax(100);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setCancelable(true);
                                    progressDialog.show();
                                    new Thread(downLoadRun).start();
                                }
                            }).create();
                    dialog.show();*/
                     new SweetAlertDialog(activity)
                            .setTitleText("发现新版本")
                            .setContentText(changeContent)
                            .setConfirmText("下载")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.cancel();
                                    progressDialog = new ProgressDialog(activity);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progressDialog.setMessage("下载中...");
                                    progressDialog.setMax(100);
                                    progressDialog.setCanceledOnTouchOutside(false);
                                    progressDialog.setCancelable(true);
                                    progressDialog.show();
                                    new Thread(downLoadRun).start();
                                }
                            })
                            .setCancelButton("明天提醒", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.cancel();
                                }
                            }).show();

                    break;
                case 23:
                    T.showLong(activity,"下载完成");
                    if(progressDialog!=null){
                        progressDialog.dismiss();
                    }
                    File apkfile = (File) msg.obj;
                    // 通过Intent安装APK文件
                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                    Uri uri2 = null;

                    // 判断版本大于等于7.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent2.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri2 = FileProvider.getUriForFile(activity, "com.ynsy.ynsyandroidapp.FileProvider", apkfile);
                    } else {
                        //intent.addCategory("android.intent.category.DEFAULT");
                        //设置intent的data和Type属性。
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        uri2 = Uri.fromFile(apkfile);
                    }
                    intent2.setDataAndType(uri2, "application/vnd.android.package-archive");
                    //跳转
                    activity.startActivity(intent2);

                    break;
                case 24:
                    T.showShort(activity,"已是最新版本");
                    break;
            }
        }
    };


    /**
     * @param url
     * @return 返回1 有附件，返回0 没有附件
     * @author chenyu 判断url地址中末尾是否有doc等附件文件
     * @time 2015年11月11日
     */
    private int parseurl(String url) {
        int length = 0;
        boolean bresult;
        String strurl = "";
        String strcut = "";

        strurl = url;
        length = strurl.length();

        //取最后3位
        strcut = strurl.substring(length - 4, length);

        bresult = strcut.equals(".doc");
        if (bresult == true) return 1;

        bresult = strcut.equals(".ppt");
        if (bresult == true) return 1;

        bresult = strcut.equals(".xls");
        if (bresult == true) return 1;

        bresult = strcut.equals(".pdf");
        if (bresult == true) return 1;

        bresult = strcut.equals(".txt");
        if (bresult == true) return 1;

        //取最后4位
        strcut = strurl.substring(length - 5, length);
        bresult = strcut.equals(".docx");
        if (bresult == true) return 1;

        bresult = strcut.equals(".pptx");
        if (bresult == true) return 1;

        bresult = strcut.equals(".xlsx");
        if (bresult == true) return 1;

        //没有附件
        return 0;
    }

    private void appendCloseWebUrl(){
        if(closeWebViews==null){
            closeWebViews = new String[5];
            closeWebViews[0]=UrlManager.appRemoteHomePageUrl;
            closeWebViews[1]=UrlManager.appRemoteHomePageUrl+"app";
            closeWebViews[2]=UrlManager.appRemoteHomePageUrl+"todo";
            closeWebViews[3]=UrlManager.appRemoteHomePageUrl+"contact";
            closeWebViews[4]=UrlManager.appRemoteHomePageUrl+"account";
        }else{
            String[] strs = new String[closeWebViews.length+5];
            for (int i = 0; i <closeWebViews.length ; i++) {
                strs[i]=closeWebViews[i];
            }
            strs[closeWebViews.length]=UrlManager.appRemoteHomePageUrl;
            strs[closeWebViews.length+1]=UrlManager.appRemoteHomePageUrl+"app";
            strs[closeWebViews.length+2]=UrlManager.appRemoteHomePageUrl+"todo";
            strs[closeWebViews.length+3]=UrlManager.appRemoteHomePageUrl+"contact";
            strs[closeWebViews.length+4]=UrlManager.appRemoteHomePageUrl+"account";
            closeWebViews=strs;
        }
    }

    public void initUserDevice(){
        String userInfo = SPUtils.get(activity,"userInfo","").toString();
        String deviceToken = SPUtils.get(activity,"deviceToken","").toString();
        if(!StringHelper.isEmpty(userInfo) && !StringHelper.isEmpty(deviceToken)){
            OkHttpClient client = new OkHttpClient();
            try{
                JSONObject userInfoJson=new JSONObject(userInfo);
                FormBody formBody = new FormBody.Builder()
                        .add("userName",userInfoJson.getString("loginName"))
                        .add("userDisplayName",userInfoJson.getString("name"))
                        .add("devicesId",SPUtils.get(activity,"deviceToken","").toString())
                        .add("devicesType","android")
                        .add("userId",userInfoJson.getString("id"))
                        .add("machine",DeviceUtil.getManufacturer()+"_"+DeviceUtil.getDeviceModel())
                        .add("os",DeviceUtil.getSystemVersion())
                        .add("appVersion",DeviceUtil.getReleaseVersion(activity)).build();
                Request request = new Request.Builder()
                        .url(UrlManager.userDeviceSaveOrUpdateUrl)
                        .post(formBody)
                        .build();
                Response response = client.newCall(request).execute();
                if(response.isSuccessful()){
                    L.i("用户devicesToken绑定成功");
                }else{
                    L.i("用户devicesToken绑定失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteUserDevice(){
            try{
                OkHttpClient client = new OkHttpClient();
                FormBody formBody = new FormBody.Builder()
                        .add("userName",username).build();
                Request request = new Request.Builder()
                        .url(UrlManager.userDeviceDeleteUrl)
                        .post(formBody)
                        .build();
                Response response = client.newCall(request).execute();
                if(response.isSuccessful()){
                    L.i("用户devicesToken解绑成功");
                }else{
                    L.i("用户devicesToken解绑失败");
                }
            }catch (IOException e) {
                e.printStackTrace();
            }

    }

    private void checkVersion(){
        new Thread(run).start();
    }

    //检查版本更新
    Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(UrlManager.appVersionUrl)
                        .get()
                        .build();
                Response response = client.newCall(request).execute();
                String body = "";
                if (response.isSuccessful()) {
                    body = response.body().string();
                    JSONObject rJson = new JSONObject(body);
                    versionNumber = rJson.getString("versionNumber");
                    changeContent = rJson.getString("changeContent");
                    String curVersion = DeviceUtil.getReleaseVersion(activity);
                    if(!versionNumber.equals(curVersion)){
                       mHandler.sendEmptyMessage(22);
                    }else{
                        mHandler.sendEmptyMessage(24);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    /**
     * 升级
     */
    private void updateApk(String url) {
        DownloadUtil.get().download(url, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), url.substring(url.lastIndexOf("/")),
                new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess(File file) {
                        //下载完成进行相关逻辑操作
                        Message msg = mHandler.obtainMessage();
                        msg.what = 23;
                        msg.obj=file;
                        mHandler.sendMessage(msg);
                    }
                    @Override
                    public void onDownloading(int progress) {
                        Message msg = mHandler.obtainMessage();
                        msg.what = 11;
                        msg.arg1 = progress;
                        mHandler.sendMessage(msg);
                    }
                    @Override
                    public void onDownloadFailed(Exception e) {
                        //下载异常进行相关提示操作
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = e;
                        mHandler.sendMessage(msg);
                    }
                });
    }

    Runnable downLoadRun = new Runnable() {
        @Override
        public void run() {
            updateApk(UrlManager.appDownLoadUrl);
        }
    };

}