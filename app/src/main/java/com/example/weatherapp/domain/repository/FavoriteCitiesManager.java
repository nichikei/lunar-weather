package com.example.weatherapp.domain.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.weatherapp.data.models.FavoriteCity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteCitiesManager {
    private static final String PREFS_NAME = "FavoriteCities";
    private static final String KEY_CITIES = "cities";
    private static final int MAX_FAVORITE_CITIES = 10;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public FavoriteCitiesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<FavoriteCity> getFavoriteCities() {
        String json = sharedPreferences.getString(KEY_CITIES, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<FavoriteCity>>(){}.getType();
        List<FavoriteCity> cities = gson.fromJson(json, type);
        return cities != null ? cities : new ArrayList<>();
    }

    public boolean addFavoriteCity(FavoriteCity city) {
        List<FavoriteCity> cities = getFavoriteCities();

        // Check if city already exists
        for (FavoriteCity existingCity : cities) {
            if (existingCity.getCityName().equalsIgnoreCase(city.getCityName())) {
                return false; // City already in favorites
            }
        }

        // Check max limit
        if (cities.size() >= MAX_FAVORITE_CITIES) {
            return false; // Max limit reached
        }

        cities.add(city);
        saveCities(cities);
        return true;
    }

    public boolean removeFavoriteCity(String cityName) {
        List<FavoriteCity> cities = getFavoriteCities();
        boolean removed = cities.removeIf(city -> city.getCityName().equalsIgnoreCase(cityName));

        if (removed) {
            saveCities(cities);
        }
        return removed;
    }

    public boolean isFavorite(String cityName) {
        List<FavoriteCity> cities = getFavoriteCities();
        for (FavoriteCity city : cities) {
            if (city.getCityName().equalsIgnoreCase(cityName)) {
                return true;
            }
        }
        return false;
    }

    public void updateCityWeather(String cityName, double temp, String condition, String description) {
        List<FavoriteCity> cities = getFavoriteCities();
        for (FavoriteCity city : cities) {
            if (city.getCityName().equalsIgnoreCase(cityName)) {
                city.setCurrentTemp(temp);
                city.setWeatherCondition(condition);
                city.setWeatherDescription(description);
                city.setLastUpdated(System.currentTimeMillis());
                break;
            }
        }
        saveCities(cities);
    }

    private void saveCities(List<FavoriteCity> cities) {
        String json = gson.toJson(cities);
        sharedPreferences.edit().putString(KEY_CITIES, json).apply();
    }

    public int getFavoriteCitiesCount() {
        return getFavoriteCities().size();
    }

    public boolean canAddMoreCities() {
        return getFavoriteCities().size() < MAX_FAVORITE_CITIES;
    }
}
