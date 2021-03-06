package com.ynsy.ynsyandroidapp.webview;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.hsinfo.encrypt.Decrypt;

import com.ynsy.ynsyandroidapp.LoginActivity;
import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AndroidShare;
import com.ynsy.ynsyandroidapp.util.BadgeNum;
import com.ynsy.ynsyandroidapp.util.Base64Util;
import com.ynsy.ynsyandroidapp.util.DeviceUtil;
import com.ynsy.ynsyandroidapp.util.DownloadUtil;
import com.ynsy.ynsyandroidapp.util.FileTypeHelper;
import com.ynsy.ynsyandroidapp.util.HttpUtil;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.PermissionsUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommonWebView extends AppCompatActivity {

    private Activity activity;
    private String[] closeWebViews;
    private ImageView btn_back;
    private TextView tv_title;
    @BindView(R.id.tv_close)
    public ImageView tv_close;
    @OnClick(R.id.tv_close)
    public void closeWebView(View view){
        finish();
    }
    private static boolean back=false;

    ProgressDialog progressDialog;
    View loadingWebView;

    private WebView webView;
    private String url;

    private String username;
    private String zhbgToken;

    private String versionNumber;
    private String changeContent;

    private String[] permissions = new String[]{Manifest.permission.CAMERA };
    private Uri imageUri;
    private int FILE_CHOOSER_RESULT_CODE=1;
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

    private String htmlContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String openUrl = intent.getStringExtra("url");
        url = openUrl;
        //获取指定关闭webView的url
        closeWebViews = intent.getStringArrayExtra("closeWebViews");

        //通过参数设置屏幕是否跟随系统旋转
        String orientation = intent.getStringExtra("orientation");
        if(orientation!=null){
            // 设置为跟随系统sensor的状态
            if(orientation.equals("sensor")){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }else if(orientation.equals("landscape")){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        String nav = intent.getStringExtra("nav");
        String title = intent.getStringExtra("title");
        if(nav!=null){
            if(nav.equals("0")){
                setContentView(R.layout.activity_common_no_nav_webview);
            }else{
                setContentView(R.layout.activity_common_webview);
                ButterKnife.bind(this);
                tv_title = findViewById(R.id.tv_navtitle);
                if(title!=null){
                    tv_title.setText(title);
                }
                btn_back = findViewById(R.id.iv_back);
                //绑定返回点击事件
                btn_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
            }
        }else{
            setContentView(R.layout.activity_common_webview);
            tv_title = findViewById(R.id.tv_navtitle);
            if(title!=null){
                tv_title.setText(title);
            }
            btn_back = findViewById(R.id.iv_back);
            //绑定返回点击事件
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        activity = this;
        loadingWebView = LoadingUtil.creatWebLoadingView(activity);

        username = SPUtils.get(activity,"username","").toString();
        zhbgToken = SPUtils.get(activity,"token","").toString();
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

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 断网或者网络连接超时
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
                    Intent intent = new Intent(activity,LoginActivity.class);
                    activity.startActivity(intent);
                    finish();
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                L.i(url);
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
                //邮箱中遇见下载的情况
                if(url.startsWith("http://mail.ynwdi.com/coremail")&&url.contains("&mode=download")){
                    CookieManager cookieManager = CookieManager.getInstance();
                    final String cookieStr = cookieManager.getCookie(url);
                    L.i("Fetch Cookie: " + cookieStr);
                    Message msg = mHandler.obtainMessage(30,"正在下载...");
                    mHandler.sendMessage(msg);
//                    downloadBySystem(url,null,null,cookieStr);
                    HttpUtil.get().download(
                            url,
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(),
                            url.substring(url.lastIndexOf("/")),
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

                return response;
            }

            @Override
            // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                L.i(url);
                int result = 0;
                // 判断url链接中是否含有某个字段，如果有就执行指定的跳转（不执行跳转url链接），如果没有就加载url链接
                if (url.startsWith("tel:") || url.startsWith("sms:") || url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.startsWith(UrlManager.appRemoteHomePageUrl)){
                    String username = SPUtils.get(activity,"username","").toString();
                    String token = SPUtils.get(activity,"token","").toString();
                    if(tv_title!=null&&StringHelper.isEmpty(String.valueOf(tv_title.getText()))){
                        tv_title.setText(webView.getTitle());
                    }
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("userName",username);
                        String txlStr =SPUtils.get(activity,"TXL","").toString();
                        txlStr = txlStr.replaceAll("\\\\","\\\\\\\\");
                        JSONObject txlJson = new JSONObject(txlStr);
                        jsonObject.put("contactBook",txlJson);
                        jsonObject.put("softVersion",DeviceUtil.getReleaseVersion(activity));
                        jsonObject.put("token",token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    view.loadUrl("javascript:window._NativeActivate_('"+jsonObject.toString()+"')");
                }

                if(url.startsWith("http://mail.ynwdi.com/coremail")&&!url.contains("&mode=download")){
                    view.loadUrl("javascript:console.log(document.getElementById('pageHeader').outerHTML='')");
                    view.loadUrl("javascript:console.log(document.getElementById('dvContainer').style.top='0px')");
                    view.loadUrl("javascript:console.log(document.getElementsByClassName('footer')[0].outerHTML='')");
                }

                super.onPageFinished(view, url);
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
            //For Android  >= 4.1
            public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
                uploadMessage = valueCallback;
                PermissionsUtils.getInstance().chekPermissions(activity, permissions, permissionsResult);
            }

            // For Android >= 5.0
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;

                PermissionsUtils.getInstance().chekPermissions(activity, permissions, permissionsResult);
                return true;
            }
        });
        webView.loadUrl(openUrl);
//        webView.loadUrl("file:///android_asset/js-call-native.html");

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

    //创建监听权限的接口对象
    PermissionsUtils.IPermissionsResult permissionsResult = new PermissionsUtils.IPermissionsResult() {
        @Override
        public void passPermissons() {
            take();
        }

        @Override
        public void forbitPermissons() {
            T.showShortError(activity.getApplicationContext(),"获取摄像头权限失败无法启动相机拍照功能",true);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //就多一个参数this
        PermissionsUtils.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private void take(){
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ynsy");
        // Create the storage directory if it does not exist
        if (! imageStorageDir.exists()){
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = Uri.fromFile(file);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);

        }
        Intent imgIntent= new Intent(Intent.ACTION_PICK);
        imgIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(imgIntent,"请选择");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        this.startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //主动取消
        if(resultCode == Activity.RESULT_CANCELED){
            if (null != uploadMessageAboveL) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
            if (null != uploadMessage) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            return;
        }
        if(requestCode==FILE_CHOOSER_RESULT_CODE)
        {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            }
            else if (uploadMessage != null) {
                if(result==null){
                    uploadMessage.onReceiveValue(imageUri);
                    uploadMessage = null;
                }else {
                    uploadMessage.onReceiveValue(result);
                    uploadMessage = null;
                }


            }
        }
    }

    @SuppressWarnings("null")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        //主动取消
        if(resultCode == Activity.RESULT_CANCELED){
            if (null != uploadMessageAboveL) {
                uploadMessageAboveL.onReceiveValue(null);
                uploadMessageAboveL = null;
            }
            if (null != uploadMessage) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            return;
        }
        //通过FileChooser选择器打开
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    results = new Uri[]{imageUri};
                } else {
                    String dataString = data.getDataString();
                    ClipData clipData = data.getClipData();

                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }

                    if (dataString != null)
                        results = new Uri[]{Uri.parse(dataString)};
                }
            }
            if(results!=null){
                uploadMessageAboveL.onReceiveValue(results);
                uploadMessageAboveL = null;
            }else{
                results = new Uri[]{imageUri};
                uploadMessageAboveL.onReceiveValue(results);
                uploadMessageAboveL = null;
            }
        }

        return;
    }

    //双击返回退出标识
    private static boolean isExit = false;
    /*
    * 双击返回退出
    * */
    @Override
    public void onBackPressed() {

        if(back){
            return;
        }

        if (null != uploadMessageAboveL) {
            uploadMessageAboveL.onReceiveValue(null);
            uploadMessageAboveL = null;
        }
        if (null != uploadMessage) {
            uploadMessage.onReceiveValue(null);
            uploadMessage = null;
        }

        String webU = webView.getUrl();
        L.i(webU);

        //论客返回时直接关闭webview  http://mail.ynwdi.com/coremail/xphone/main.jsp#module=
        L.i(String.valueOf(webView.copyBackForwardList().getSize()));
        if((webView.copyBackForwardList().getSize()==2||webView.copyBackForwardList().getSize()==3)
                &&webU.equals("http://mail.ynwdi.com/coremail/xphone/main.jsp#module=folder")
                ||webU.equals("http://mail.ynwdi.com/coremail/xphone/main.jsp#module=foldmain")){
            finish();
        }


        if(closeWebViews.length>5){
            super.onBackPressed();
        }else{
            for (String tempUrl : closeWebViews){
                if (webU.equals(tempUrl)){
                    exit();
                    return;
                }
            }
            if(webView.canGoBack()){
                webView.goBack();
            }else{
                finish();
            }

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
            T.showShortInfo(getApplicationContext(), "再按一次退出", true);
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
    public void openWebView(String obj) {
/*        Intent intent = new Intent(activity, CommonWebView.class);
        intent.putExtra("url", url);//设置参数
        activity.startActivity(intent);*/
        try {
            JSONObject paramJson = new JSONObject(obj.toString());
            String openUrl = paramJson.getString("openUrl");

            Intent intent = new Intent(activity, CommonWebView.class);
            intent.putExtra("url", openUrl);//设置参数

            if(openUrl.startsWith("http://mail.ynwdi.com/coremail")){
                HttpUtil.get().syncGet(openUrl);
            }

            if (paramJson.has("closeUrl")) {
                String closeWebView = paramJson.getString("closeUrl");
                String[] closeWebViews = closeWebView.split(" ");
                intent.putExtra("closeWebViews", closeWebViews);
            }

            if (paramJson.has("orientation")) {
                String orientation = paramJson.getString("orientation");
                intent.putExtra("orientation", orientation);
            }

            if (paramJson.has("nav")) {
                String nav = paramJson.getString("nav");
                intent.putExtra("nav", nav);
            }

            if (paramJson.has("title")) {
                String title = paramJson.getString("title");
                intent.putExtra("title", title);
            }
            activity.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //唤起手机浏览器
    @JavascriptInterface
    public void openActionView(String url) {
        L.i(url);
        //从其他浏览器打开
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        startActivity(Intent.createChooser(intent, "请选择浏览器"));
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
        downFile(url,zhbgToken);
    }

    //应用更新
    @JavascriptInterface
    public void updateApp() {
        checkVersion();
    }

    //back是否生效
    @JavascriptInterface
    public void back(String value) {
        if(value.equals("false"))
            CommonWebView.back=false;
        else
            CommonWebView.back=true;
    }

    //设置桌面角标
    @JavascriptInterface
    public void setBadge(String num) {//对应js中xxx.openWebView("")
        int dbCount = Integer.parseInt(num);
        BadgeNum.setBadgeNum(activity, dbCount);
    }

    //加密
    @JavascriptInterface
    public void encryption(String str) {//对应js中xxx.openWebView("")
        try {
            final String result = Decrypt.Encrypt(str.trim(), Decrypt.SECRETKEY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:encryption('"+result+"')");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //解密
    @JavascriptInterface
    public void decrypt(String str) {//对应js中xxx.openWebView("")
        try {
            final String result = Decrypt.Decrypts(str.trim(), Decrypt.SECRETKEY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:decrypt('"+result+"')");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void callShare(String msg) {
        if(StringHelper.isEmpty(msg)){
            Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.appdownload);
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
    private void downFile(String url,String token) {
        DownloadUtil.get().download(url,token, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), url.substring(url.lastIndexOf("/")),
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
                    T.showLongSuccess(activity,"下载完成",true);
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
                    String type = FileTypeHelper.getMIMEType(file);
                    intent.setDataAndType (uri, type);
                    startActivity(Intent.createChooser(intent, "打开方式"));

                    break;
                case 1:
                    T.showLongError(activity,"下载失败",true);
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
                    T.showLongSuccess(activity,"下载完成",true);
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
                    T.showShortSuccess(activity,"已是最新版本",true);
                    break;
                case 30:

                    T.showShortInfo(activity,msg.obj.toString(),true);
                    break;
            }
        }
    };

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
                        .addHeader("token",zhbgToken)
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
                        .addHeader("token",zhbgToken)
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
    private void updateApk(String url,String token) {
        DownloadUtil.get().download(url,token,Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), url.substring(url.lastIndexOf("/")),
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
            updateApk(UrlManager.appDownLoadUrl,zhbgToken);
        }
    };

}