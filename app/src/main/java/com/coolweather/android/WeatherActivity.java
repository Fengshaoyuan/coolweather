package com.coolweather.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.DailyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

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

    //    Alarms
    private TextView alarmsTitleText;
    private TextView alarmsElseText;

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
    //    处理灾害预警显示
    private View alarmsView;

    //实例
    private static WeatherActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        判断版本
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
//            设置UI显示
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            设置状态栏为透明
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
//        初始化控件
        drawerLayout = findViewById(R.id.drawer_layout);
//        title
        navButton = findViewById(R.id.nav_button);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
//        必应每日一图
        bingPicImg = findViewById(R.id.bing_pic_img);

        weatherLayout = findViewById(R.id.weather_layout);

//        now
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        weatherImg = findViewById(R.id.weather_img);
        flText = findViewById(R.id.fl_text);
        humText = findViewById(R.id.hum_text);
        pcpnText = findViewById(R.id.pcpn_text);
        presText = findViewById(R.id.pres_text);
        visText = findViewById(R.id.vis_text);
        degText = findViewById(R.id.deg_text);
        dirText = findViewById(R.id.dir_text);
        scText = findViewById(R.id.sc_text);
        spdText = findViewById(R.id.spd_text);

        forecastLayout = findViewById(R.id.forecast_layout);

//        alarms
        alarmsTitleText = findViewById(R.id.alarms_title_text);
        alarmsElseText = findViewById(R.id.alarms_else_text);

//        aqi
        aqiText = findViewById(R.id.aqi_text);
        pm25Text = findViewById(R.id.pm25_text);
        coText = findViewById(R.id.co_text);
        no2Text = findViewById(R.id.no2_text);
        o3Text = findViewById(R.id.o3_text);
        pm10Text = findViewById(R.id.pm10_text);
        qltyText = findViewById(R.id.qlty_text);
        so2Text = findViewById(R.id.so2_text);
//        suggestion
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        dressingText = findViewById(R.id.dressing_text);
        fluText = findViewById(R.id.flu_text);
        sportText = findViewById(R.id.sport_text);
        ultraVioleText = findViewById(R.id.ultraViole_text);

//        复选框
        cbComfort = findViewById(R.id.cb_comfort);
        cbCarWash = findViewById(R.id.cb_car_wash);
        cbDressing = findViewById(R.id.cb_dressing);
        cbFlu = findViewById(R.id.cb_flu);
        cbSport = findViewById(R.id.cb_sport);
        cbUltraViole = findViewById(R.id.cb_ultraViole);

//        控制是否显示
        checkView = findViewById(R.id.CheckBoxOnClick);
        alarmsView = findViewById(R.id.alarms_layout);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String bingPic = prefs.getString("bing_pic", null);
        String weatherString = prefs.getString("weather", null);

        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        if (weatherString != null) {
            //有缓存时直接解析数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            assert weather != null;
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            /*weather_id 首字母不应该大写
             * 在ChooseAreaFragment.java 中写入Intent传过来*/
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        navButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        swipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));
        checkView.setOnLongClickListener(v -> {
            if (cbComfort.getVisibility() == View.GONE) {
                cbComfort.setVisibility(View.VISIBLE);
                cbCarWash.setVisibility(View.VISIBLE);
                cbDressing.setVisibility(View.VISIBLE);
                cbFlu.setVisibility(View.VISIBLE);
                cbSport.setVisibility(View.VISIBLE);
                cbUltraViole.setVisibility(View.VISIBLE);
            } else if (cbComfort.getVisibility() == View.VISIBLE) {
                cbComfort.setVisibility(View.GONE);
                cbCarWash.setVisibility(View.GONE);
                cbDressing.setVisibility(View.GONE);
                cbFlu.setVisibility(View.GONE);
                cbSport.setVisibility(View.GONE);
                cbUltraViole.setVisibility(View.GONE);
            }
            return true;
        });
    }


    /**
     * 根据天气id请求城市天气信息
     *
     * @param weatherId 天气id
     */
    public void requestWeather(final String weatherId) {
        /*http://guolin.tech/api/weather?cityid=
        bc0418b57b2d4918819d3974ac1285d9

        https://free-api.heweather.com/v5/weather?city=yourcity&key=yourkey
        71c6605ca7d9430a8012a94672060b75*/
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=71c6605ca7d9430a8012a94672060b75";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {

            @SuppressWarnings("NullableProblems")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = Objects.requireNonNull(response.body()).string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefresh.setRefreshing(false);
                });
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败",
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
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
            @SuppressWarnings("NullableProblems")
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @SuppressWarnings("NullableProblems")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = Objects.requireNonNull(response.body()).string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(() -> Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg));
            }
        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     *
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
    String alarmsTitle;
    String alarmsInfo;

    @SuppressLint("SetTextI18n")
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
        String degreeNow = weather.now.wind_degree;
        String directionNow = weather.now.wind_direction;
        String scaleNow = weather.now.wind_scale;
        String speedNow = weather.now.wind_speed;

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
        for (DailyForecast dailyForecast : weather.dailyForecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(dailyForecast.date);
            infoText.setText(dailyForecast.more.info);
            maxText.setText(dailyForecast.temperature.max);
            minText.setText(dailyForecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.alarms != null) {
            alarmsView.setVisibility(View.VISIBLE);
            alarmsTitle = weather.alarms.title;
            alarmsTitleText.setText(alarmsTitle);
            alarmsInfo = "预警等级：" + weather.alarms.level + "\n预警状态:" + weather.alarms.stat + "\n信息详情：" + weather.alarms.txt + "\n天气类型：" + weather.alarms.type;
            alarmsElseText.setText(alarmsInfo);
        } else {
            alarmsView.setVisibility(View.GONE);
        }
        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
//            coText.setText(weather.aqi.city.co);
//            no2Text.setText(weather.aqi.city.no2);
//            o3Text.setText(weather.aqi.city.o3);
//            pm10Text.setText(weather.aqi.city.pm10);
            qltyText.setText(weather.aqi.city.qlty);
//            so2Text.setText(weather.aqi.city.so2);
        }
        comfort = "舒适度：" + weather.suggestion.comfort.keyWord + "\n" + "简介：" + weather.suggestion.comfort.info;
        carWash = "洗车指数：" + weather.suggestion.carWash.keyWord + "\n" + "简介：" + weather.suggestion.carWash.info;
//        dressing = "穿衣指数：" + weather.suggestion.dressing.keyWord + "\n" + "简介：" + weather.suggestion.dressing.info;
//        flu = "感冒指数：" + weather.suggestion.flu.keyWord + "\n" + "简介：" + weather.suggestion.flu.info;
        sport = "运动建议：" + weather.suggestion.sport.keyWord + "\n" + "简介：" + weather.suggestion.sport.info;
//        ultraViole = "紫外线指数：" + weather.suggestion.ultraViolet.keyWord + "\n" + "简介：" + weather.suggestion.ultraViolet.info;

        comfortText.setText(comfort);
        carWashText.setText(carWash);
//        dressingText.setText(dressing);
//        fluText.setText(flu);
        sportText.setText(sport);
//        ultraVioleText.setText(ultraViole);

        weatherLayout.setVisibility(View.VISIBLE);
        //激活AutoUpdateService服务
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    public Bitmap getWeatherImg(String path) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(path);
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void setWeatherImg(final String weatherCode) {
        new Thread(() -> {
            final Bitmap bitmap = getWeatherImg("https://cdn.heweather.com/cond_icon/" + weatherCode + ".png");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            weatherImg.post(() -> weatherImg.setImageBitmap(bitmap));
        }).start();
    }

    /*
     * 分享灾害预警
     * */
    public void onAlarmsShare(View view) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String myAlarmsToElse = cityName + "：" + updateTime + "\n" + alarmsTitle + "\n" + alarmsInfo;
        shareIntent.putExtra(Intent.EXTRA_TEXT, myAlarmsToElse);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }

    /*
     *分享生活建议
     * */
    public void onSuggestionShare(View view) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String mySuggestionToElse = cityName + "：" + updateTime + ":\n";
        /*mySuggestionToElse = cityName + updateTime + ":\n" + comfort + "\n" + carWash + "\n"
                + dressing + "\n" + flu + "\n" + sport + "\n" + ultraViole;*/
        if (cbComfort.isChecked()) {
            mySuggestionToElse += comfort;
        }
        if (cbCarWash.isChecked()) {
            mySuggestionToElse += "\n" + carWash;
        }
        if (cbDressing.isChecked()) {
            mySuggestionToElse += "\n" + dressing;
        }
        if (cbFlu.isChecked()) {
            mySuggestionToElse += "\n" + flu;
        }
        if (cbSport.isChecked()) {
            mySuggestionToElse += "\n" + sport;
        }
        if (cbUltraViole.isChecked()) {
            mySuggestionToElse += "\n" + ultraViole;
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, mySuggestionToElse);
        shareIntent.setType("text/plain");

        //设置分享列表的标题，并且每次都显示分享列表
        startActivity(Intent.createChooser(shareIntent, "分享到"));
    }
}
