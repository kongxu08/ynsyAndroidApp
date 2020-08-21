package com.ynsy.ynsyandroidapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ynsy.ynsyandroidapp.R;
import com.ynsy.ynsyandroidapp.util.AudioRecoderUtils;
import com.ynsy.ynsyandroidapp.util.L;
import com.ynsy.ynsyandroidapp.util.SDCardUtils;
import com.ynsy.ynsyandroidapp.util.T;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AutioActivity extends AppCompatActivity {
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
    private View view ;
    private TextView recordingTime;
    private Button mButton;
    private Button cButton;
    private long ltime;
    private ImageView micImage;
    private AudioRecoderUtils mAudioRecoderUtils;
    View rl ;

    private MediaPlayer player;
    String path;
    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autio);
        checkPermission();
        rl = getWindow().getDecorView();
        mButton=findViewById(R.id.buttonPressToSpeak);
        cButton=findViewById(R.id.buttonBroadcast);
        path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/temp/sound.aac";
        ctx=getApplication().getApplicationContext();
        initVedio();
    }
    //录音功能 初始化
    private void initVedio(){
        mAudioRecoderUtils = new AudioRecoderUtils();
        view = View.inflate(this, R.layout.popup_window, null);
        //设置空白的背景色
        final PopupWindow mPop  = new PopupWindow(view);
        recordingTime=(TextView)view.findViewById(R.id.recording_time);
        micImage=(ImageView)view.findViewById(R.id.iv_pro);

        //录音回调
        mAudioRecoderUtils.setOnAudioStatusUpdateListener(new AudioRecoderUtils.OnAudioStatusUpdateListener() {

            //录音中....db为声音分贝，time为录音时长
            @Override
            public void onUpdate(double db, long time) {
                ltime=time;
                //根据分贝值来设置录音时话筒图标的上下波动
                micImage.getDrawable().setLevel((int) (3000 + 6000 * db / 100));
                recordingTime.setText(sdf.format(new Date(time)));
            }

            //录音结束，filePath为保存路径
            @Override
            public void onStop(String filePath) {
                if(ltime<1500){//判断，如果录音时间小于1.5秒，则删除文件提示，过短
                    File file = new File(filePath);
                    if(file.exists()){//判断文件是否存在，如果存在删除文件
                        file.delete();//删除文件
                        T.showShortError(AutioActivity.this, "录音时间过短", false);
                    }
                }else{
                    try {
                       T.showShortInfo(AutioActivity.this, "录音保存在：" + filePath, false);
                        recordingTime.setText("00:00");
                        ltime=0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //Button的touch监听
        mButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        L.i("MotionEvent.ACTION_DOWN");
                        mPop.setWidth(800);
                        mPop.setHeight(600);
                        mPop.showAtLocation(rl,Gravity.CENTER,0,0);
                        mAudioRecoderUtils.startRecord();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        L.i("MotionEvent.ACTION_MOVE");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        L.i("MotionEvent.ACTION_CANCEL");
                        mAudioRecoderUtils.stopRecord();        //结束录音（保存录音文件）
                        mPop.dismiss();
                        break;
                    case MotionEvent.ACTION_UP:
                        L.i("MotionEvent.ACTION_UP");
                        mAudioRecoderUtils.stopRecord();        //结束录音（保存录音文件）
                        mPop.dismiss();

                        break;
                }
                return true;
            }
        });

        //播放录音
        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!SDCardUtils.isExit(path)){
                    Toast.makeText(AutioActivity.this, "此故障信息暂无录音内容", Toast.LENGTH_SHORT).show();
                }else{
                    if(player==null){
                        player = mAudioRecoderUtils.playRecord(path,ctx);//播放
                        cButton.setText("暂停");
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {//监听是否播放完毕
                                cButton.setText("播放");
                                player.release();//释放资源
                                player=null;
                            }
                        });
                    }else if(player.isPlaying()){
                        player.stop();
                        player.release();//释放资源
                        player=null;
                        cButton.setText("播放");
                    }
                }
            }
        });
    }

    /**
     * 权限申请
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == 200) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 200) {
            checkPermission();
        }
    }
}
