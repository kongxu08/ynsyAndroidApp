package com.cjwsjy.lhkandroidapp.util;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class BadgeNum {
    public static void setBadgeNum(Activity activity, int num){
        try{
            Bundle bunlde =new Bundle();
            bunlde.putString("package", "com.ynsy.ynsyandroidapp");
            bunlde.putString("class", "AppStart");
            bunlde.putInt("badgenumber",num);
            activity.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, bunlde);

        }catch(Exception e){
            Log.e("BadgeNum","该设备不支持桌面小红点");
        }

    }
}
