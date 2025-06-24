package com.example.weatherforecast2.util;

import android.util.Log;
import android.os.SystemClock;

public class PerformanceMonitor {
    private static final String TAG = "PerformanceMonitor";
    private static long startTime;
    private static String currentOperation;

    public static void startOperation(String operationName) {
        startTime = SystemClock.elapsedRealtime();
        currentOperation = operationName;
        Log.d(TAG, "开始操作: " + operationName);
    }

    public static void endOperation() {
        if (currentOperation != null) {
            long duration = SystemClock.elapsedRealtime() - startTime;
            Log.d(TAG, "操作完成: " + currentOperation + ", 耗时: " + duration + "ms");
            
            // 如果操作时间超过100ms，记录警告
            if (duration > 100) {
                Log.w(TAG, "性能警告: " + currentOperation + " 耗时过长 (" + duration + "ms)");
            }
            
            currentOperation = null;
        }
    }

    public static void logOperation(String operationName, long duration) {
        Log.d(TAG, "操作: " + operationName + ", 耗时: " + duration + "ms");
        if (duration > 100) {
            Log.w(TAG, "性能警告: " + operationName + " 耗时过长 (" + duration + "ms)");
        }
    }

    public static void checkMainThread() {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            Log.d(TAG, "当前在主线程执行");
        } else {
            Log.d(TAG, "当前在后台线程执行");
        }
    }
} 