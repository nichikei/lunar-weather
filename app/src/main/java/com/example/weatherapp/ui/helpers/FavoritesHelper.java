package com.example.weatherapp.ui.helpers;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.R;
import com.example.weatherapp.domain.repository.FavoriteCitiesManager;
import com.example.weatherapp.ui.activities.MainActivity;
import com.example.weatherapp.ui.activities.FavoriteCitiesActivity;

/**
 * Helper class for managing favorite cities
 * Handles adding, removing, and updating favorite status
 * REFACTORED: Now uses domain model WeatherData instead of data layer WeatherResponse
 * Updated to support both FAB and regular View (iOS bottom nav)
 */
public class FavoritesHelper {
    private final MainActivity activity;
    private final FavoriteCitiesManager favoritesManager;
    private final View favoriteButton;
    
    public FavoritesHelper(MainActivity activity, FavoriteCitiesManager favoritesManager, 
                          View favoriteButton) {
        this.activity = activity;
        this.favoritesManager = favoritesManager;
        this.favoriteButton = favoriteButton;
    }
    
    /**
     * Toggle favorite status of a city
     * @param cityName City name
     * @param weatherData WeatherData from domain layer
     * @param lat Latitude
     * @param lon Longitude
     */
    public void toggleFavorite(String cityName, WeatherData weatherData, 
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
    private void addFavorite(String cityName, WeatherData weatherData, 
                            double lat, double lon) {
        String country = "";
        if (weatherData != null) {
            country = weatherData.getCountryCode();
        }

        FavoriteCity favoriteCity = new FavoriteCity(cityName, country, lat, lon);

        // Set weather information if available
        if (weatherData != null) {
            favoriteCity.setCurrentTemp(weatherData.getTemperature());
            favoriteCity.setWeatherCondition(weatherData.getWeatherMain());
            favoriteCity.setWeatherDescription(weatherData.getWeatherDescription());
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
     * Update favorite button icon based on favorite status
     */
    public void updateFavoriteIcon(String cityName) {
        if (favoriteButton != null && cityName != null) {
            boolean isFavorite = favoritesManager.isFavorite(cityName);
            updateFavoriteIcon(isFavorite);
        }
    }
    
    /**
     * Update favorite button icon (works with FAB or ImageView)
     */
    private void updateFavoriteIcon(boolean isFavorite) {
        if (favoriteButton == null) {
            return;
        }
        
        // Try to find ImageView within the button (for iOS bottom nav)
        ImageView imageView = null;
        
        if (favoriteButton instanceof ImageView) {
            imageView = (ImageView) favoriteButton;
        } else if (favoriteButton instanceof android.view.ViewGroup) {
            // First try to find by ID
            imageView = favoriteButton.findViewById(R.id.ivFavoriteIcon);
            if (imageView == null) {
                // Fallback: search for any ImageView child
                imageView = findImageViewInView(favoriteButton);
            }
        }
        
        // Only update if we found a valid ImageView
        if (imageView != null) {
            try {
                imageView.setImageResource(
                    isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_line
                );
            } catch (ClassCastException e) {
                android.util.Log.e("FavoritesHelper", "Failed to update favorite icon", e);
            }
        } else {
            android.util.Log.w("FavoritesHelper", "No ImageView found in favoriteButton to update icon");
        }
    }
    
    /**
     * Helper to find ImageView in a View hierarchy
     */
    private ImageView findImageViewInView(View view) {
        if (view instanceof ImageView) {
            return (ImageView) view;
        }
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ImageView found = findImageViewInView(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
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
