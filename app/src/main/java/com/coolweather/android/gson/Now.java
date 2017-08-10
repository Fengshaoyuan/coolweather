package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 实况天气
 * Created by lenovo on 2017/7/29.
 */

public class Now {
//    温度
    @SerializedName("tmp")
    public String temperature;

    //    天气状况
    @SerializedName("cond")
    public More more;

    public class More{
        //        天气状况代码
        @SerializedName("code")
        public String img_code;
        //        天气状况描述
        @SerializedName("txt")
        public String info;

    }

//    体感温度
    @SerializedName("fl")
    public String feel;
//    相对湿度（%）
    @SerializedName("hum")
    public String humidity;
//    降水量（mm）
    @SerializedName("pcpn")
    public String precipitation;
//    气压
    @SerializedName("pres")
    public String pressure;
//    能见度（km）
    @SerializedName("vis")
    public String visibility;

//    风力风向
    @SerializedName("wind")
    public Wind wind;

    public class Wind{
//        风向（360度）
        @SerializedName("deg")
        public String degree;
//        风向
        @SerializedName("dir")
        public String direction;
//        风力
        @SerializedName("sc")
        public String scale;
//        风速（kmph）
        @SerializedName("spd")
        public String speed;
    }

}