package com.example.weatherapp.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.ui.activities.IOSWeatherActivity.DailyForecast;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for iOS Style Daily Forecast
 */
public class IOSDailyForecastAdapter extends RecyclerView.Adapter<IOSDailyForecastAdapter.ViewHolder> {

    private List<DailyForecast> data = new ArrayList<>();

    public void setData(List<DailyForecast> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_daily_forecast_ios, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailyForecast item = data.get(position);
        
        holder.tvDayName.setText(item.day);
        holder.tvLowTemp.setText(item.lowTemp + "°");
        holder.tvHighTemp.setText(item.highTemp + "°");
        
        // Show rain probability if needed
        if (item.rainProbability > 0) {
            holder.tvRainProbability.setVisibility(View.VISIBLE);
            holder.tvRainProbability.setText(item.rainProbability + "%");
        } else {
            holder.tvRainProbability.setVisibility(View.GONE);
        }
        
        // Hide divider for last item
        if (position == getItemCount() - 1) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }
        
        // Adjust temperature bar based on temperature range
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.viewTempBar.getLayoutParams();
        
        // Calculate temperature range (assuming min 10°, max 35°)
        float minTemp = 10f;
        float maxTemp = 35f;
        float range = maxTemp - minTemp;
        
        float lowPercent = (item.lowTemp - minTemp) / range;
        float highPercent = (item.highTemp - minTemp) / range;
        
        // Convert to margin (100dp bar width)
        int barWidth = 100; // dp
        params.leftMargin = (int) (lowPercent * barWidth);
        params.rightMargin = (int) ((1 - highPercent) * barWidth);
        
        holder.viewTempBar.setLayoutParams(params);
        
        // Set weather icon (using text for SF Symbols)
        // In a real app, you would use actual weather icons
        holder.ivWeatherIcon.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        ImageView ivWeatherIcon;
        TextView tvLowTemp;
        TextView tvHighTemp;
        TextView tvRainProbability;
        View viewTempBar;
        View divider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            tvLowTemp = itemView.findViewById(R.id.tvLowTemp);
            tvHighTemp = itemView.findViewById(R.id.tvHighTemp);
            tvRainProbability = itemView.findViewById(R.id.tvRainProbability);
            viewTempBar = itemView.findViewById(R.id.viewTempBar);
            divider = itemView.findViewById(R.id.divider);
        }
    }
}
