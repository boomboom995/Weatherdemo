package com.example.weatherforecast2.model;

import java.util.List;

public class Data {
    private String wendu;
    private String ganmao;
    private List<Forecast> forecast;
    private String shidu;
    private String pm25;
    private String quality;

    // Getters
    public String getWendu() { return wendu; }
    public String getGanmao() { return ganmao; }
    public List<Forecast> getForecast() { return forecast; }
    public String getShidu() { return shidu; }
    public String getPm25() { return pm25; }
    public String getQuality() { return quality; }
}