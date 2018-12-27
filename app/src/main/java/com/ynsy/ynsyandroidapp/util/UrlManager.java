package com.ynsy.ynsyandroidapp.util;

public class UrlManager {
    private static String seeyonHost = "http://172.16.7.211";
    public static String seeyonToken =seeyonHost+"/seeyon/rest/token/admin/cjwsjy123";
    public static String seeyonHostAuthentication =seeyonHost+"/seeyon/rest/orgMember/effective/loginNameAndPassword/";

    private static String Host = "http://192.168.31.176:8080";
    public static String appRemoteLoginUrl = Host+"/yn/a/login";
    public static String appRemoteHomePageUrl = Host+"/cjwsjy-yt-app";
    public static String getUserInfoUrl = Host+"/yn/a/commonApi/getUserInfo";
    public static String userDeviceSaveOrUpdateUrl = Host+"/cjwsjy-yt/createUserDevices";
    public static String userDeviceDeleteUrl = Host+"/cjwsjy-yt/deleteUserDevices";
}
