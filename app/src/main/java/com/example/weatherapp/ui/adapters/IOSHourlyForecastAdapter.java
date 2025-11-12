package com.example.weatherapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.ui.activities.IOSWeatherActivity.HourlyForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for iOS Style Hourly Forecast
 */
public class IOSHourlyForecastAdapter extends RecyclerView.Adapter<IOSHourlyForecastAdapter.ViewHolder> {

    private List<HourlyForecast> data = new ArrayList<>();

    public void setData(List<HourlyForecast> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_hourly_forecast_ios, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HourlyForecast item = data.get(position);
        
        holder.tvTime.setText(item.time);
        holder.tvTemperature.setText(item.temperature + "°");
        
        // Set weather icon (using text for SF Symbols)
        // In a real app, you would use actual weather icons
        holder.ivWeatherIcon.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        ImageView ivWeatherIcon;
        TextView tvTemperature;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
        }
    }
}
