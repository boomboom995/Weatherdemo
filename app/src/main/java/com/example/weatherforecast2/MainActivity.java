package com.example.weatherforecast2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast2.Activity.DiaryActivity;
import com.example.weatherforecast2.model.Forecast;
import com.example.weatherforecast2.model.Weather;
import com.google.gson.Gson;
import com.example.weatherforecast2.util.WeatherApiHelper;
import com.example.weatherforecast2.util.LocationHelper;
import com.example.weatherforecast2.Adapter.WeatherForecastAdapter;
import com.example.weatherforecast2.Adapter.WeatherHourlyAdapter;
import com.example.weatherforecast2.Adapter.SeniverseHourlyAdapter;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WeatherAppDebug";

    // UI控件
    private TextView tvCurrentTemp, tvWeatherDetail, tvHumidityAir, tvLocation;
    private TextView tvDate1, tvWeather1, tvTempRange1;
    private TextView tvDate2, tvWeather2, tvTempRange2;
    private TextView tvDate3, tvWeather3, tvTempRange3;
    private ImageView ivWeatherIcon1, ivWeatherIcon2, ivWeatherIcon3;
    private RecyclerView rvForecast;
    private WeatherForecastAdapter forecastAdapter;
    private RecyclerView rvHourly;
    private WeatherHourlyAdapter hourlyAdapter;

    // 要查询的城市代码，例如北京是 "101010100"
    private final String cityCode = "101010100";

    private final String PREFS_NAME = "weather";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String cityId = sp.getString("cityId", "101070101"); // 只定义一次

        initViews();

        // 启动时优先用本地保存的城市
        String cityName = sp.getString("cityName", "沈阳");
        tvLocation.setText(cityName);
        loadWeather(cityId);

        Button btnOpenDiary = findViewById(R.id.bth_diary);
        btnOpenDiary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DiaryActivity.class);
            startActivity(intent);
        });

        // 城市管理入口
        ImageView ivSettings = findViewById(R.id.ivSettings);
        ivSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.weatherforecast2.Activity.CityManagerActivity.class);
            startActivity(intent);
        });

        // 城市选择入口，点击城市名可切换城市
        tvLocation.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, com.example.weatherforecast2.Activity.CitySelectionActivity.class);
            startActivityForResult(intent, 1001);
        });

        // 今日详情按钮
        Button btnTodayDetail = findViewById(R.id.btnTodayDetail);
        if (btnTodayDetail != null) {
            btnTodayDetail.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.weatherforecast2.Activity.TodayDetailActivity.class);
                intent.putExtra("cityId", cityId);
                startActivity(intent);
            });
        }

        // 15天趋势预报按钮
        Button btnMore = findViewById(R.id.btn15DayForecast);
        if (btnMore != null) {
            btnMore.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, com.example.weatherforecast2.Activity.TrendForecastActivity.class);
                intent.putExtra("cityId", cityId);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String cityId = data.getStringExtra("cityId");
            String cityName = data.getStringExtra("cityName");
            Log.d("WeatherAppDebug", "切换城市: " + cityName + " cityId=" + cityId);
            SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            sp.edit().putString("cityId", cityId).putString("cityName", cityName).apply();
            tvLocation.setText(cityName);
            loadWeather(cityId);
        }
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
        rvForecast = findViewById(R.id.rvForecast);
        rvForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourly = findViewById(R.id.rvHourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadWeather(String cityId) {
        WeatherApiHelper.getWeather7d(cityId, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "天气获取失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        runOnUiThread(() -> updateUI(body));
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "解析天气失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "天气接口错误: HTTP " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 获取逐时天气数据
    private void loadHourlyWeather(String cityName) {
        Log.d("WeatherAPI", "开始获取逐时天气，城市: " + cityName);
        WeatherApiHelper.getHourlyWeather(cityName, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e("WeatherAPI", "逐时天气获取失败: " + e.getMessage());
                runOnUiThread(() -> {
                    // 隐藏逐时天气视图，不影响主界面
                    rvHourly.setVisibility(View.GONE);
                });
            }
            
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        Log.d("WeatherAPI", "逐时天气响应成功: " + body);
                        runOnUiThread(() -> updateHourlyUI(body));
                    } catch (Exception e) {
                        Log.e("WeatherAPI", "解析逐时天气失败: " + e.getMessage());
                        runOnUiThread(() -> rvHourly.setVisibility(View.GONE));
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.body() != null ? response.body().string() : "";
                    } catch (Exception e) {
                        // ignore
                    }
                    Log.e("WeatherAPI", "逐时天气接口错误: HTTP " + response.code() + ", 响应: " + errorBody);
                    runOnUiThread(() -> rvHourly.setVisibility(View.GONE));
                }
            }
        });
    }

    private void updateUI(String body) {
        try {
            JSONObject obj = new JSONObject(body);
            JSONObject cityInfo = obj.getJSONObject("cityInfo");
            JSONObject data = obj.getJSONObject("data");
            String cityName = cityInfo.getString("city");
            String temp = data.getString("wendu");
            String humidity = data.getString("shidu");
            String quality = data.getString("quality");
            JSONArray forecast = data.getJSONArray("forecast");
            JSONArray hourly = data.optJSONArray("hourly");

            // 更新城市名
            tvLocation.setText(cityName);
            
            // 更新当前温度
            tvCurrentTemp.setText(temp + "°");
            
            // 更新天气详情（从今日预报获取更详细信息）
            JSONObject today = forecast.getJSONObject(0);
            String todayType = today.getString("type");
            String high = today.getString("high").replaceAll("[^0-9]", "");
            String low = today.getString("low").replaceAll("[^0-9]", "");
            tvWeatherDetail.setText(todayType + " 最高" + high + "° 最低" + low + "°");
            
            // 更新湿度和空气质量
            tvHumidityAir.setText("湿度：" + humidity + "  空气质量：" + quality);

            // 多日天气列表
            if (forecastAdapter == null) {
                forecastAdapter = new WeatherForecastAdapter(this, forecast);
                rvForecast.setAdapter(forecastAdapter);
            } else {
                rvForecast.setAdapter(new WeatherForecastAdapter(this, forecast));
            }

            // 逐小时天气列表 - 使用心知天气API获取
            // 暂时注释，避免API限制影响主界面
            // loadHourlyWeather(cityName);

            // 今天卡片
            tvDate1.setText("今天");
            tvWeather1.setText(todayType);
            tvTempRange1.setText(today.getString("low") + " ~ " + today.getString("high"));
            ivWeatherIcon1.setImageResource(getWeatherIconResource(todayType));

            // 明天卡片
            if (forecast.length() > 1) {
                JSONObject tomorrow = forecast.getJSONObject(1);
                tvDate2.setText("明天");
                tvWeather2.setText(tomorrow.getString("type"));
                tvTempRange2.setText(tomorrow.getString("low") + " ~ " + tomorrow.getString("high"));
                ivWeatherIcon2.setImageResource(getWeatherIconResource(tomorrow.getString("type")));
            }

            // 后天卡片
            if (forecast.length() > 2) {
                JSONObject afterTomorrow = forecast.getJSONObject(2);
                tvDate3.setText("后天");
                tvWeather3.setText(afterTomorrow.getString("type"));
                tvTempRange3.setText(afterTomorrow.getString("low") + " ~ " + afterTomorrow.getString("high"));
                ivWeatherIcon3.setImageResource(getWeatherIconResource(afterTomorrow.getString("type")));
            }
            
        } catch (Exception e) {
            Log.e("WeatherAPI", "天气数据显示异常", e);
            Toast.makeText(this, "天气数据显示异常", Toast.LENGTH_SHORT).show();
        }
    }

    // 处理心知天气API的逐时天气数据
    private void updateHourlyUI(String body) {
        try {
            JSONObject obj = new JSONObject(body);
            JSONArray results = obj.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject result = results.getJSONObject(0);
                JSONArray hourly = result.getJSONArray("hourly");
                
                // 显示逐时天气RecyclerView
                rvHourly.setVisibility(View.VISIBLE);
                
                // 创建心知天气格式的适配器
                SeniverseHourlyAdapter seniverseAdapter = new SeniverseHourlyAdapter(this, hourly);
                rvHourly.setAdapter(seniverseAdapter);
                
                Log.d("WeatherAPI", "逐时天气数据更新成功，共" + hourly.length() + "小时");
            }
        } catch (Exception e) {
            Log.e("WeatherAPI", "逐时天气UI更新异常", e);
            // 隐藏逐时天气视图
            rvHourly.setVisibility(View.GONE);
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

    private String getWeekday(String dateStr) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date date = sdf.parse(dateStr);
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
            String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
            return weekDays[dayOfWeek - 1];
        } catch (Exception e) {
            return dateStr;
        }
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