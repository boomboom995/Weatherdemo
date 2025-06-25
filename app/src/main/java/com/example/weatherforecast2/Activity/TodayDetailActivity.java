package com.example.weatherforecast2.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherforecast2.R;
import com.example.weatherforecast2.Adapter.HourlyWeatherAdapter;
import com.example.weatherforecast2.Adapter.HourlyLineChartView;
import com.example.weatherforecast2.util.WeatherApiHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class TodayDetailActivity extends AppCompatActivity {
    private RecyclerView rvHourlyWeather;
    private HourlyWeatherAdapter hourlyAdapter;
    private HourlyLineChartView hourlyLineChart;
    private TextView tvTitle, tvRainTip;
    private ImageView btnBack;
    private String cityId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_detail);
        
        initViews();
        setupRecyclerView();
        
        cityId = getIntent().getStringExtra("cityId");
        if (cityId == null) cityId = "101010100"; // 默认北京
        
        loadTodayDetailWeather(cityId);
    }

    private void initViews() {
        rvHourlyWeather = findViewById(R.id.rvHourlyWeather);
        hourlyLineChart = findViewById(R.id.hourlyLineChart);
        tvTitle = findViewById(R.id.tvTitle);
        tvRainTip = findViewById(R.id.tvRainTip);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
        tvTitle.setText("今日详情");
    }

    private void setupRecyclerView() {
        rvHourlyWeather.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadTodayDetailWeather(String cityId) {
        WeatherApiHelper.getWeather7d(cityId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvTitle.setText("获取天气失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONObject obj = new JSONObject(body);
                        JSONObject data = obj.getJSONObject("data");
                        JSONArray forecast = data.getJSONArray("forecast");
                        
                        // 获取今日天气数据
                        JSONObject today = forecast.getJSONObject(0);
                        
                        runOnUiThread(() -> {
                            // 生成模拟的逐时数据用于展示
                            JSONArray mockHourly = generateMockHourlyData(today);
                            
                            hourlyAdapter = new HourlyWeatherAdapter(TodayDetailActivity.this, mockHourly);
                            rvHourlyWeather.setAdapter(hourlyAdapter);
                            hourlyLineChart.setHourlyArray(mockHourly);
                            
                            // 分析降水情况，更新提示
                            updateRainTip(mockHourly);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> tvTitle.setText("解析天气失败: " + e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> tvTitle.setText("天气接口错误: HTTP " + response.code()));
                }
            }
        });
    }
    
    // 根据今日天气生成模拟的逐时数据
    private JSONArray generateMockHourlyData(JSONObject today) {
        JSONArray hourlyArray = new JSONArray();
        try {
            String type = today.getString("type");
            String high = today.getString("high").replaceAll("[^0-9]", "");
            String low = today.getString("low").replaceAll("[^0-9]", "");
            int highTemp = Integer.parseInt(high);
            int lowTemp = Integer.parseInt(low);
            
            // 生成24小时数据
            for (int i = 0; i < 24; i++) {
                JSONObject hourData = new JSONObject();
                
                // 时间
                String time = String.format("%02d:00", i);
                hourData.put("time", time);
                
                // 温度：按照正弦曲线模拟一天的温度变化
                double tempProgress = Math.sin((i - 6) * Math.PI / 12.0); // 6点最低，18点最高
                int temp = (int) (lowTemp + (highTemp - lowTemp) * (tempProgress + 1) / 2);
                hourData.put("temp", String.valueOf(temp));
                
                // 天气类型
                hourData.put("weather", type);
                
                // 风力
                hourData.put("wind", "2级");
                
                hourlyArray.put(hourData);
            }
        } catch (Exception e) {
            // 如果生成失败，返回空数组
        }
        return hourlyArray;
    }
    
    private void updateRainTip(JSONArray hourly) {
        boolean hasRain = false;
        try {
            for (int i = 0; i < hourly.length() && i < 6; i++) { // 检查未来6小时
                JSONObject hour = hourly.getJSONObject(i);
                String weather = hour.optString("weather", "");
                if (weather.contains("雨") || weather.contains("雪") || weather.contains("雷")) {
                    hasRain = true;
                    break;
                }
            }
        } catch (Exception e) {
            // 解析失败，默认无降水
        }
        
        if (hasRain) {
            tvRainTip.setText("未来短时内可能有降水");
        } else {
            tvRainTip.setText("未来短时内无降水");
        }
    }
} 