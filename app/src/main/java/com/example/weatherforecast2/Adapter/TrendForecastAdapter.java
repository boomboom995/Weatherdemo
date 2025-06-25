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

public class TrendForecastAdapter extends RecyclerView.Adapter<TrendForecastAdapter.TrendViewHolder> {
    private final Context context;
    private final JSONArray forecastArray;

    public TrendForecastAdapter(Context context, JSONArray forecastArray) {
        this.context = context;
        this.forecastArray = forecastArray;
    }

    @NonNull
    @Override
    public TrendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trend_forecast, parent, false);
        return new TrendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendViewHolder holder, int position) {
        try {
            JSONObject day = forecastArray.getJSONObject(position);
            
            // 日期格式化
            String dateText = formatDate(day.optString("ymd", ""), day.optString("week", ""), position);
            holder.tvDate.setText(dateText);
            
            // 高温和低温
            String high = day.optString("high", "0").replaceAll("[^0-9]", "");
            String low = day.optString("low", "0").replaceAll("[^0-9]", "");
            holder.tvTempHigh.setText(high + "°");
            holder.tvTempLow.setText(low + "°");
            
            // 天气图标
            String type = day.optString("type", "晴");
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(type));
            
            // "今天"高亮效果
            if (position == 0) {
                holder.todayHighlight.setVisibility(View.VISIBLE);
                // 增强今天的文字颜色
                holder.tvDate.setTextColor(0xFFFFFFFF);
                holder.tvTempHigh.setTextColor(0xFFFFD54F);
                holder.tvTempLow.setTextColor(0xFF81D4FA);
            } else {
                holder.todayHighlight.setVisibility(View.GONE);
                // 普通颜色
                holder.tvDate.setTextColor(0xE0FFFFFF);
                holder.tvTempHigh.setTextColor(0xD0FFD54F);
                holder.tvTempLow.setTextColor(0xD081D4FA);
            }
            
        } catch (Exception e) {
            // 默认值
            holder.tvDate.setText("--");
            holder.tvTempHigh.setText("--°");
            holder.tvTempLow.setText("--°");
            holder.ivWeatherIcon.setImageResource(R.drawable.ic_weather_sunny);
            holder.todayHighlight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(forecastArray.length(), 15); // 限制最多显示15天
    }

    private String formatDate(String ymd, String week, int position) {
        if (position == 0) return "今天";
        if (position == 1) return "明天";
        if (position == 2) return "后天";
        
        // 解析日期显示月日格式
        if (ymd.length() >= 8) {
            String month = ymd.substring(4, 6);
            String day = ymd.substring(6, 8);
            // 去掉前导0
            int monthInt = Integer.parseInt(month);
            int dayInt = Integer.parseInt(day);
            return monthInt + "/" + dayInt;
        }
        
        if (!week.isEmpty()) return week;
        return ymd;
    }

    static class TrendViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTempHigh, tvTempLow;
        ImageView ivWeatherIcon;
        View todayHighlight;
        
        public TrendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTempHigh = itemView.findViewById(R.id.tvTempHigh);
            tvTempLow = itemView.findViewById(R.id.tvTempLow);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            todayHighlight = itemView.findViewById(R.id.todayHighlight);
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
} 