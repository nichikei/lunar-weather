package com.example.weatherapp.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.data.models.CityWeather;
import com.example.weatherapp.presentation.state.UIState;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for SearchActivity
 * Manages search state and popular cities list
 */
public class SearchViewModel extends ViewModel {
    
    private final MutableLiveData<UIState<List<CityWeather>>> citiesState = new MutableLiveData<>();
    private final MutableLiveData<UIState<LocationData>> locationState = new MutableLiveData<>();
    
    public LiveData<UIState<List<CityWeather>>> getCitiesState() {
        return citiesState;
    }
    
    public LiveData<UIState<LocationData>> getLocationState() {
        return locationState;
    }
    
    /**
     * Load popular cities list
     */
    public void loadPopularCities() {
        citiesState.setValue(new UIState.Loading<>());
        
        // Simulate loading popular cities
        List<CityWeather> cities = getPopularCitiesList();
        citiesState.setValue(new UIState.Success<>(cities));
    }
    
    /**
     * Validate and process city search
     */
    public boolean validateCitySearch(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            return false;
        }
        return true;
    }
    
    /**
     * Set location data
     */
    public void setLocation(double latitude, double longitude) {
        locationState.setValue(new UIState.Success<>(new LocationData(latitude, longitude)));
    }
    
    /**
     * Set location error
     */
    public void setLocationError(String message) {
        locationState.setValue(new UIState.Error<>(message));
    }
    
    /**
     * Get popular cities list
     * In real app, this could come from Repository/UseCase
     */
    private List<CityWeather> getPopularCitiesList() {
        List<CityWeather> cities = new ArrayList<>();
        
        cities.add(new CityWeather("Montreal", "Canada", "Mid Rain",
                19, 24, 18,
                com.example.weatherapp.R.drawable.moon_cloud_mid_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("Toronto", "Canada", "Fast Wind",
                20, 21, 15,
                com.example.weatherapp.R.drawable.moon_cloud_fast_wind,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("Tokyo", "Japan", "Showers",
                13, 16, 8,
                com.example.weatherapp.R.drawable.sun_cloud_angled_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("Singapore", "Singapore", "Partly Cloudy",
                31, 36, 26,
                com.example.weatherapp.R.drawable.sun_cloud_mid_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("Paris", "France", "Clear Sky",
                18, 22, 14,
                com.example.weatherapp.R.drawable.sun_cloud_little_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("New York", "United States", "Cloudy",
                22, 28, 18,
                com.example.weatherapp.R.drawable.tornado,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("London", "United Kingdom", "Rainy",
                15, 18, 12,
                com.example.weatherapp.R.drawable.moon_cloud_mid_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));

        cities.add(new CityWeather("Sydney", "Australia", "Sunny",
                25, 30, 20,
                com.example.weatherapp.R.drawable.sun_cloud_little_rain,
                com.example.weatherapp.R.drawable.weather_detail_card_background));
        
        return cities;
    }
    
    /**
     * Location data holder
     */
    public static class LocationData {
        private final double latitude;
        private final double longitude;
        
        public LocationData(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
    }
}
