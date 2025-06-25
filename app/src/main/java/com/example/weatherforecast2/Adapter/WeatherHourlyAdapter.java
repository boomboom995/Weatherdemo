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

public class WeatherHourlyAdapter extends RecyclerView.Adapter<WeatherHourlyAdapter.HourlyViewHolder> {
    private final Context context;
    private final JSONArray hourlyArray;

    public WeatherHourlyAdapter(Context context, JSONArray hourlyArray) {
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
            String time = hour.optString("time", "");
            String hourStr = time.length() >= 16 ? time.substring(11, 16) : time;
            holder.tvHour.setText(hourStr);
            holder.tvWeather.setText(hour.optString("weather", ""));
            holder.tvTemp.setText(hour.optString("temp", "") + "°");
            holder.tvWind.setText(hour.optString("wind", ""));
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(hour.optString("weather", "")));
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
        sb.append("时间: ").append(hour.optString("time", "")).append("\n");
        sb.append("天气: ").append(hour.optString("weather", "")).append("\n");
        sb.append("温度: ").append(hour.optString("temp", "")).append("°\n");
        sb.append("风力: ").append(hour.optString("wind", "")).append("\n");
        new AlertDialog.Builder(context)
                .setTitle("小时天气详情")
                .setMessage(sb.toString())
                .setIcon(getWeatherIconResource(hour.optString("weather", "")))
                .setPositiveButton("关闭", null)
                .show();
    }
} 