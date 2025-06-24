// 文件路径: app/src/main/java/com/example/weatherforecast2/Activity/DiaryActivity.java

package com.example.weatherforecast2.Activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherforecast2.Adapter.CalendarAdapter;
import com.example.weatherforecast2.AlarmReceiver;
import com.example.weatherforecast2.DatabaseHelper.DiaryDatabaseHelper;
import com.example.weatherforecast2.Entity.DiaryEntry;
import com.example.weatherforecast2.MainActivity;
import com.example.weatherforecast2.R;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.weatherforecast2.util.PerformanceMonitor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiaryActivity extends AppCompatActivity implements CalendarAdapter.CalendarClickListener {

    private TextView tvMonthYear;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    private Calendar currentMonthCalendar;
    private DiaryDatabaseHelper dbHelper;
    private FloatingActionButton fabAddDiary, fabAddReminder;
    private ImageView ivPrevMonth, ivNextMonth;
    
    // 性能优化相关
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        // 请求通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        dbHelper = new DiaryDatabaseHelper(this);
        tvMonthYear = findViewById(R.id.tv_month_year);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        fabAddDiary = findViewById(R.id.fab_add_diary);
        fabAddReminder = findViewById(R.id.fab_add_reminder);
        ivPrevMonth = findViewById(R.id.iv_prev_month);
        ivNextMonth = findViewById(R.id.iv_next_month);
        currentMonthCalendar = Calendar.getInstance();

        // 优化RecyclerView性能
        setupRecyclerView();

        ivPrevMonth.setOnClickListener(v -> {
            if (!isRefreshing) {
                currentMonthCalendar.add(Calendar.MONTH, -1);
                refreshCalendarAsync();
            }
        });
        ivNextMonth.setOnClickListener(v -> {
            if (!isRefreshing) {
                currentMonthCalendar.add(Calendar.MONTH, 1);
                refreshCalendarAsync();
            }
        });
        tvMonthYear.setOnClickListener(v -> showMonthYearPicker());
        fabAddDiary.setOnClickListener(v -> {
            Intent intent = new Intent(DiaryActivity.this, MoodSelectionActivity.class);
            intent.putExtra("selected_date", new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
            startActivity(intent);
        });
        fabAddReminder.setOnClickListener(v -> showAddReminderDialog());
        
        // 异步初始化日历
        refreshCalendarAsync();
        
        Button b = findViewById(R.id.bth_diarytomain);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( DiaryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRecyclerView() {
        // 性能优化配置
        calendarRecyclerView.setHasFixedSize(true);
        calendarRecyclerView.setItemViewCacheSize(42); // 缓存所有日期项
        calendarRecyclerView.setDrawingCacheEnabled(true);
        calendarRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        GridLayoutManager layoutManager = new GridLayoutManager(this, 7);
        layoutManager.setItemPrefetchEnabled(true);
        layoutManager.setInitialPrefetchItemCount(42);
        calendarRecyclerView.setLayoutManager(layoutManager);
    }

    private void showAddReminderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);
        builder.setView(dialogView);

        final EditText etPlanContent = dialogView.findViewById(R.id.et_plan_content);
        final Button btnSelectTime = dialogView.findViewById(R.id.btn_dialog_select_time);
        final TextView tvSelectedTime = dialogView.findViewById(R.id.tv_selected_time); //【修正】变量名拼写错误
        final Calendar reminderCalendar = Calendar.getInstance();

        btnSelectTime.setOnClickListener(v -> {
            Calendar currentDate = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                reminderCalendar.set(year, month, dayOfMonth);
                new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderCalendar.set(Calendar.MINUTE, minute);
                    reminderCalendar.set(Calendar.SECOND, 0);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    tvSelectedTime.setText(sdf.format(reminderCalendar.getTime()));
                }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setTitle("新建日程提醒")
                .setPositiveButton("保存", (dialog, which) -> {
                    String planContent = etPlanContent.getText().toString().trim();
                    if (planContent.isEmpty() || tvSelectedTime.getText().toString().equals("未选择")) {
                        Toast.makeText(this, "内容和时间均不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long reminderMillis = reminderCalendar.getTimeInMillis();
                    if (reminderMillis <= System.currentTimeMillis()) {
                        Toast.makeText(this, "提醒时间必须在未来", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DiaryEntry entry = new DiaryEntry();
                    entry.setPlan(true);
                    entry.setPlanContent(planContent);
                    entry.setDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(reminderCalendar.getTime()));
                    entry.setReminderTime(reminderMillis);

                    // 先插入数据库获取ID，再用这个ID去设置闹钟
                    long id = dbHelper.addOrUpdateDiary(entry);
                    entry.setId((int) id);

                    setAlarm(entry);
                    refreshCalendarAsync();
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.create().show();
    }

    private void setAlarm(DiaryEntry entry) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", "日程提醒");
        intent.putExtra("content", entry.getPlanContent());
        intent.putExtra("id", entry.getId());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, entry.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            // 检查是否有精确闹钟的权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // 引导用户去设置开启权限
                    Intent intentSetting = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intentSetting);
                    Toast.makeText(this, "请为本应用开启精确闹钟权限", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, entry.getReminderTime(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, entry.getReminderTime(), pendingIntent);
            }
            Toast.makeText(this, "提醒设置成功！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCalendarAsync();
    }

    // 异步刷新日历，避免主线程阻塞
    private void refreshCalendarAsync() {
        if (isRefreshing) return;
        
        PerformanceMonitor.startOperation("refreshCalendarAsync");
        isRefreshing = true;
        executorService.execute(() -> {
            try {
                PerformanceMonitor.checkMainThread();
                long startTime = System.currentTimeMillis();
                
                List<Date> days = getDaysInMonth(currentMonthCalendar);
                SimpleDateFormat monthYearFormat = new SimpleDateFormat("yyyy年 MM月", Locale.getDefault());
                String monthYearText = monthYearFormat.format(currentMonthCalendar.getTime());
                
                long dataPrepTime = System.currentTimeMillis() - startTime;
                PerformanceMonitor.logOperation("数据准备", dataPrepTime);
                
                mainHandler.post(() -> {
                    PerformanceMonitor.checkMainThread();
                    long uiStartTime = System.currentTimeMillis();
                    
                    tvMonthYear.setText(monthYearText);
                    calendarAdapter = new CalendarAdapter(this, days, currentMonthCalendar, this);
                    calendarRecyclerView.setAdapter(calendarAdapter);
                    
                    long uiTime = System.currentTimeMillis() - uiStartTime;
                    PerformanceMonitor.logOperation("UI更新", uiTime);
                    
                    isRefreshing = false;
                    PerformanceMonitor.endOperation();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "日历加载失败", Toast.LENGTH_SHORT).show();
                    isRefreshing = false;
                    PerformanceMonitor.endOperation();
                });
            }
        });
    }

    private void showMonthYearPicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("选择年月");
        builder.setSelection(currentMonthCalendar.getTimeInMillis());
        final MaterialDatePicker<Long> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            currentMonthCalendar.setTimeInMillis(selection);
            refreshCalendarAsync();
        });
        datePicker.show(getSupportFragmentManager(), "MONTH_YEAR_PICKER");
    }

    private List<Date> getDaysInMonth(Calendar calendar) {
        List<Date> days = new ArrayList<>();
        Calendar tempCalendar = (Calendar) calendar.clone();
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK);
        int offset = firstDayOfWeek - Calendar.SUNDAY;
        if (offset < 0) offset += 7;
        tempCalendar.add(Calendar.DAY_OF_MONTH, -offset);
        for (int i = 0; i < 42; i++) {
            days.add(tempCalendar.getTime());
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    @Override
    public void onDayClick(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateStr = dateFormat.format(date);
        boolean hasEntry = dbHelper.hasEntryForDate(selectedDateStr);
        Intent intent;
        if (hasEntry) {
            intent = new Intent(DiaryActivity.this, DiaryListActivity.class);
            intent.putExtra("focus_date", selectedDateStr);
        } else {
            intent = new Intent(DiaryActivity.this, MoodSelectionActivity.class);
        }
        intent.putExtra("selected_date", selectedDateStr);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}