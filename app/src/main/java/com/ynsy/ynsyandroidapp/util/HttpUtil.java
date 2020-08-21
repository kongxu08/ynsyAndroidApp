package com.ynsy.ynsyandroidapp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    private static HttpUtil httpUntil;
    private static OkHttpClient okHttpClient;
    //cookie存储
    private static ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();
    public static HttpUtil get() {

        if (httpUntil == null) {
            httpUntil = new HttpUtil();
            okHttpClient = new OkHttpClient.Builder()
                    .cookieJar(new CookieJar()
                    {//这里可以做cookie传递，保存等操作
                        @Override
                        public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
                        {//可以做保存cookies操作
                            cookieStore.put(url.host(), cookies);
                        }

                        @Override
                        public List<Cookie> loadForRequest(HttpUrl url)
                        {//加载新的cookies
                            List<Cookie> cookies = cookieStore.get(url.host());
                            return cookies != null ? cookies : new ArrayList<Cookie>();
                        }
                    })
                    .build();
        }

        return httpUntil;

    }

    public void syncGet(final String url) {

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Call call = okHttpClient.newCall(request);
                try
                {
                    Response response = call.execute();
                    sycCallback(response);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sycCallback(Response response)
    {
        L.e("Code: " + response.code());
        L.e("Message: " + response.message());

        if (response.isSuccessful())
        {
            try
            {
                L.e("Body: " + response.body().string());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                L.e("ERROR: " + "response'body is error");
            }
        }
        else
        {
            L.e("ERROR: " + "okHttp is request error");
        }
        L.e("");
    }

    /**
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param destFileName 下载文件名称
     * @param listener     下载监听
     */
    public void download(final String url, final String destFileDir, final String destFileName, final DownloadUtil.OnDownloadListener listener) {

        Request request = new Request.Builder()
                .url(url)
                .build();

        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败监听回调
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                //储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                try {
                    is = response.body().byteStream();
                    String disposition = response.header("Content-disposition").substring(response.header("Content-disposition").indexOf("\"")+1,response.header("Content-disposition").lastIndexOf("\""));
                    disposition = new String(disposition.getBytes(),"GB2312");
                    File file = new File(dir, disposition);
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    //下载完成
                    listener.onDownloadSuccess(file);
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                } finally {
                    try {
                        if (is != null) {

                            is.close();

                        }

                        if (fos != null) {

                            fos.close();

                        }

                    } catch (IOException e) {


                    }


                }


            }

        });

    }

}
