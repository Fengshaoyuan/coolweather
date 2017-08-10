package com.coolweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by lenovo on 2017/7/29.
 */

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
//    title
    private Button navButton;
    private TextView titleCity;
    private TextView titleUpdateTime;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;
//    必应每日一图
    private ImageView bingPicImg;

    private ScrollView weatherLayout;
//    now
    private TextView degreeText;
    private TextView weatherInfoText;
    private ImageView weatherImg;
    private TextView flText;
    private TextView humText;
    private TextView pcpnText;
    private TextView presText;
    private TextView visText;
    private TextView degText;
    private TextView dirText;
    private TextView scText;
    private TextView spdText;

    private LinearLayout forecastLayout;
//    AQI 空气质量指数
    private TextView aqiText;
    private TextView pm25Text;
    private TextView coText;
    private TextView no2Text;
    private TextView o3Text;
    private TextView pm10Text;
    private TextView qltyText;
    private TextView so2Text;
//    Suggestion
    private TextView comfortText;
    private TextView carWashText;
    private TextView dressingText;
    private TextView fluText;
    private TextView sportText;
    private TextView ultraVioleText;
//    分享复选框
    private CheckBox cbComfort;
    private CheckBox cbCarWash;
    private CheckBox cbDressing;
    private CheckBox cbFlu;
    private CheckBox cbSport;
    private CheckBox cbUltraViole;
//    处理check事件
    private View checkView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//        判断版本
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
//            设置UI显示
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            设置状态栏为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
//        初始化控件
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
//        title
        navButton = (Button)findViewById(R.id.nav_button);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);

        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        必应每日一图
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);

        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);

//        now
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        weatherImg = (ImageView)findViewById(R.id.weather_img);
        flText = (TextView) findViewById(R.id.fl_text);
        humText = (TextView) findViewById(R.id.hum_text);
        pcpnText = (TextView) findViewById(R.id.pcpn_text);
        presText = (TextView) findViewById(R.id.pres_text);
        visText = (TextView) findViewById(R.id.vis_text);
        degText = (TextView) findViewById(R.id.deg_text);
        dirText = (TextView) findViewById(R.id.dir_text);
        scText = (TextView) findViewById(R.id.sc_text);
        spdText = (TextView) findViewById(R.id.spd_text);

        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
//        aqi
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        coText = (TextView)findViewById(R.id.co_text);
        no2Text = (TextView)findViewById(R.id.no2_text);
        o3Text = (TextView)findViewById(R.id.o3_text);
        pm10Text = (TextView)findViewById(R.id.pm10_text);
        qltyText = (TextView)findViewById(R.id.qlty_text);
        so2Text = (TextView)findViewById(R.id.so2_text);
//        sugesstion
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        dressingText = (TextView)findViewById(R.id.dressing_text);
        fluText = (TextView)findViewById(R.id.flu_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        ultraVioleText = (TextView)findViewById(R.id.ultraViole_text);

//        复选框
        cbComfort = (CheckBox)findViewById(R.id.cb_comfort);
        cbCarWash = (CheckBox)findViewById(R.id.cb_car_wash);
        cbDressing = (CheckBox)findViewById(R.id.cb_dressing);
        cbFlu = (CheckBox)findViewById(R.id.cb_flu);
        cbSport = (CheckBox)findViewById(R.id.cb_sport);
        cbUltraViole = (CheckBox)findViewById(R.id.cb_ultraViole);

        checkView =  findViewById(R.id.CheckBoxOnClick);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String bingPic = prefs.getString("bing_pic", null);
        String weatherString = prefs.getString("weather", null);

        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        if(weatherString != null){
            //有缓存时直接解析数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            /*weather_id 首字母不应该大写
            * 在ChooseAreaFragment.java 中写入Intent传过来*/
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.
                OnRefreshListener(){
            @Override
            public void onRefresh(){
                requestWeather(mWeatherId);
            }
        });
        checkView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                if(cbComfort.getVisibility() == View.GONE){
                    cbComfort.setVisibility(View.VISIBLE);
                    cbCarWash.setVisibility(View.VISIBLE);
                    cbDressing.setVisibility(View.VISIBLE);
                    cbFlu.setVisibility(View.VISIBLE);
                    cbSport.setVisibility(View.VISIBLE);
                    cbUltraViole.setVisibility(View.VISIBLE);
                }else if(cbComfort.getVisibility() == View.VISIBLE){
                    cbComfort.setVisibility(View.GONE);
                    cbCarWash.setVisibility(View.GONE);
                    cbDressing.setVisibility(View.GONE);
                    cbFlu.setVisibility(View.GONE);
                    cbSport.setVisibility(View.GONE);
                    cbUltraViole.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }



    /**
     * 根据天气id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(final String weatherId) {
        /*http://guolin.tech/api/weather?cityid=
        bc0418b57b2d4918819d3974ac1285d9

        https://free-api.heweather.com/v5/weather?city=yourcity&key=yourkey
        71c6605ca7d9430a8012a94672060b75*/
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=71c6605ca7d9430a8012a94672060b75";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

        });
        loadBingPic();
    }
    /*
    * 加载必应每日一图
    * */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     * @param weather
     */
    String cityName;
    String updateTime;
    String comfort;
    String carWash;
    String dressing;
    String flu;
    String sport;
    String ultraViole;

   private void showWeatherInfo(Weather weather) {
        cityName = weather.basic.cityName;
        updateTime = weather.basic.update.updateTime.split(" ")[1];
//        now
        String temperatureNow = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        String weatherCode = weather.now.more.img_code;
        String feelNow = weather.now.feel;
        String humidityNow = weather.now.humidity;
        String precipitationNow = weather.now.precipitation;
        String pressureNow = weather.now.pressure;
        String visibilityNow = weather.now.visibility;
        String degreeNow = weather.now.wind.degree;
        String directionNow = weather.now.wind.direction;
        String scaleNow = weather.now.wind.scale;
        String speedNow = weather.now.wind.speed;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
//       now
        degreeText.setText(temperatureNow);
        weatherInfoText.setText(weatherInfo);
        setWeatherImg(weatherCode);
        flText.setText("体感温度：" + feelNow + "℃");
        humText.setText("相对湿度：" + humidityNow + "%");
        pcpnText.setText("降水量:" + precipitationNow + "mm");
        presText.setText("气压：" + pressureNow + "hPa");
        visText.setText("能见度：" + visibilityNow + "km");
        degText.setText("风向：" + degreeNow + "°");
        dirText.setText("风向：" + directionNow);
        scText.setText("风力：" + scaleNow);
        spdText.setText("风速：" + speedNow + "kmph");

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            coText.setText(weather.aqi.city.co);
            no2Text.setText(weather.aqi.city.no2);
            o3Text.setText(weather.aqi.city.o3);
            pm10Text.setText(weather.aqi.city.pm10);
            qltyText.setText(weather.aqi.city.qlty);
            so2Text.setText(weather.aqi.city.so2);
        }
        comfort = "舒适度：" + weather.suggestion.comfort.keyWord + "\n" + "简介：" + weather.suggestion.comfort.info;
        carWash = "洗车指数：" + weather.suggestion.carWash.keyWord + "\n" + "简介：" + weather.suggestion.carWash.info;
        dressing = "穿衣指数：" + weather.suggestion.dressing.keyWord + "\n" + "简介：" + weather.suggestion.dressing.info;
        flu = "感冒指数：" + weather.suggestion.flu.keyWord + "\n" + "简介：" + weather.suggestion.flu.info;
        sport = "运动建议：" + weather.suggestion.sport.keyWord + "\n" + "简介：" + weather.suggestion.sport.info;
        ultraViole = "紫外线指数：" + weather.suggestion.ultraViolet.keyWord + "\n" + "简介：" + weather.suggestion.ultraViolet.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
        dressingText.setText(dressing);
        fluText.setText(flu);
        sportText.setText(sport);
        ultraVioleText.setText(ultraViole);

        weatherLayout.setVisibility(View.VISIBLE);
        //激活AutoUpdateService服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    public Bitmap getWeatherImg(String path){
        Bitmap bitmap=null;
        try{
            URL url=new URL(path);
            URLConnection connection=url.openConnection();
            connection.connect();
            InputStream inputStream=connection.getInputStream();
            bitmap= BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  bitmap;
    }
    public void setWeatherImg(final String weatherCode){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap=getWeatherImg("https://cdn.heweather.com/cond_icon/" + weatherCode + ".png");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                weatherImg.post(new Runnable() {
                    @Override
                    public void run() {
                        weatherImg.setImageBitmap(bitmap);
                    }
                });
            }
        }).start();
    }
    /*
    *分享生活建议
    * */
    public void onShare(View view) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String mySuggestionToElse = cityName + updateTime + ":\n" ;
        /*mySuggestionToElse = cityName + updateTime + ":\n" + comfort + "\n" + carWash + "\n"
                + dressing + "\n" + flu + "\n" + sport + "\n" + ultraViole;*/
        if(cbComfort.isChecked()){
            mySuggestionToElse += comfort;
        }
        if(cbCarWash.isChecked()){
            mySuggestionToElse += "\n" + carWash;
        }
        if(cbDressing.isChecked()){
            mySuggestionToElse += "\n" + dressing;
        }
        if(cbFlu.isChecked()){
            mySuggestionToElse += "\n" + flu;
        }
        if(cbSport.isChecked()){
            mySuggestionToElse += "\n" + sport;
        }
        if(cbUltraViole.isChecked()){
            mySuggestionToElse += "\n" + ultraViole;
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, mySuggestionToElse);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
}
