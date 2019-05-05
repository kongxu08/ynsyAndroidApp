package com.ynsy.ynsyandroidapp.webview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.LoadingUtil;
import com.ynsy.ynsyandroidapp.util.StringHelper;

import wendu.dsbridge.DWebView;

public class CommonDWebView extends AppCompatActivity {
    private String url;
    private String[] closeWebViews;
    private ImageView btn_back;
    private TextView tv_title;

    private DWebView dwebView;

    View loadingView;

    private Activity activity;

    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;

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
        dwebView.addJavascriptObject(new JsApi(activity), null);
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
}
