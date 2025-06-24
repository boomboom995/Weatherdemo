package com.example.weatherforecast2.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.example.weatherforecast2.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.content.SharedPreferences;

public class CityManagerActivity extends Activity {
    private LinearLayout llCities;
    private ImageView btnBack;
    private EditText etSearch;
    private SharedPreferences sp;
    private static final String PREFS_NAME = "weather";
    private static final String KEY_CITY_LIST = "cityList";
    private static final String KEY_CITY_NAME_LIST = "cityNameList";
    private static final int REQ_SELECT_CITY = 1001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);
        llCities = findViewById(R.id.llCities);
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        btnBack.setOnClickListener(v -> finish());
        etSearch.setOnClickListener(v -> {
            Intent intent = new Intent(CityManagerActivity.this, CitySelectionActivity.class);
            startActivityForResult(intent, REQ_SELECT_CITY);
        });
        loadCities();
    }

    private void loadCities() {
        llCities.removeAllViews();
        // 显示最近定位城市
        // String lastLocatedId = sp.getString("lastLocatedCityId", null);
        // String lastLocatedName = sp.getString("lastLocatedCityName", null);
        // if (lastLocatedId != null && lastLocatedName != null) {
        //     View item = getLayoutInflater().inflate(R.layout.item_city_manager, llCities, false);
        //     TextView tvName = item.findViewById(R.id.tvCityName);
        //     View btnDelete = item.findViewById(R.id.btnDelete);
        //     tvName.setText("定位城市：" + lastLocatedName);
        //     btnDelete.setVisibility(View.GONE); // 定位城市不允许删除
        //     item.setOnClickListener(v -> {
        //         Intent result = new Intent();
        //         result.putExtra("cityId", lastLocatedId);
        //         result.putExtra("cityName", lastLocatedName);
        //         setResult(RESULT_OK, result);
        //         finish();
        //     });
        //     llCities.addView(item);
        // }
        Set<String> cityIds = sp.getStringSet(KEY_CITY_LIST, new HashSet<>());
        Set<String> cityNames = sp.getStringSet(KEY_CITY_NAME_LIST, new HashSet<>());
        List<String> idList = new ArrayList<>(cityIds);
        List<String> nameList = new ArrayList<>(cityNames);
        for (int i = 0; i < idList.size(); i++) {
            String cityId = idList.get(i);
            String cityName = nameList.size() > i ? nameList.get(i) : cityId;
            View item = getLayoutInflater().inflate(R.layout.item_city_manager, llCities, false);
            TextView tvName = item.findViewById(R.id.tvCityName);
            View btnDelete = item.findViewById(R.id.btnDelete);
            tvName.setText(cityName);
            btnDelete.setOnClickListener(v -> {
                cityIds.remove(cityId);
                cityNames.remove(cityName);
                sp.edit().putStringSet(KEY_CITY_LIST, cityIds).putStringSet(KEY_CITY_NAME_LIST, cityNames).apply();
                loadCities();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            });
            item.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("cityId", cityId);
                result.putExtra("cityName", cityName);
                setResult(RESULT_OK, result);
                finish();
            });
            llCities.addView(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_SELECT_CITY && resultCode == RESULT_OK && data != null) {
            String cityId = data.getStringExtra("cityId");
            String cityName = data.getStringExtra("cityName");
            Set<String> cityIds = new HashSet<>(sp.getStringSet(KEY_CITY_LIST, new HashSet<>()));
            Set<String> cityNames = new HashSet<>(sp.getStringSet(KEY_CITY_NAME_LIST, new HashSet<>()));
            cityIds.add(cityId);
            cityNames.add(cityName);
            sp.edit().putStringSet(KEY_CITY_LIST, cityIds).putStringSet(KEY_CITY_NAME_LIST, cityNames).apply();
            loadCities();
        }
    }
} 