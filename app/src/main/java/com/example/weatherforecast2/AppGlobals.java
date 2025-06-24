package com.example.weatherforecast2;

import android.app.Application;
import android.content.Context;

public class AppGlobals extends Application {
    private static AppGlobals instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }
} 