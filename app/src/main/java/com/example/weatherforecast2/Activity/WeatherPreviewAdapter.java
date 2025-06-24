package com.example.weatherforecast2.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherforecast2.R;
import java.util.List;

public class WeatherPreviewAdapter extends RecyclerView.Adapter<WeatherPreviewAdapter.PreviewViewHolder> {
    private List<WeatherPreviewItem> data;
    public WeatherPreviewAdapter(List<WeatherPreviewItem> data) {
        this.data = data;
    }
    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather_preview, parent, false);
        return new PreviewViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        WeatherPreviewItem item = data.get(position);
        holder.tvDate.setText(item.date);
        holder.tvWeather.setText(item.text);
        holder.tvTemp.setText(item.tempMin + "° / " + item.tempMax + "°");
        holder.ivWeather.setImageResource(getWeatherIconRes(item.text));
    }
    @Override
    public int getItemCount() {
        return data.size();
    }
    static class PreviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvWeather, tvTemp;
        ImageView ivWeather;
        public PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeather = itemView.findViewById(R.id.tvWeather);
            tvTemp = itemView.findViewById(R.id.tvTemp);
            ivWeather = itemView.findViewById(R.id.ivWeather);
        }
    }
    private int getWeatherIconRes(String weatherType) {
        if (weatherType == null) return R.drawable.ic_weather_sunny;
        if (weatherType.contains("晴")) {
            return R.drawable.ic_weather_sunny;
        } else if (weatherType.contains("多云")) {
            return R.drawable.ic_weather_cloudy;
        } else if (weatherType.contains("阴")) {
            return R.drawable.ic_weather_overcast;
//        } else if (weatherType.contains("雾")) {
//            return R.drawable.ic_weather_fog;
//        } else if (weatherType.contains("雷阵雨")) {
//            return R.drawable.ic_weather_thunderstorm;
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

class WeatherPreviewItem {
    public String date;
    public String text;
    public String tempMax;
    public String tempMin;
    public WeatherPreviewItem(String date, String text, String tempMax, String tempMin) {
        this.date = date;
        this.text = text;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
    }
} 