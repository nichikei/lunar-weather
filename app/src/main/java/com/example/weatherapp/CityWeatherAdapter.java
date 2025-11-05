package com.example.weatherapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {

    private List<CityWeather> cityList;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(CityWeather city);
    }

    public CityWeatherAdapter(List<CityWeather> cityList, OnCityClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CityWeather city = cityList.get(position);

        // Set city name and country
        holder.tvCityName.setText(city.getCityName());
        holder.tvCountry.setText(city.getCountry());
        holder.tvWeatherDesc.setText(city.getWeatherDescription());
        holder.tvTemperature.setText(String.format(Locale.getDefault(), "%d°", city.getTemperature()));
        holder.tvTempRange.setText(String.format(Locale.getDefault(),
                "H:%d°  L:%d°", city.getHighTemp(), city.getLowTemp()));

        // Set weather icon - always use resource ID
        holder.ivWeatherIcon.setImageResource(city.getIconResourceId());

        // Set gradient background for the card
        holder.cardBackground.setBackgroundResource(city.getGradientBackground());

        holder.itemView.setOnClickListener(v -> listener.onCityClick(city));
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        TextView tvCountry;
        TextView tvWeatherDesc;
        TextView tvTemperature;
        TextView tvTempRange;
        ImageView ivWeatherIcon;
        LinearLayout cardBackground;

        ViewHolder(View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            tvCountry = itemView.findViewById(R.id.tvCountry);
            tvWeatherDesc = itemView.findViewById(R.id.tvWeatherDesc);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            cardBackground = itemView.findViewById(R.id.cardBackground);
        }
    }
}
