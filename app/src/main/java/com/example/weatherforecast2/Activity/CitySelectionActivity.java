package com.example.weatherforecast2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.weatherforecast2.R;
import com.example.weatherforecast2.util.WeatherApiHelper;
import com.example.weatherforecast2.util.LocationHelper;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.content.SharedPreferences;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;
import android.util.Log;

public class CitySelectionActivity extends Activity {
    private EditText etSearch;
    private TextView tvCancel;
    private TextView tvLocationCity;
    private GridLayout gridHotCities;
    private RecyclerView rvCities;
    private ProgressBar progressBar;
    private CityAdapter adapter;
    private LinearLayout layoutWeatherPreview;
    private TextView tvPreviewCity;
    private RecyclerView rvPreviewWeather;
    private final List<String[]> hotCities = Arrays.asList(
            new String[][]{
                    {"北京", "101010100"}, {"上海", "101020100"}, {"广州", "101280101"}, {"深圳", "101280601"},
                    {"沈阳", "101070101"}, {"南京", "101190101"}, {"杭州", "101210101"}, {"武汉", "101200101"},
                    {"重庆", "101040100"}, {"西安", "101110101"}, {"成都", "101270101"}, {"长沙", "101250101"},
                    {"珠海", "101280701"}, {"佛山", "101280800"}, {"苏州", "101190401"}, {"厦门", "101230201"},
                    {"南宁", "101300101"}, {"昆明", "101290101"}, {"福州", "101230101"}, {"青岛", "101120201"},
                    {"石家庄", "101090101"}, {"太原", "101100101"}, {"天津", "101030100"}
            }
    );
    private boolean inSearchMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_selection);
        etSearch = findViewById(R.id.etSearch);
        tvCancel = findViewById(R.id.tvCancel);
        tvLocationCity = findViewById(R.id.tvLocationCity);
        gridHotCities = findViewById(R.id.gridHotCities);
        rvCities = findViewById(R.id.rvCities);
        progressBar = findViewById(R.id.progressBar);
        layoutWeatherPreview = findViewById(R.id.layoutWeatherPreview);
        tvPreviewCity = findViewById(R.id.tvPreviewCity);
        rvPreviewWeather = findViewById(R.id.rvPreviewWeather);
        adapter = new CityAdapter(new ArrayList<>(), (id, name) -> {
            // 展示天气预览
            showWeatherPreview(id, name);
        }, (id, name) -> {
            // 添加城市
            Log.d("WeatherAppDebug", "选择城市: " + name + " cityId=" + id);
            Intent result = new Intent();
            result.putExtra("cityId", id);
            result.putExtra("cityName", name);
            setResult(RESULT_OK, result);
            finish();
        });
        rvCities.setLayoutManager(new LinearLayoutManager(this));
        rvCities.setAdapter(adapter);
        // 搜索输入监听
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    inSearchMode = true;
                    searchCity(s.toString());
                } else {
                    inSearchMode = false;
                    adapter.updateData(new ArrayList<>());
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        // 取消按钮
        tvCancel.setOnClickListener(v -> finish());
        // 热门城市流式布局
        gridHotCities.removeAllViews();
        for (String[] city : hotCities) {
            TextView tv = new TextView(this);
            tv.setText(city[0]);
            tv.setTextSize(16);
            tv.setTextColor(0xFF2196F3);
            tv.setBackgroundResource(R.drawable.rounded_edittext_background);
            tv.setPadding(32, 16, 32, 16);
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("cityId", city[1]);
                result.putExtra("cityName", city[0]);
                setResult(RESULT_OK, result);
                finish();
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.setMargins(16, 16, 16, 16);
            gridHotCities.addView(tv, lp);
        }
    }

    private void searchCity(String keyword) {
        progressBar.setVisibility(View.VISIBLE);
        WeatherApiHelper.searchCity(keyword, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CitySelectionActivity.this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                try {
                    String body = response.body().string();
                    JSONObject obj = new JSONObject(body);
                    JSONArray arr = obj.optJSONArray("location");
                    List<CityItem> list = new ArrayList<>();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject c = arr.getJSONObject(i);
                            String id = c.getString("id");
                            String name = c.getString("name");
                            list.add(new CityItem(id, name));
                        }
                    }
                    runOnUiThread(() -> adapter.updateData(list));
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(CitySelectionActivity.this, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void showWeatherPreview(String cityId, String cityName) {
        layoutWeatherPreview.setVisibility(View.VISIBLE);
        tvPreviewCity.setText(cityName + " - 天气预览");
        WeatherApiHelper.getWeather7d(cityId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CitySelectionActivity.this, "天气获取失败", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject obj = new JSONObject(body);
                    JSONArray arr = obj.optJSONArray("daily");
                    List<WeatherPreviewItem> list = new ArrayList<>();
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject c = arr.getJSONObject(i);
                            String date = c.getString("fxDate");
                            String text = c.getString("textDay");
                            String tempMax = c.getString("tempMax");
                            String tempMin = c.getString("tempMin");
                            list.add(new WeatherPreviewItem(date, text, tempMax, tempMin));
                        }
                    }
                    runOnUiThread(() -> rvPreviewWeather.setAdapter(new WeatherPreviewAdapter(list)));
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(CitySelectionActivity.this, "天气解析失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
} 