package com.example.weatherforecast2.model;

public class Weather {
    private CityInfo cityInfo;
    private Data data;
    private String status;
    private String message;

    public CityInfo getCityInfo() { return cityInfo; }
    public Data getData() { return data; }
    public String getStatus() { return status; }
    public String getMessage() { return message; }
}