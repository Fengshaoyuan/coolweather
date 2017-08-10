package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by lenovo on 2017/7/29.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("drsg")
    public Dressing dressing;

    @SerializedName("flu")
    public Flu flu;

    @SerializedName("sport")
    public Sport sport;

    @SerializedName("uv")
    public UltraViolet ultraViolet;


    /*舒适度指数*/
    public class Comfort{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
    /*洗车指数*/
    public class CarWash{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
    /*穿衣指数*/
    public class Dressing{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
    /*感冒指数*/
    public class Flu{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
    /*运动指数*/
    public class Sport{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
    /*紫外线指数*/
    public class UltraViolet{

        @SerializedName("brf")
        public String keyWord;

        @SerializedName("txt")
        public String info;

    }
}
