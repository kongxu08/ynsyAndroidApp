package com.ynsy.ynsyandroidapp.webview;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AudioRecoderUtils;
import com.ynsy.ynsyandroidapp.util.DeviceUtil;
import com.ynsy.ynsyandroidapp.util.DownloadUtil;
import com.ynsy.ynsyandroidapp.util.FileTypeHelper;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.SPUtils;
import com.ynsy.ynsyandroidapp.util.StringHelper;
import com.ynsy.ynsyandroidapp.util.T;
import com.ynsy.ynsyandroidapp.util.UrlManager;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import wendu.dsbridge.DWebView;

public class CommonDWebView extends AppCompatActivity {
    private String url;
    private String[] closeWebViews;
    private ImageView btn_back;
    private TextView tv_title;

    private DWebView dwebView;
    //进度条
    ProgressDialog progressDialog;
    //版本号
    private String versionNumber;
    //升级说明
    private String changeContent;

    View loadingView;

    private Activity activity;

    @Override
    public void onBackPressed() {
        String webU = dwebView.getUrl();
        if(closeWebViews!=null){
            for (String tempUrl : closeWebViews){
                if (webU.indexOf(tempUrl)!=-1){
                    super.onBackPressed();
                }
            }
        }
        if (webU.equals(url)){
            super.onBackPressed();
        }else {
            dwebView.goBack();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_dwebview);
        dwebView = findViewById(R.id.webview);

        activity=this;

        btn_back = findViewById(R.id.iv_back);
        tv_title = findViewById(R.id.tv_navtitle);

        //绑定返回点击事件
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        String openUrl = intent.getStringExtra("url");
        url = openUrl;
        //获取指定关闭webView的url
        closeWebViews = intent.getStringArrayExtra("closeWebViews");

        loadingView=LoadingUtil.creatWebLoadingView(activity);

        // set debug mode
        DWebView.setWebContentsDebuggingEnabled(true);
        dwebView.addJavascriptObject(new JsApi(this), null);
        dwebView.addJavascriptObject(new JsEchoApi(),"echo");

        dwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // 断网或者网络连接超时
                if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT) {
                    view.loadUrl("file://" + getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/cjwsjy/errorPage/no_network.html");
                    // 避免出现默认的错误界面
                    tv_title.setText(view.getTitle());
                }
            }

            @Override
            // 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。这个函数我们可以做很多操作，比如我们读取到某些特殊的URL，于是就可以不打开地址，取消这个操作，进行预先定义的其他操作，这对一个程序是非常必要的。
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
                String title = view.getTitle();
                if (!StringHelper.isEmpty(title)) {
                    tv_title.setText(title);
                }
            }
        });

        //网页加载进度条
        dwebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // TODO 自动生成的方法存根

                if(newProgress==100){
                    loadingView.setVisibility(View.GONE);
                }
                else{
                    loadingView.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    LoadingUtil.pg.setProgress(newProgress);//设置进度值
                }
            }
        });

        if(StringHelper.isEmpty((openUrl))){
            dwebView.loadUrl("file:///android_asset/js-call-native.html");
        }else{
            dwebView.loadUrl(openUrl);
        }

    }




    public void checkVer(){
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
                    L.i(body);
                    JSONObject rJson = new JSONObject(body);
                    versionNumber = rJson.getString("versionNumber");
                    changeContent = rJson.getString("changeContent");
                    UrlManager.appDownLoadUrl = rJson.getString("downloadurl");
                    String curVersion = DeviceUtil.getReleaseVersion(activity);
                    if (!versionNumber.equals(curVersion)) {
                        mHandler.sendEmptyMessage(22);
                    } else {
                        Message msg= mHandler.obtainMessage();
                        msg.obj="已是最新版本";
                        msg.what=200;
                        mHandler.sendMessage(msg);
                    }
                }else{
                    Message msg= mHandler.obtainMessage();
                    msg.obj=response.code();
                    msg.what=500;
                    mHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Message msg= mHandler.obtainMessage();
                msg.obj="版本更新服务器连接失败";
                msg.what=500;
                mHandler.sendMessage(msg);
                e.printStackTrace();
            }
        }
    };

    //下载
    Runnable downLoadRun = new Runnable() {
        @Override
        public void run() {
            DownloadUtil.get().download(UrlManager.appDownLoadUrl,SPUtils.get(activity, "token", "").toString(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), UrlManager.appDownLoadUrl.substring(UrlManager.appDownLoadUrl.lastIndexOf("/")),
                    new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(File file) {
                            Message msg = mHandler.obtainMessage();
                            if(file.getName().toLowerCase().endsWith("apk")){
                                msg.what = 24;
                            }else{
                                msg.what = 23;
                            }
                            //下载完成进行相关逻辑操作
                            msg.obj = file;
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
    };

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    T.showLongError(activity, "下载失败", true);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;
                case 11:
                    if (progressDialog != null) {
                        progressDialog.setProgress(msg.arg1);
                    }
                    break;
                case 23:
                    T.showLongSuccess(activity, "下载完成", true);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    File file = (File) msg.obj;
                    //下载完成后打开
                    Intent intent = new Intent("android.intent.action.VIEW");
                    Uri uri = null;
                    // 判断版本大于等于7.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri = FileProvider.getUriForFile(activity, "com.cjwsjy.lhkandroidapp.fileprovider", file);
                    } else {
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        uri = Uri.fromFile(file);
                    }
                    intent.addCategory("android.intent.category.DEFAULT");
                    String type = FileTypeHelper.getMIMEType(file);
                    intent.setDataAndType(uri, type);
                    startActivity(Intent.createChooser(intent, "打开方式"));

                    break;
                case 24:
                    T.showLongSuccess(activity, "下载完成", true);
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    File apkfile = (File) msg.obj;
                    // 通过Intent安装APK文件
                    Intent intent2 = new Intent(Intent.ACTION_VIEW);
                    Uri uri2 = null;

                    // 判断版本大于等于7.0
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent2.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        uri2 = FileProvider.getUriForFile(activity, "com.cjwsjy.lhkandroidapp.fileprovider", apkfile);
                    } else {
                        //intent.addCategory("android.intent.category.DEFAULT");
                        //设置intent的data和Type属性。
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        uri2 = Uri.fromFile(apkfile);
                    }
                    intent2.setDataAndType(uri2, "application/vnd.android.package-archive");
                    //跳转
                    startActivity(intent2);

                    break;
                case 200:
                    T.showShortSuccess(activity, msg.obj.toString(), true);
                    break;
                case 500:
                    T.showShortError(activity, msg.obj.toString(), true);
                    break;
            }
        }
    };
}
