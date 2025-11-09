package com.example.weatherapp.ui.viewholders;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.data.models.FavoriteCity;

/**
 * ViewHolder cho City Weather items
 * Hiển thị thông tin thời tiết của từng thành phố
 */
public class CityWeatherViewHolder extends RecyclerView.ViewHolder {

    private final TextView tvCityName;
    private final TextView tvTemperature;
    private final TextView tvWeatherDesc;
    private final ImageButton btnRemove;

    public CityWeatherViewHolder(@NonNull View itemView) {
        super(itemView);
        tvCityName = itemView.findViewById(R.id.tvCityName);
        tvTemperature = itemView.findViewById(R.id.tvTemperature);
        tvWeatherDesc = itemView.findViewById(R.id.tvWeatherDesc);
        btnRemove = itemView.findViewById(R.id.btnRemove);
    }

    /**
     * Bind dữ liệu city vào views
     */
    public void bind(FavoriteCity city) {
        if (city == null) return;

        tvCityName.setText(city.getCityName());
        tvTemperature.setText(String.format("%.0f°", city.getCurrentTemp()));
        tvWeatherDesc.setText(city.getWeatherDescription());

        // Set weather icon based on condition
        // ivWeatherIcon.setImageResource(getWeatherIcon(city.getWeatherCondition()));
    }

    /**
     * Bind với click listeners
     */
    public void bind(FavoriteCity city, OnCityClickListener clickListener, OnDeleteClickListener deleteListener) {
        bind(city);

        if (clickListener != null) {
            itemView.setOnClickListener(v -> clickListener.onCityClick(city, getAdapterPosition()));
        }

        if (deleteListener != null && btnRemove != null) {
            btnRemove.setOnClickListener(v -> deleteListener.onDeleteClick(city, getAdapterPosition()));
        }
    }

    /**
     * Interface cho city click events
     */
    public interface OnCityClickListener {
        void onCityClick(FavoriteCity city, int position);
    }

    /**
     * Interface cho delete button click
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(FavoriteCity city, int position);
    }
}
