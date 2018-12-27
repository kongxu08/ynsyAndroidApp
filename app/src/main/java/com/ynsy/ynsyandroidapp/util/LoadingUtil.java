package com.ynsy.ynsyandroidapp.util;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ynsy.ynsyandroidapp.R;

public class LoadingUtil {
    public static View creatLoadingView(Activity activity) {
        // activity根部的ViewGroup，其实是一个FrameLayout
        FrameLayout rootContainer = (FrameLayout) activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.loading_process_dialog_color, null);
        // 将菊花添加到FrameLayout中
        rootContainer.addView(view);
        return view;
    }
    public static View creatLoadingView(Activity activity,String msg) {
        // activity根部的ViewGroup，其实是一个FrameLayout
        FrameLayout rootContainer = (FrameLayout) activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.loading_process_dialog_color, null);
        TextView tv_msg=view.findViewById(R.id.tv_msg);
        tv_msg.setText(msg);
        // 将菊花添加到FrameLayout中
        rootContainer.addView(view);
        return view;
    }
}
