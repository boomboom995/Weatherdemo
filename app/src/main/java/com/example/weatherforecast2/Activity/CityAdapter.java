package com.example.weatherforecast2.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherforecast2.R;
import com.example.weatherforecast2.Activity.CityItem;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {
    public interface OnCityPreviewListener {
        void onPreview(String cityId, String cityName);
    }
    public interface OnCityAddListener {
        void onAdd(String cityId, String cityName);
    }
    private List<CityItem> data;
    private OnCityPreviewListener previewListener;
    private OnCityAddListener addListener;
    public CityAdapter(List<CityItem> data, OnCityPreviewListener previewListener, OnCityAddListener addListener) {
        this.data = data;
        this.previewListener = previewListener;
        this.addListener = addListener;
    }
    public void updateData(List<CityItem> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_search, parent, false);
        return new CityViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        CityItem item = data.get(position);
        holder.tvCity.setText(item.name);
        holder.itemView.setOnClickListener(v -> previewListener.onPreview(item.id, item.name));
        holder.ivAdd.setOnClickListener(v -> addListener.onAdd(item.id, item.name));
    }
    @Override
    public int getItemCount() {
        return data.size();
    }
    static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;
        ImageView ivAdd;
        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tvCity);
            ivAdd = itemView.findViewById(R.id.ivAdd);
        }
    }
} 