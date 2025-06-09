// 文件路径: app/src/main/java/com/example/weatherforecast2/Activity/DiaryListActivity.java
package com.example.weatherforecast2.Activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast2.Adapter.DiaryAdapter;
import com.example.weatherforecast2.DatabaseHelper.DiaryDatabaseHelper;
import com.example.weatherforecast2.Entity.DiaryEntry;
import com.example.weatherforecast2.R;

import java.util.List;

public class DiaryListActivity extends AppCompatActivity {

    private RecyclerView rvDiaryList;
    private DiaryAdapter diaryAdapter;
    private DiaryDatabaseHelper dbHelper;
    private List<DiaryEntry> diaryEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        Toolbar toolbar = findViewById(R.id.toolbar_diary_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DiaryDatabaseHelper(this);
        rvDiaryList = findViewById(R.id.rv_diary_list);
        rvDiaryList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 当从编辑页面返回时，重新加载数据以刷新列表
        loadDiaries();
    }

    private void loadDiaries() {
        // 【修改】调用正确的方法名 getAllDiaryEntries()
        diaryEntries = dbHelper.getAllDiaryEntries();

        if (diaryAdapter == null) {
            diaryAdapter = new DiaryAdapter(this, diaryEntries);
            rvDiaryList.setAdapter(diaryAdapter);
        } else {
            // 更新适配器的数据
            diaryAdapter.updateData(diaryEntries);
        }
    }
}