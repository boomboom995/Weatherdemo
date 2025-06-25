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
        View view = LayoutInflater.from(context).inflate(R.layout.item_trend_card, parent, false);
        return new TrendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendViewHolder holder, int position) {
        try {
            JSONObject day = forecastArray.getJSONObject(position);
            
            // 日期格式化，参考竞品图1
            String dateText = formatDate(day.optString("ymd", ""), day.optString("week", ""), position);
            holder.tvDate.setText(dateText);
            
            // 天气类型
            String type = day.optString("type", "晴");
            holder.tvWeatherType.setText(type);
            
            // 高温（顶部显示）
            String high = day.optString("high", "0").replaceAll("[^0-9]", "");
            holder.tvTempHigh.setText(high + "°");
            
            // 低温（底部显示）
            String low = day.optString("low", "0").replaceAll("[^0-9]", "");
            holder.tvTempLow.setText(low + "°");
            
            // 空气质量
            String aqi = day.optString("aqi", "良");
            holder.tvAqi.setText(aqi);
            
            // 天气图标
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(type));
            
        } catch (Exception e) {
            // 默认值
            holder.tvDate.setText("--");
            holder.tvWeatherType.setText("--");
            holder.tvTempHigh.setText("--°");
            holder.tvTempLow.setText("--°");
            holder.tvAqi.setText("--");
            holder.ivWeatherIcon.setImageResource(R.drawable.ic_weather_sunny);
        }
    }

    @Override
    public int getItemCount() {
        return forecastArray.length();
    }

    private String formatDate(String ymd, String week, int position) {
        if (position == 0) return "今天";
        if (position == 1) return "明天";
        
        // 解析日期显示月日格式
        if (ymd.length() >= 8) {
            String month = ymd.substring(4, 6);
            String day = ymd.substring(6, 8);
            return month + "月" + day + "日";
        }
        
        if (!week.isEmpty()) return week;
        return ymd;
    }

    static class TrendViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeatherType, tvTempHigh, tvTempLow, tvAqi;
        ImageView ivWeatherIcon;
        
        public TrendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeatherType = itemView.findViewById(R.id.tvWeatherType);
            tvTempHigh = itemView.findViewById(R.id.tvTempHigh);
            tvTempLow = itemView.findViewById(R.id.tvTempLow);
            tvAqi = itemView.findViewById(R.id.tvAqi);
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
} 