package com.example.weatherforecast2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforecast2.Activity.DiaryActivity;
import com.example.weatherforecast2.model.Forecast;
import com.example.weatherforecast2.model.Weather;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherAppDebug";

    // UI控件
    private TextView tvCurrentTemp, tvWeatherDetail, tvHumidityAir, tvLocation;
    private TextView tvDate1, tvWeather1, tvTempRange1;
    private TextView tvDate2, tvWeather2, tvTempRange2;
    private TextView tvDate3, tvWeather3, tvTempRange3;
    private ImageView ivWeatherIcon1, ivWeatherIcon2, ivWeatherIcon3;

    // 要查询的城市代码，例如北京是 "101010100"
    private final String cityCode = "101010100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        Button btnOpenDiary = findViewById(R.id.bth_diary);
        btnOpenDiary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DiaryActivity.class);
            startActivity(intent);
        });

        fetchWeather();
    }

    private void initViews() {
        tvCurrentTemp = findViewById(R.id.tvCurrentTemp);
        tvWeatherDetail = findViewById(R.id.tvWeatherDetail);
        tvHumidityAir = findViewById(R.id.tvHumidityAir);
        tvLocation = findViewById(R.id.tvLocation);
        tvDate1 = findViewById(R.id.tvDate1);
        tvWeather1 = findViewById(R.id.tvWeather1);
        tvTempRange1 = findViewById(R.id.tvTempRange1);
        ivWeatherIcon1 = findViewById(R.id.ivWeatherIcon1);
        tvDate2 = findViewById(R.id.tvDate2);
        tvWeather2 = findViewById(R.id.tvWeather2);
        tvTempRange2 = findViewById(R.id.tvTempRange2);
        ivWeatherIcon2 = findViewById(R.id.ivWeatherIcon2);
        tvDate3 = findViewById(R.id.tvDate3);
        tvWeather3 = findViewById(R.id.tvWeather3);
        tvTempRange3 = findViewById(R.id.tvTempRange3);
        ivWeatherIcon3 = findViewById(R.id.ivWeatherIcon3);
    }

    private void fetchWeather() {
        Log.d(TAG, "开始获取天气数据...");
        OkHttpClient client = new OkHttpClient();
        // 使用新的API地址
        String url = "http://t.weather.itboy.net/api/weather/city/" + cityCode;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "天气获取失败: ", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "天气数据获取失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    Log.d(TAG, "天气API成功返回: " + responseBody);
                    Gson gson = new Gson();
                    final Weather weather = gson.fromJson(responseBody, Weather.class);
                    runOnUiThread(() -> {
                        if (weather != null && weather.getStatus().equals("200")) {
                            Log.d(TAG, "天气数据解析成功，准备更新UI");
                            updateUI(weather);
                        } else {
                            Log.e(TAG, "天气API返回错误: " + (weather != null ? weather.getMessage() : "Response is null"));
                            Toast.makeText(MainActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "天气请求不成功: " + response.code());
                }
            }
        });
    }

    private void updateUI(Weather weather) {
        Log.d(TAG, "正在更新UI...");

        // 更新地点
        tvLocation.setText(weather.getCityInfo().getCity());

        // 更新实时天气
        if (weather.getData() != null) {
            tvCurrentTemp.setText(weather.getData().getWendu() + "°");

            List<Forecast> forecasts = weather.getData().getForecast();
            if (forecasts != null && !forecasts.isEmpty()) {
                Forecast todayForecast = forecasts.get(0);
                tvWeatherDetail.setText(
                        todayForecast.getType() + " " +
                                formatTemperature(todayForecast.getHigh()) + " / " +
                                formatTemperature(todayForecast.getLow())
                );
            }
            tvHumidityAir.setText("提示: " + weather.getData().getGanmao());
        }

        // 更新天气预报
        if (weather.getData() != null && weather.getData().getForecast() != null && weather.getData().getForecast().size() >= 3) {
            List<Forecast> dailyForecasts = weather.getData().getForecast();

            // 今天
            Forecast today = dailyForecasts.get(0);
            tvDate1.setText("今天");
            tvWeather1.setText(today.getType());
            tvTempRange1.setText(formatTemperature(today.getLow()) + " ~ " + formatTemperature(today.getHigh()));
            ivWeatherIcon1.setImageResource(getWeatherIconResource(today.getType()));

            // 明天
            Forecast tomorrow = dailyForecasts.get(1);
            tvDate2.setText("明天");
            tvWeather2.setText(tomorrow.getType());
            tvTempRange2.setText(formatTemperature(tomorrow.getLow()) + " ~ " + formatTemperature(tomorrow.getHigh()));
            ivWeatherIcon2.setImageResource(getWeatherIconResource(tomorrow.getType()));

            // 后天（显示星期几）
            Forecast afterTomorrow = dailyForecasts.get(2);
            tvDate3.setText(afterTomorrow.getWeek());
            tvWeather3.setText(afterTomorrow.getType());
            tvTempRange3.setText(formatTemperature(afterTomorrow.getLow()) + " ~ " + formatTemperature(afterTomorrow.getHigh()));
            ivWeatherIcon3.setImageResource(getWeatherIconResource(afterTomorrow.getType()));

            Log.d(TAG, "UI更新完毕！");
        } else {
            Log.e(TAG, "天气预报数据不足3天！");
        }
    }

    private String formatTemperature(String temp) {
        // 从 "高温 25.0℃" 或 "低温 7.0℃" 中提取 "25°" 或 "7°"
        if (temp == null || temp.isEmpty()) return "";
        try {
            String[] parts = temp.split(" ");
            if (parts.length > 1) {
                float tempValue = Float.parseFloat(parts[1].replace("℃", ""));
                return (int) tempValue + "°";
            }
        } catch (Exception e) {
            Log.e(TAG, "格式化温度失败: " + temp, e);
        }
        return temp;
    }

    private String extractDayOfWeek(String date) {
        // 从 "09日星期一" 中提取 "星期一"
        if(date != null && date.contains("星期")) {
            return date.substring(date.indexOf("星期"));
        }
        return date;
    }

    private int getWeatherIconResource(String weatherType) {
        if (weatherType == null) return R.drawable.ic_weather_sunny;
        if (weatherType.contains("晴")) {
            return R.drawable.ic_weather_sunny;
        } else if (weatherType.contains("多云") || weatherType.contains("阴")) {
            return R.drawable.ic_weather_cloudy;
        } else if (weatherType.contains("雨")) {
            return R.drawable.ic_weather_rainy;
        } else if (weatherType.contains("雪")) {
            return R.drawable.ic_weather_snowy;
        }
        return R.drawable.ic_weather_sunny; // 默认图标
    }
}