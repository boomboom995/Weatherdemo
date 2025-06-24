package com.example.weatherforecast2.util;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.weatherforecast2.AppGlobals;
import java.net.URLEncoder;

public class WeatherApiHelper {
    private static final String KEY = "你的KEY";
    private static final String BASE_URL = "https://qa7qqqmxmm.re.qweatherapi.com";
    private static final OkHttpClient client = new OkHttpClient();

    // 城市搜索（模糊搜索）
    public static void searchCity(String keyword, Callback callback) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, "UTF-8");
            String url = BASE_URL + "/geo/v2/city/lookup?key=" + KEY + "&location=" + encodedKeyword;
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            callback.onFailure(null, new IOException("参数编码失败"));
        }
    }

    // 经纬度反查城市
    public static void geoCity(double lat, double lon, Callback callback) {
        String url = BASE_URL + "/geo/v2/city/lookup?key=" + KEY + "&location=" + lon + "," + lat;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    // 获取7天天气（替换为 itboy.net 公共API）
    public static void getWeather7d(String locationId, Callback callback) {
        String url = "http://t.weather.itboy.net/api/weather/city/" + locationId;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    // 获取实时天气（同样用 itboy.net 公共API，返回结构一致）
    public static void getWeatherNow(String locationId, Callback callback) {
        String url = "http://t.weather.itboy.net/api/weather/city/" + locationId;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
} 