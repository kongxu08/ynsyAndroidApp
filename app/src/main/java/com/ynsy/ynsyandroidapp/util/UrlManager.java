package com.ynsy.ynsyandroidapp.util;

public class UrlManager {
    private static String seeyonHost = "http://116.54.19.198:8086";
    public static String seeyonToken =seeyonHost+"/seeyon/rest/token/admin/cjwsjy123";
    public static String seeyonHostAuthentication =seeyonHost+"/seeyon/rest/orgMember/effective/loginNameAndPassword/";

//    private static String appHost = "http://172.16.7.209:8080";
//app应用服务器 原172.16.7.209:8080 对应172.16.7.210:8081
    private static String appHost = "http://116.54.19.198:8081";


   //public static String appRemoteLoginUrl = Host+"/yn/a/login";
    public static String appRemoteHomePageUrl = appHost+"/";
//    public static String getUserInfoUrl = Host+"/yn/a/commonApi/getUserInfo";
//    public static String userDeviceSaveOrUpdateUrl = Host+"/cjwsjy-yt/createUserDevices";
//    public static String userDeviceDeleteUrl = Host+"/cjwsjy-yt/deleteUserDevices";
}
