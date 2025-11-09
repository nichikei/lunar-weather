package com.example.weatherapp.ui.helpers;

import android.content.Intent;
import android.widget.Toast;

import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.R;
import com.example.weatherapp.ui.activities.MainActivity;
import com.example.weatherapp.ui.activities.FavoriteCitiesActivity;
import com.example.weatherapp.utils.FavoriteCitiesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Helper class for managing favorite cities
 * Handles adding, removing, and updating favorite status
 */
public class FavoritesHelper {
    private final MainActivity activity;
    private final FavoriteCitiesManager favoritesManager;
    private final FloatingActionButton fabButton;
    
    public FavoritesHelper(MainActivity activity, FavoriteCitiesManager favoritesManager, 
                          FloatingActionButton fabButton) {
        this.activity = activity;
        this.favoritesManager = favoritesManager;
        this.fabButton = fabButton;
    }
    
    /**
     * Toggle favorite status of a city
     */
    public void toggleFavorite(String cityName, WeatherResponse weatherData, 
                               double lat, double lon) {
        if (cityName == null || cityName.isEmpty()) {
            Toast.makeText(activity, "No city loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFavorite = favoritesManager.isFavorite(cityName);

        if (isFavorite) {
            removeFavorite(cityName);
        } else {
            addFavorite(cityName, weatherData, lat, lon);
        }
    }
    
    /**
     * Add city to favorites
     */
    private void addFavorite(String cityName, WeatherResponse weatherData, 
                            double lat, double lon) {
        String country = "";
        if (weatherData != null && weatherData.getSys() != null) {
            country = weatherData.getSys().getCountry();
        }

        FavoriteCity favoriteCity = new FavoriteCity(cityName, country, lat, lon);

        // Set weather information if available
        if (weatherData != null) {
            favoriteCity.setCurrentTemp(weatherData.getMain().getTemp());
            if (!weatherData.getWeather().isEmpty()) {
                favoriteCity.setWeatherCondition(weatherData.getWeather().get(0).getMain());
                favoriteCity.setWeatherDescription(weatherData.getWeather().get(0).getDescription());
            }
        }

        boolean added = favoritesManager.addFavoriteCity(favoriteCity);
        if (added) {
            updateFavoriteIcon(true);
            Toast.makeText(activity, cityName + " added to favorites", Toast.LENGTH_SHORT).show();
            showFavoritesDialog(cityName);
        } else {
            handleAddFailure();
        }
    }
    
    /**
     * Remove city from favorites
     */
    private void removeFavorite(String cityName) {
        boolean removed = favoritesManager.removeFavoriteCity(cityName);
        if (removed) {
            updateFavoriteIcon(false);
            Toast.makeText(activity, cityName + " removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Update FAB icon based on favorite status
     */
    public void updateFavoriteIcon(String cityName) {
        if (fabButton != null && cityName != null) {
            boolean isFavorite = favoritesManager.isFavorite(cityName);
            updateFavoriteIcon(isFavorite);
        }
    }
    
    /**
     * Update FAB icon
     */
    private void updateFavoriteIcon(boolean isFavorite) {
        if (fabButton != null) {
            fabButton.setImageResource(
                isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_line
            );
        }
    }
    
    /**
     * Open favorites list activity
     */
    public void openFavoritesList() {
        Intent intent = new Intent(activity, FavoriteCitiesActivity.class);
        activity.startActivity(intent);
    }
    
    /**
     * Show dialog asking to view favorites
     */
    private void showFavoritesDialog(String cityName) {
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Added to Favorites")
                .setMessage(cityName + " has been added to your favorite cities. Would you like to view your favorites list?")
                .setPositiveButton("View Favorites", (dialog, which) -> {
                    openFavoritesList();
                })
                .setNegativeButton("Continue", null)
                .show();
    }
    
    /**
     * Handle add failure
     */
    private void handleAddFailure() {
        if (favoritesManager.getFavoriteCitiesCount() >= 10) {
            Toast.makeText(activity, "Maximum 10 favorite cities allowed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "City already in favorites", Toast.LENGTH_SHORT).show();
        }
    }
}
