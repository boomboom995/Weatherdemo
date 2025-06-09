// 文件路径: app/src/main/java/com/example/weatherforecast2/AlarmReceiver.java
package com.example.weatherforecast2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "DIARY_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "日记提醒";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 从Intent中获取日程的标题和内容
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        int notificationId = intent.getIntExtra("id", 0);

        // 兼容 Android 8.0 及以上版本，需要创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // 构建通知
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 使用你自己的应用图标
                .setAutoCancel(true) // 点击后自动消失
                .build();

        // 发送通知
        notificationManager.notify(notificationId, notification);
    }
}