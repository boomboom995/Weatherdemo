// 文件路径: app/src/main/java/com/example/weatherforecast2/Activity/DiaryEditActivity.java
package com.example.weatherforecast2.Activity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherforecast2.AlarmReceiver;
import com.example.weatherforecast2.DatabaseHelper.DiaryDatabaseHelper;
import com.example.weatherforecast2.Entity.DiaryEntry;
import com.example.weatherforecast2.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DiaryEditActivity extends AppCompatActivity {

    private EditText etDiaryContent;
    private Button btnSave, btnSetReminder;
    private TextView tvReminderTime;
    private DiaryDatabaseHelper dbHelper;
    private String selectedDate;
    private String selectedMood;
    private Calendar reminderCalendar;
    private DiaryEntry currentEntry; // 用于持有当前编辑的条目

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        dbHelper = new DiaryDatabaseHelper(this);
        etDiaryContent = findViewById(R.id.et_diary_content);
        btnSave = findViewById(R.id.btn_save);
        btnSetReminder = findViewById(R.id.btn_set_reminder);
        tvReminderTime = findViewById(R.id.tv_reminder_time);
        reminderCalendar = Calendar.getInstance();

        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("selected_date");
        selectedMood = intent.getStringExtra("selected_mood");

        // 这里可以添加逻辑，如果是修改模式，则加载现有数据
        // currentEntry = dbHelper.getDiaryByDate(selectedDate);
        // if(currentEntry != null) { etDiaryContent.setText(currentEntry.getContent()); }

        btnSetReminder.setOnClickListener(v -> showDateTimePicker());
        btnSave.setOnClickListener(v -> saveDiary());
    }

    private void showDateTimePicker() {
        Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            reminderCalendar.set(Calendar.YEAR, year);
            reminderCalendar.set(Calendar.MONTH, month);
            reminderCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderCalendar.set(Calendar.MINUTE, minute);
                reminderCalendar.set(Calendar.SECOND, 0);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                tvReminderTime.setText("提醒于: " + sdf.format(reminderCalendar.getTime()));

            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();

        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveDiary() {
        String content = etDiaryContent.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 如果是修改模式，则使用 currentEntry；否则创建新对象
        if (currentEntry == null) {
            currentEntry = new DiaryEntry();
        }

        currentEntry.setDate(selectedDate);
        currentEntry.setMood(selectedMood);
        currentEntry.setContent(content);

        long reminderMillis = 0;
        if (!tvReminderTime.getText().toString().equals("未设置提醒")) {
            reminderMillis = reminderCalendar.getTimeInMillis();
            if (reminderMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "提醒时间必须在未来", Toast.LENGTH_SHORT).show();
                return;
            }
            currentEntry.setReminderTime(reminderMillis);
            setAlarm(currentEntry);
        } else {
            currentEntry.setReminderTime(0);
        }

        // 【修改】调用正确的方法名
        dbHelper.addOrUpdateDiary(currentEntry);

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setAlarm(DiaryEntry entry) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", "日程提醒: " + entry.getMood());
        intent.putExtra("content", entry.getContent());
        int notificationId = (int) System.currentTimeMillis();
        intent.putExtra("id", notificationId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, entry.getReminderTime(), pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, entry.getReminderTime(), pendingIntent);
        }
        Toast.makeText(this, "提醒设置成功！", Toast.LENGTH_SHORT).show();
    }
}