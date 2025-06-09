package com.example.weatherforecast2.model;

// 这个类用来匹配JSON的最外层结构
public class WeatherResponse {
    private String code;
    private Now now;

    // Getter方法，让Gson可以访问并赋值
    public String getCode() {
        return code;
    }

    public Now getNow() {
        return now;
    }
}