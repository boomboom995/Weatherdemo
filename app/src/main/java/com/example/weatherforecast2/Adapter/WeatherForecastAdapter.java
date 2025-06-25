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
import java.util.List;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.ForecastViewHolder> {
    private final Context context;
    private final JSONArray forecastArray;

    public WeatherForecastAdapter(Context context, JSONArray forecastArray) {
        this.context = context;
        this.forecastArray = forecastArray;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        try {
            JSONObject day = forecastArray.getJSONObject(position);
            holder.tvDate.setText(day.optString("date", ""));
            holder.tvWeatherType.setText(day.optString("type", ""));
            holder.tvTempRange.setText(day.optString("low", "") + " ~ " + day.optString("high", ""));
            holder.ivWeatherIcon.setImageResource(getWeatherIconResource(day.optString("type", "")));
            holder.itemView.setOnClickListener(v -> showDetailDialog(day));
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public int getItemCount() {
        return forecastArray.length();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeatherType, tvTempRange;
        ImageView ivWeatherIcon;
        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeatherType = itemView.findViewById(R.id.tvWeatherType);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
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

    private void showDetailDialog(JSONObject day) {
        StringBuilder sb = new StringBuilder();
        sb.append("日期: ").append(day.optString("ymd", "")).append("\n");
        sb.append("星期: ").append(day.optString("week", "")).append("\n");
        sb.append("天气: ").append(day.optString("type", "")).append("\n");
        sb.append("温度: ").append(day.optString("low", "")).append(" ~ ").append(day.optString("high", "")).append("\n");
        sb.append("风向: ").append(day.optString("fx", "")).append(" ").append(day.optString("fl", "")).append("\n");
        sb.append("空气质量: ").append(day.optString("aqi", "")).append("\n");
        sb.append("提示: ").append(day.optString("notice", "")).append("\n");
        new AlertDialog.Builder(context)
                .setTitle("天气详情")
                .setMessage(sb.toString())
                .setIcon(getWeatherIconResource(day.optString("type", "")))
                .setPositiveButton("关闭", null)
                .show();
    }
} 