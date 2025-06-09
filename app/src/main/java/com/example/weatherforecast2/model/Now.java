package com.example.weatherforecast2.model;

// 这个类用来匹配JSON里 "now" 对象内部的数据
public class Now {
    private String temp;
    private String text;
    private String humidity;

    // Getter方法
    public String getTemp() {
        return temp;
    }

    public String getText() {
        return text;
    }

    public String getHumidity() {
        return humidity;
    }
}