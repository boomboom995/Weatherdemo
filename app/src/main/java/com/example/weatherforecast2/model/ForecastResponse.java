// 文件路径: app/src/main/java/com/example/weatherforecast2/model/ForecastResponse.java
package com.example.weatherforecast2.model;

import java.util.List;

public class ForecastResponse {
    private String code;
    private List<DailyForecast> daily;

    public String getCode() {
        return code;
    }

    public List<DailyForecast> getDaily() {
        return daily;
    }
}