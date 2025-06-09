package com.example.weatherforecast2.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherforecast2.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MoodSelectionActivity extends AppCompatActivity {

    private TextView tvMoodDate;
    private ImageView ivCloseMoodSelection;
    private ImageView ivSelectedMoodCenter;
    private Button btnConfirmMood;
    private GridLayout moodGrid;

    private String selectedDate;
    private String selectedMoodTag = null; // 用于存储选中的心情Tag
    private Drawable selectedMoodDrawable = null; // 用于存储选中的心情图标

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_selection);

        // 获取日期
        if (getIntent().hasExtra("selected_date")) {
            selectedDate = getIntent().getStringExtra("selected_date");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = dateFormat.format(Calendar.getInstance().getTime());
        }

        // 初始化视图
        tvMoodDate = findViewById(R.id.tv_mood_date);
        ivCloseMoodSelection = findViewById(R.id.iv_close_mood_selection);
        ivSelectedMoodCenter = findViewById(R.id.iv_selected_mood_center);
        btnConfirmMood = findViewById(R.id.btn_confirm_mood);
        moodGrid = findViewById(R.id.mood_grid);

        displayDate(selectedDate);
        ivCloseMoodSelection.setOnClickListener(v -> finish());

        // 为GridLayout中的所有ImageView设置点击监听器
        for (int i = 0; i < moodGrid.getChildCount(); i++) {
            ImageView moodIcon = (ImageView) moodGrid.getChildAt(i);
            moodIcon.setOnClickListener(v -> {
                // 更新中央选中的心情
                selectedMoodDrawable = moodIcon.getDrawable();
                selectedMoodTag = (String) moodIcon.getTag();

                ivSelectedMoodCenter.setImageDrawable(selectedMoodDrawable);
                ivSelectedMoodCenter.setVisibility(View.VISIBLE);
            });
        }

        // 确认按钮的点击逻辑
        btnConfirmMood.setOnClickListener(v -> {
            if (selectedMoodTag == null) {
                Toast.makeText(this, "请先选择一个心情", Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog();
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("确认心情")
                .setMessage("要以「" + selectedMoodTag + "」的心情为 " + selectedDate + " 创建一篇日记吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    navigateToDiaryEdit(selectedMoodTag);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void navigateToDiaryEdit(String mood) {
        Intent intent = new Intent(MoodSelectionActivity.this, DiaryEditActivity.class);
        intent.putExtra("selected_date", selectedDate);
        intent.putExtra("selected_mood", mood);
        startActivity(intent);
        finish(); // 完成心情选择后关闭此界面
    }

    private void displayDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            SimpleDateFormat outputFormat = new SimpleDateFormat("M月d日\nEEEE", Locale.getDefault());
            tvMoodDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
            tvMoodDate.setText(dateString);
        }
    }
}