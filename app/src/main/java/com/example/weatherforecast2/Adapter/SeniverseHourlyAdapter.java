package com.example.weatherforecast2.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherforecast2.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class SeniverseHourlyAdapter extends RecyclerView.Adapter<SeniverseHourlyAdapter.HourlyViewHolder> {
    private final Context context;
    private final JSONArray hourlyArray;

    public SeniverseHourlyAdapter(Context context, JSONArray hourlyArray) {
        this.context = context;
        this.hourlyArray = hourlyArray;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weather_hourly, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        try {
            JSONObject hour = hourlyArray.getJSONObject(position);
            
            // 心知天气API数据格式:
            // {
            //   "text": "晴",
            //   "code": "0",
            //   "temperature": "25",
            //   "time": "2023-06-25T15:00:00+08:00"
            // }
            
            String time = hour.optString("time", "");
            String hourStr = extractHour(time);
            String text = hour.optString("text", "");
            String temperature = hour.optString("temperature", "");
            
            holder.tvHour.setText(hourStr);
            holder.tvWeather.setText(text);
            holder.tvTemp.setText(temperature + "°");
            holder.tvWind.setText(""); // 心知天气逐时API不包含风力信息
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(text));
            
            holder.itemView.setOnClickListener(v -> showDetailDialog(hour));
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public int getItemCount() {
        return hourlyArray.length();
    }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvHour, tvWeather, tvTemp, tvWind;
        ImageView ivWeatherIcon;
        
        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHour = itemView.findViewById(R.id.tvHour);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWind = itemView.findViewById(R.id.tvWind);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
    }
    
    // 从时间字符串提取小时 "2023-06-25T15:00:00+08:00" -> "15:00"
    private String extractHour(String timeString) {
        try {
            if (timeString.contains("T")) {
                String timePart = timeString.split("T")[1];
                if (timePart.length() >= 5) {
                    return timePart.substring(0, 5); // "15:00"
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return timeString;
    }

    private int getWeatherIconResource(String weatherType) {
        if (weatherType == null) return R.drawable.ic_weather_sunny;
        if (weatherType.contains("晴")) {
            return R.drawable.ic_weather_sunny;
        } else if (weatherType.contains("多云") || weatherType.contains("阴")) {
            return R.drawable.ic_weather_cloudy;
        } else if (weatherType.contains("雨")) {
            return R.drawable.ic_weather_rainy;
        } else if (weatherType.contains("雪")) {
            return R.drawable.ic_weather_snowy;
        }
        return R.drawable.ic_weather_sunny;
    }

    private void showDetailDialog(JSONObject hour) {
        StringBuilder sb = new StringBuilder();
        sb.append("时间: ").append(extractHour(hour.optString("time", ""))).append("\n");
        sb.append("天气: ").append(hour.optString("text", "")).append("\n");
        sb.append("温度: ").append(hour.optString("temperature", "")).append("°\n");
        
        new AlertDialog.Builder(context)
                .setTitle("小时天气详情")
                .setMessage(sb.toString())
                .setIcon(getWeatherIconResource(hour.optString("text", "")))
                .setPositiveButton("关闭", null)
                .show();
    }
} 