package com.example.weatherapp.ui.navigation;

import android.content.Intent;
import android.widget.Toast;

import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.ui.activities.ChartsActivity;
import com.example.weatherapp.ui.activities.MainActivity;
import com.example.weatherapp.ui.activities.OutfitSuggestionActivity;

/**
 * Helper class for navigation between activities
 * Handles opening charts, outfit suggestions, and other screens
 */
public class NavigationHelper {
    private final MainActivity activity;
    
    public NavigationHelper(MainActivity activity) {
        this.activity = activity;
    }
    
    /**
     * Open Charts Activity with weather data
     */
    public void openChartsActivity(HourlyForecastResponse hourlyData, 
                                   WeatherResponse currentData,
                                   int uvIndex) {
        if (hourlyData == null || currentData == null) {
            Toast.makeText(activity, "Weather data not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(activity, ChartsActivity.class);
        intent.putExtra("hourly_data", hourlyData);
        intent.putExtra("current_data", currentData);
        intent.putExtra("uv_index", uvIndex);
        activity.startActivity(intent);
    }
    
    /**
     * Open Outfit Suggestion Activity
     */
    public void openOutfitSuggestionActivity(WeatherResponse weatherData) {
        if (weatherData == null) {
            Toast.makeText(activity, "Weather data not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(activity, OutfitSuggestionActivity.class);
        intent.putExtra("weather_data", weatherData);
        activity.startActivity(intent);
    }
}
