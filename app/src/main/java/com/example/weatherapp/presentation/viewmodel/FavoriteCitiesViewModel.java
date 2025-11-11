package com.example.weatherapp.presentation.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.model.UIState;
import com.example.weatherapp.domain.repository.FavoriteCitiesManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for FavoriteCitiesActivity
 * Manages favorite cities list and weather data fetching
 */
public class FavoriteCitiesViewModel extends ViewModel {
    
    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";
    
    private final MutableLiveData<UIState<List<FavoriteCityWithWeather>>> favoriteCitiesState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canAddMoreCities = new MutableLiveData<>();
    
    private FavoriteCitiesManager favoritesManager;
    private WeatherApiService weatherApiService;
    
    public LiveData<UIState<List<FavoriteCityWithWeather>>> getFavoriteCitiesState() {
        return favoriteCitiesState;
    }
    
    public LiveData<Boolean> getCanAddMoreCities() {
        return canAddMoreCities;
    }
    
    /**
     * Initialize manager and service
     */
    public void init(Context context) {
        this.favoritesManager = new FavoriteCitiesManager(context);
        this.weatherApiService = RetrofitClient.getInstance().getWeatherApi();
        canAddMoreCities.setValue(favoritesManager.canAddMoreCities());
    }
    
    /**
     * Load favorite cities with weather data
     */
    public void loadFavoriteCities() {
        List<FavoriteCity> favoriteCities = favoritesManager.getFavoriteCities();
        
        if (favoriteCities.isEmpty()) {
            favoriteCitiesState.setValue(UIState.success(new ArrayList<>()));
            return;
        }
        
        favoriteCitiesState.setValue(UIState.loading());
        
        List<FavoriteCityWithWeather> citiesWithWeather = new ArrayList<>();
        int[] pendingRequests = {favoriteCities.size()};
        
        for (FavoriteCity city : favoriteCities) {
            fetchWeatherForCity(city, citiesWithWeather, pendingRequests);
        }
    }
    
    /**
     * Fetch weather data for a single city
     */
    private void fetchWeatherForCity(FavoriteCity city, 
                                     List<FavoriteCityWithWeather> citiesWithWeather, 
                                     int[] pendingRequests) {
        weatherApiService.getWeatherByCity(city.getCityName(), API_KEY, "metric")
            .enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        citiesWithWeather.add(new FavoriteCityWithWeather(city, response.body()));
                    }
                    
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        favoriteCitiesState.postValue(UIState.success(citiesWithWeather));
                    }
                }
                
                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    pendingRequests[0]--;
                    if (pendingRequests[0] == 0) {
                        if (citiesWithWeather.isEmpty()) {
                            favoriteCitiesState.postValue(UIState.error("Failed to load weather data"));
                        } else {
                            favoriteCitiesState.postValue(UIState.success(citiesWithWeather));
                        }
                    }
                }
            });
    }
    
    /**
     * Remove a favorite city
     */
    public void removeFavoriteCity(FavoriteCity city) {
        favoritesManager.removeFavoriteCity(city.getCityName());
        canAddMoreCities.setValue(favoritesManager.canAddMoreCities());
        loadFavoriteCities();
    }
    
    /**
     * City with weather data holder
     */
    public static class FavoriteCityWithWeather {
        public final FavoriteCity city;
        public final WeatherResponse weatherData;
        
        public FavoriteCityWithWeather(FavoriteCity city, WeatherResponse weatherData) {
            this.city = city;
            this.weatherData = weatherData;
        }
    }
}
