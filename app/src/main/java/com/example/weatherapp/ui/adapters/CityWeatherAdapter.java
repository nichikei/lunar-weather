package com.example.weatherapp.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.models.CityWeather;
import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.domain.repository.FavoriteCitiesManager;
import com.example.weatherapp.ui.activities.FavoriteCitiesActivity;
import com.example.weatherapp.ui.helpers.RecyclerViewItemAnimator;

import java.util.List;
import java.util.Locale;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {

    private static final String TAG = "CityWeatherAdapter";
    private final List<CityWeather> cityList;
    private final OnCityClickListener listener;
    private FavoriteCitiesManager favoriteCitiesManager;
    private RecyclerViewItemAnimator itemAnimator;
    private int lastPosition = -1;

    public interface OnCityClickListener {
        void onCityClick(CityWeather city);
    }

    public CityWeatherAdapter(List<CityWeather> cityList, OnCityClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
        this.itemAnimator = new RecyclerViewItemAnimator();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_city_weather, parent, false);
        // Initialize FavoriteCitiesManager
        if (favoriteCitiesManager == null) {
            favoriteCitiesManager = new FavoriteCitiesManager(parent.getContext());
        }
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

        // Update favorite button icon based on favorite status
        boolean isFavorite = favoriteCitiesManager.isFavorite(city.getCityName());
        Log.d(TAG, "City: " + city.getCityName() + " is favorite: " + isFavorite);

        if (isFavorite) {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_heart_line);
        }

        // Handle favorite button click
        holder.btnFavorite.setOnClickListener(v -> {
            Context context = v.getContext();
            boolean currentlyFavorite = favoriteCitiesManager.isFavorite(city.getCityName());

            if (currentlyFavorite) {
                // Remove from favorites
                boolean removed = favoriteCitiesManager.removeFavoriteCity(city.getCityName());
                if (removed) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_heart_line);
                    Log.d(TAG, "Removed " + city.getCityName() + " from favorites");
                    Toast.makeText(context,
                            city.getCityName() + " removed from favorites",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Add to favorites - use 0,0 as placeholder coordinates
                // Real coordinates will be fetched when weather data is loaded
                FavoriteCity favoriteCity = new FavoriteCity(
                        city.getCityName(),
                        city.getCountry(),
                        0.0,  // latitude placeholder
                        0.0   // longitude placeholder
                );

                // Set weather information
                favoriteCity.setCurrentTemp(city.getTemperature());
                favoriteCity.setWeatherCondition(city.getWeatherDescription());
                favoriteCity.setWeatherDescription(city.getWeatherDescription());

                boolean added = favoriteCitiesManager.addFavoriteCity(favoriteCity);
                if (added) {
                    holder.btnFavorite.setImageResource(R.drawable.ic_heart_filled);

                    // Log for debugging
                    Log.d(TAG, "Added " + city.getCityName() + " to favorites");
                    Log.d(TAG, "Total favorites now: " + favoriteCitiesManager.getFavoriteCitiesCount());
                    Log.d(TAG, "All favorites: " + favoriteCitiesManager.getFavoriteCities().toString());

                    Toast.makeText(context,
                            city.getCityName() + " added to favorites! Tap here to view",
                            Toast.LENGTH_LONG).show();

                    // Show dialog to open favorites
                    new android.app.AlertDialog.Builder(context)
                            .setTitle("Added to Favorites")
                            .setMessage(city.getCityName() + " has been added to your favorite cities. Would you like to view your favorites list?")
                            .setPositiveButton("View Favorites", (dialog, which) -> {
                                Intent intent = new Intent(context, FavoriteCitiesActivity.class);
                                context.startActivity(intent);
                            })
                            .setNegativeButton("Continue", null)
                            .show();
                } else {
                    // Check if it's because of max limit
                    if (favoriteCitiesManager.getFavoriteCitiesCount() >= 10) {
                        Toast.makeText(context,
                                "Maximum 10 favorite cities allowed",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context,
                                "City already in favorites",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Khi click vào card: gọi listener để hiển thị trên MainActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCityClick(city);
            }
        });

        // Apply smooth slide-in and fade-in animation
        animateItem(holder.itemView, position);
    }

    /**
     * Animate item with smooth slide and fade effect
     */
    private void animateItem(View view, int position) {
        if (position > lastPosition) {
            lastPosition = position;
            itemAnimator.animateSlideAndScale(view, position);
        } else {
            // Clear animation for recycled views
            RecyclerViewItemAnimator.clearAnimation(view);
        }
    }

    /**
     * Reset animation state (call when data changes)
     */
    public void resetAnimation() {
        lastPosition = -1;
        itemAnimator.reset();
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
        ImageButton btnFavorite;

        ViewHolder(View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            tvCountry = itemView.findViewById(R.id.tvCountry);
            tvWeatherDesc = itemView.findViewById(R.id.tvWeatherDesc);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvTempRange = itemView.findViewById(R.id.tvTempRange);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            cardBackground = itemView.findViewById(R.id.cardBackground);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
