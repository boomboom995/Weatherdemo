package com.example.weatherforecast2.Adapter;

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

public class HourlyWeatherAdapter extends RecyclerView.Adapter<HourlyWeatherAdapter.HourlyViewHolder> {
    private final Context context;
    private final JSONArray hourlyArray;

    public HourlyWeatherAdapter(Context context, JSONArray hourlyArray) {
        this.context = context;
        this.hourlyArray = hourlyArray;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hourly_weather, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        try {
            JSONObject hour = hourlyArray.getJSONObject(position);
            
            // 时间格式化（如：08:00）
            String time = hour.optString("time", "");
            if (time.length() >= 2) {
                time = time.substring(0, 2) + ":00";
            }
            holder.tvTime.setText(time);
            
            // 温度
            String temp = hour.optString("temp", "0");
            holder.tvTemp.setText(temp + "°");
            
            // 天气类型
            String weather = hour.optString("weather", "晴");
            holder.tvWeather.setText(weather);
            
            // 天气图标
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(weather));
            
        } catch (Exception e) {
            // 默认值
            holder.tvTime.setText("--:--");
            holder.tvTemp.setText("--°");
            holder.tvWeather.setText("--");
            holder.ivWeatherIcon.setImageResource(R.drawable.ic_weather_sunny);
        }
    }

    @Override
    public int getItemCount() {
        return hourlyArray.length();
    }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp, tvWeather;
        ImageView ivWeatherIcon;
        
        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
        }
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
        } else if (weatherType.contains("夜")) {
            return R.drawable.ic_weather_night;
        }
        return R.drawable.ic_weather_sunny;
    }
} 