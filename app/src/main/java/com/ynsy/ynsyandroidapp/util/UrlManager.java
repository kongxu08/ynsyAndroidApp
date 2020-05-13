package com.ynsy.ynsyandroidapp.util;

public class UrlManager {
    private static String seeyonHost = "http://116.54.19.198:8086";
    public static String seeyonToken =seeyonHost+"/seeyon/rest/token/admin/cjwsjy123";
    public static String seeyonHostAuthentication =seeyonHost+"/seeyon/rest/orgMember/effective/loginNameAndPassword/";
    //oa服务器对应72.16.7.210:8085
    public static String Host = "http://116.54.19.198:8111";
//    private static String Host = "http://116.54.19.198:8085";

    //正式app服务器对应172.16.7.210:8081
    private static String appHost = "http://116.54.19.198:8081";
    //李衍测试
//    private static String appHost = "http://116.54.19.198:8088";

    public static String appVersionUrl = Host+"/oa/getAppVersion?appSystem=android";
    public static String appDownLoadUrl = appHost+"/download/ynslsd.apk";

    public static String appTXLUrl = Host+"/oa/a/appApi/phoneBook";
//  public static String appTXLUrl = Host+"/oa/yn/a/appApi/phon eBook";

   //public static String appRemoteLoginUrl = Host+"/yn/a/login";
    public static String appRemoteHomePageUrl = appHost+"/";

//  public static String getUserInfoUrl = Host+"/oa/a/loginApi/getUserInfo";
    public static String getUserInfoUrl = Host+"/login";

    public static String userDeviceSaveOrUpdateUrl = Host+"/oa/createUserDevices";
    public static String userDeviceDeleteUrl = Host+"/oa/deleteUserDevices";
}
