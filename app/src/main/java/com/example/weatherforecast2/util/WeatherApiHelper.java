package com.example.weatherforecast2.util;

import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.IOException;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.weatherforecast2.AppGlobals;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public class WeatherApiHelper {
    private static final String KEY = "你的KEY";
    private static final String BASE_URL = "https://qa7qqqmxmm.re.qweatherapi.com";
    
    // 心知天气API配置
    private static final String SENIVERSE_KEY = "S-43cMjSNkdYMmSS-";
    private static final String SENIVERSE_BASE_URL = "https://api.seniverse.com/v3";
    
    private static final OkHttpClient client = new OkHttpClient();

    // 本地城市数据库
    private static final List<String[]> CITIES_DATABASE = Arrays.asList(
        // [城市名, cityId]
        new String[]{"北京", "101010100"}, new String[]{"上海", "101020100"}, new String[]{"天津", "101030100"},
        new String[]{"重庆", "101040100"}, new String[]{"哈尔滨", "101050101"}, new String[]{"长春", "101060101"},
        new String[]{"沈阳", "101070101"}, new String[]{"呼和浩特", "101080101"}, new String[]{"石家庄", "101090101"},
        new String[]{"太原", "101100101"}, new String[]{"西安", "101110101"}, new String[]{"济南", "101120101"},
        new String[]{"青岛", "101120201"}, new String[]{"郑州", "101180101"}, new String[]{"南京", "101190101"},
        new String[]{"苏州", "101190401"}, new String[]{"杭州", "101210101"}, new String[]{"宁波", "101210401"},
        new String[]{"合肥", "101220101"}, new String[]{"福州", "101230101"}, new String[]{"厦门", "101230201"},
        new String[]{"南昌", "101240101"}, new String[]{"长沙", "101250101"}, new String[]{"武汉", "101200101"},
        new String[]{"广州", "101280101"}, new String[]{"深圳", "101280601"}, new String[]{"珠海", "101280701"},
        new String[]{"佛山", "101280800"}, new String[]{"南宁", "101300101"}, new String[]{"海口", "101310101"},
        new String[]{"成都", "101270101"}, new String[]{"贵阳", "101260101"}, new String[]{"昆明", "101290101"},
        new String[]{"拉萨", "101140101"}, new String[]{"兰州", "101160101"}, new String[]{"西宁", "101150101"},
        new String[]{"银川", "101170101"}, new String[]{"乌鲁木齐", "101130101"}, new String[]{"大连", "101070201"},
        new String[]{"无锡", "101190201"}, new String[]{"常州", "101191101"}, new String[]{"温州", "101210601"},
        new String[]{"嘉兴", "101210501"}, new String[]{"台州", "101210801"}, new String[]{"金华", "101210901"},
        new String[]{"绍兴", "101210701"}, new String[]{"湖州", "101211001"}, new String[]{"丽水", "101211101"},
        new String[]{"舟山", "101211201"}, new String[]{"衢州", "101210301"}, new String[]{"泰州", "101191301"},
        new String[]{"镇江", "101191201"}, new String[]{"盐城", "101190701"}, new String[]{"扬州", "101190601"},
        new String[]{"徐州", "101190301"}, new String[]{"淮安", "101190801"}, new String[]{"连云港", "101191001"},
        new String[]{"宿迁", "101190901"}, new String[]{"南通", "101190501"}, new String[]{"唐山", "101090201"},
        new String[]{"秦皇岛", "101091101"}, new String[]{"保定", "101090301"}, new String[]{"邯郸", "101090401"},
        new String[]{"邢台", "101090501"}, new String[]{"张家口", "101090601"}, new String[]{"承德", "101090701"},
        new String[]{"廊坊", "101090801"}, new String[]{"沧州", "101090901"}, new String[]{"衡水", "101091001"},
        new String[]{"烟台", "101120501"}, new String[]{"威海", "101121001"}, new String[]{"济宁", "101120701"},
        new String[]{"泰安", "101120801"}, new String[]{"临沂", "101120901"}, new String[]{"菏泽", "101121101"},
        new String[]{"滨州", "101121201"}, new String[]{"东营", "101121301"}, new String[]{"潍坊", "101120601"},
        new String[]{"淄博", "101120301"}, new String[]{"枣庄", "101120401"}, new String[]{"聊城", "101121001"},
        new String[]{"德州", "101121101"}, new String[]{"临沂", "101120901"}, new String[]{"日照", "101121201"}
    );

    // 城市搜索（本地搜索）
    public static void searchCity(String keyword, Callback callback) {
        new Thread(() -> {
            try {
                JSONObject result = new JSONObject();
                JSONArray locations = new JSONArray();
                
                // 本地搜索匹配
                for (String[] city : CITIES_DATABASE) {
                    String cityName = city[0];
                    String cityId = city[1];
                    
                    // 支持中文和拼音搜索（简单实现）
                    if (cityName.contains(keyword) || 
                        getPinyin(cityName).toLowerCase().contains(keyword.toLowerCase())) {
                        JSONObject cityObj = new JSONObject();
                        cityObj.put("id", cityId);
                        cityObj.put("name", cityName);
                        locations.put(cityObj);
                        
                        // 限制搜索结果数量
                        if (locations.length() >= 20) break;
                    }
                }
                
                result.put("location", locations);
                
                // 模拟网络响应
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.postDelayed(() -> {
                    try {
                        String responseBody = result.toString();
                        Response mockResponse = new Response.Builder()
                            .request(new Request.Builder().url("http://localhost/mock").build())
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(200)
                            .message("OK")
                            .body(ResponseBody.create(responseBody, okhttp3.MediaType.parse("application/json")))
                            .build();
                        callback.onResponse(null, mockResponse);
                    } catch (Exception e) {
                        callback.onFailure(null, new IOException("本地搜索失败: " + e.getMessage()));
                    }
                }, 200); // 模拟200ms网络延迟
                
            } catch (Exception e) {
                callback.onFailure(null, new IOException("本地搜索失败: " + e.getMessage()));
            }
        }).start();
    }
    
    // 简单的拼音转换（仅实现常用城市）
    private static String getPinyin(String chinese) {
        return chinese.replace("北京", "beijing")
                     .replace("上海", "shanghai")
                     .replace("广州", "guangzhou")
                     .replace("深圳", "shenzhen")
                     .replace("天津", "tianjin")
                     .replace("重庆", "chongqing")
                     .replace("成都", "chengdu")
                     .replace("杭州", "hangzhou")
                     .replace("南京", "nanjing")
                     .replace("武汉", "wuhan")
                     .replace("西安", "xian")
                     .replace("苏州", "suzhou")
                     .replace("青岛", "qingdao")
                     .replace("厦门", "xiamen")
                     .replace("长沙", "changsha")
                     .replace("大连", "dalian")
                     .replace("福州", "fuzhou")
                     .replace("宁波", "ningbo")
                     .replace("昆明", "kunming")
                     .replace("沈阳", "shenyang");
    }

    // 经纬度反查城市
    public static void geoCity(double lat, double lon, Callback callback) {
        String url = BASE_URL + "/geo/v2/city/lookup?key=" + KEY + "&location=" + lon + "," + lat;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    // 获取7天天气（替换为 itboy.net 公共API）
    public static void getWeather7d(String locationId, Callback callback) {
        String url = "http://t.weather.itboy.net/api/weather/city/" + locationId;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    // 获取实时天气（同样用 itboy.net 公共API，返回结构一致）
    public static void getWeatherNow(String locationId, Callback callback) {
        String url = "http://t.weather.itboy.net/api/weather/city/" + locationId;
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }
    
    // 获取逐时天气（使用心知天气API）
    public static void getHourlyWeather(String cityName, Callback callback) {
        try {
            String encodedLocation = URLEncoder.encode(cityName, "UTF-8");
            String url = SENIVERSE_BASE_URL + "/weather/hourly.json?key=" + SENIVERSE_KEY + 
                        "&location=" + encodedLocation + "&language=zh-Hans&unit=c&start=0&hours=24";
            
            Log.d("WeatherAPI", "心知天气逐时API URL: " + url);
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            Log.e("WeatherAPI", "构建逐时天气请求失败: " + e.getMessage());
            callback.onFailure(null, new IOException("获取逐时天气失败: " + e.getMessage()));
        }
    }
} 