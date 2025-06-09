package com.example.weatherforecast2.model;

import java.util.List;

public class Data {
    private String wendu;
    private String ganmao;
    private List<Forecast> forecast;

    public String getWendu() { return wendu; }
    public String getGanmao() { return ganmao; }
    public List<Forecast> getForecast() { return forecast; }
}