package com.example.weatherforecast2.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import android.util.Log;
import android.widget.Toast;

public class LocationHelper {
    public interface LocationCallback {
        void onLocation(double lat, double lon, String cityName);
        void onError(String msg);
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Log.d("LocationHelper", "GPS定位成功: lat=" + lat + ", lon=" + lon);
                    Toast.makeText(context, "GPS定位成功", Toast.LENGTH_SHORT).show();
                    WeatherApiHelper.geoCity(lat, lon, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("LocationHelper", "城市反查失败: " + e.getMessage());
                            Toast.makeText(context, "城市反查失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            callback.onError("城市反查失败: " + e.getMessage());
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String body = response.body().string();
                                JSONObject obj = new JSONObject(body);
                                JSONArray arr = obj.optJSONArray("location");
                                if (arr != null && arr.length() > 0) {
                                    JSONObject c = arr.getJSONObject(0);
                                    String cityName = c.getString("name");
                                    Log.d("LocationHelper", "反查城市成功: " + cityName);
                                    Toast.makeText(context, "反查城市成功: " + cityName, Toast.LENGTH_SHORT).show();
                                    callback.onLocation(lat, lon, cityName);
                                } else {
                                    Log.e("LocationHelper", "未找到城市");
                                    Toast.makeText(context, "未找到城市", Toast.LENGTH_SHORT).show();
                                    callback.onError("未找到城市");
                                }
                            } catch (Exception e) {
                                Log.e("LocationHelper", "解析失败: " + e.getMessage());
                                Toast.makeText(context, "解析失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                callback.onError("解析失败: " + e.getMessage());
                            }
                        }
                    });
                }
                @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {
                    Log.e("LocationHelper", "定位服务未开启");
                    Toast.makeText(context, "定位服务未开启", Toast.LENGTH_SHORT).show();
                    callback.onError("定位服务未开启");
                }
            }, null);
        } catch (SecurityException e) {
            Log.e("LocationHelper", "定位权限异常: " + e.getMessage());
            Toast.makeText(context, "定位权限异常: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            callback.onError("定位权限异常: " + e.getMessage());
        } catch (Exception e) {
            Log.e("LocationHelper", "定位异常: " + e.getMessage());
            Toast.makeText(context, "定位异常: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            callback.onError("定位异常: " + e.getMessage());
        }
    }

    public static void getCityByIP(LocationCallback callback) {
        String url = "https://api.map.baidu.com/location/ip?ak=7khcth5v6N9ddhW0spLMGg8ZuF5HAbzq&coor=bd09ll";
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("LocationHelper", "IP定位失败: " + e.getMessage());
                callback.onError("IP定位失败: " + e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                try {
                    String body = response.body().string();
                    Log.d("LocationHelper", "IP定位返回: " + body);
                    org.json.JSONObject obj = new org.json.JSONObject(body);
                    org.json.JSONObject content = obj.optJSONObject("content");
                    if (content != null) {
                        org.json.JSONObject addressDetail = content.optJSONObject("address_detail");
                        if (addressDetail != null) {
                            String city = addressDetail.optString("city");
                            Log.d("LocationHelper", "IP定位成功, city=" + city);
                            // 用和风天气API查城市ID
                            com.example.weatherforecast2.util.WeatherApiHelper.searchCity(city, new okhttp3.Callback() {
                                @Override
                                public void onFailure(okhttp3.Call call, java.io.IOException e) {
                                    Log.e("LocationHelper", "城市ID查找失败: " + e.getMessage());
                                    callback.onError("城市ID查找失败: " + e.getMessage());
                                }
                                @Override
                                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                                    try {
                                        String body = response.body().string();
                                        org.json.JSONObject obj = new org.json.JSONObject(body);
                                        org.json.JSONArray arr = obj.optJSONArray("location");
                                        if (arr != null && arr.length() > 0) {
                                            org.json.JSONObject c = arr.getJSONObject(0);
                                            String id = c.getString("id");
                                            Log.d("LocationHelper", "IP定位反查城市ID成功: " + id);
                                            callback.onLocation(0, 0, city); // 经纬度未知
                                        } else {
                                            Log.e("LocationHelper", "未找到城市ID");
                                            callback.onError("未找到城市ID");
                                        }
                                    } catch (Exception e) {
                                        Log.e("LocationHelper", "城市ID解析失败: " + e.getMessage());
                                        callback.onError("城市ID解析失败: " + e.getMessage());
                                    }
                                }
                            });
                            return;
                        }
                    }
                    Log.e("LocationHelper", "IP定位解析失败");
                    callback.onError("IP定位解析失败");
                } catch (Exception e) {
                    Log.e("LocationHelper", "IP定位解析异常: " + e.getMessage());
                    callback.onError("IP定位解析异常: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 自动优先用GPS/基站定位，失败后兜底用IP定位
     */
    public static void getCurrentCityAuto(Context context, LocationCallback callback) {
        getCurrentLocation(context, new LocationCallback() {
            @Override
            public void onLocation(double lat, double lon, String cityName) {
                Log.d("LocationHelper", "自动定位成功: " + cityName);
                Toast.makeText(context, "自动定位成功: " + cityName, Toast.LENGTH_SHORT).show();
                callback.onLocation(lat, lon, cityName);
            }
            @Override
            public void onError(String msg) {
                Log.e("LocationHelper", "自动定位失败: " + msg);
                Toast.makeText(context, "自动定位失败，尝试IP定位: " + msg, Toast.LENGTH_SHORT).show();
                getCityByIP(callback);
            }
        });
    }
} 