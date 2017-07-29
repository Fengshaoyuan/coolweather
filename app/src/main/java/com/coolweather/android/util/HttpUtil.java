package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by lenovo on 2017/7/29.
 */

/**
 * 用OKHttp封装，与服务器进行交互
 * 只需要调用sendOKHttpRequest()方法
 */
public class HttpUtil {
    public static  void  sedOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
