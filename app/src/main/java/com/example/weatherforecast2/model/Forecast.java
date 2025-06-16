package com.example.weatherforecast2.model;

public class Forecast {
    private String date;
    private String high;
    private String low;
    private String type;
    private String fx; // 风向
    private String fl; // 风力

    // Getters
    public String getDate() { return date; }
    public String getHigh() { return high; }
    public String getLow() { return low; }
    public String getType() { return type; }
    public String getFx() { return fx; }
    public String getFl() { return fl; }
}