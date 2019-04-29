package com.cjwsjy.lhkandroidapp.util;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cjwsjy.lhkandroidapp.R;


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

    public static ProgressBar pg;
    public static View creatWebLoadingView(Activity activity) {
        // activity根部的ViewGroup，其实是一个FrameLayout
        FrameLayout rootContainer = (FrameLayout) activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.loading_web_process_dialog_color, null);
        pg=view.findViewById(R.id.loading_process_dialog_progressBar);
        // 将菊花添加到FrameLayout中
        rootContainer.addView(view);
        return view;
    }
}
