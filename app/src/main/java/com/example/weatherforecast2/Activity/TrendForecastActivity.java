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
import com.example.weatherforecast2.Adapter.TrendForecastAdapter;
import com.example.weatherforecast2.Adapter.TrendLineChartView;
import com.example.weatherforecast2.util.WeatherApiHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class TrendForecastActivity extends AppCompatActivity {
    private RecyclerView rvTrendForecast;
    private TrendForecastAdapter trendAdapter;
    private TrendLineChartView trendLineChart;
    private TextView tvTitle, tvCityName;
    private ImageView btnBack;
    private String cityId, cityName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend_forecast);
        
        initViews();
        setupRecyclerView();
        
        cityId = getIntent().getStringExtra("cityId");
        cityName = getIntent().getStringExtra("cityName");
        if (cityId == null) cityId = "101010100"; // 默认北京
        if (cityName == null) cityName = "北京";
        
        // 显示城市名
        tvCityName.setText(cityName);
        
        loadTrendForecast(cityId);
    }

    private void initViews() {
        rvTrendForecast = findViewById(R.id.rvTrendForecast);
        trendLineChart = findViewById(R.id.trendLineChart);
        tvTitle = findViewById(R.id.tvTitle);
        tvCityName = findViewById(R.id.tvCityName);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());
        tvTitle.setText("15天趋势预报");
    }

    private void setupRecyclerView() {
        // 改为水平滚动布局
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvTrendForecast.setLayoutManager(layoutManager);
        
        // 优化滚动性能
        rvTrendForecast.setHasFixedSize(true);
        rvTrendForecast.setItemViewCacheSize(20);
    }

    private void loadTrendForecast(String cityId) {
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
                        JSONObject cityInfo = obj.getJSONObject("cityInfo");
                        JSONObject data = obj.getJSONObject("data");
                        JSONArray forecast = data.getJSONArray("forecast");
                        
                        // 更新城市名（如果API返回了城市信息）
                        String apiCityName = cityInfo.optString("city", cityName);
                        
                        // 取前15天（如果不足15天就显示全部）
                        JSONArray trend15 = new JSONArray();
                        for (int i = 0; i < Math.min(15, forecast.length()); i++) {
                            trend15.put(forecast.getJSONObject(i));
                        }
                        
                        runOnUiThread(() -> {
                            tvCityName.setText(apiCityName);
                            trendAdapter = new TrendForecastAdapter(TrendForecastActivity.this, trend15);
                            rvTrendForecast.setAdapter(trendAdapter);
                            trendLineChart.setForecastArray(trend15);
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> tvTitle.setText("解析天气失败"));
                    }
                } else {
                    runOnUiThread(() -> tvTitle.setText("天气接口错误"));
                }
            }
        });
    }
} 