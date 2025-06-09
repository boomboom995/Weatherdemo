// 文件路径: app/src/main/java/com/example/weatherforecast2/model/DailyForecast.java
package com.example.weatherforecast2.model;

public class DailyForecast {
    private String fxDate;   // 日期
    private String tempMax;  // 最高温度
    private String tempMin;  // 最低温度
    private String iconDay;  // 白天天气图标代码
    private String textDay;  // 白天天气文字

    public String getFxDate() {
        return fxDate;
    }

    public String getTempMax() {
        return tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public String getIconDay() {
        return iconDay;
    }

    public String getTextDay() {
        return textDay;
    }
}