package com.example.weatherforecast2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherforecast2.Activity.DiaryActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button btnOpenDiary = findViewById(R.id.bth_diary); // 绑定按钮
        btnOpenDiary.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DiaryActivity.class);
            startActivity(intent);
        });
    }
}