package com.example.weatherapp.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for WeatherDetailsActivity
 * Manages weather detail display state
 */
public class WeatherDetailsViewModel extends ViewModel {
    
    private final MutableLiveData<WeatherDetailsState> weatherState = new MutableLiveData<>();
    
    public LiveData<WeatherDetailsState> getWeatherState() {
        return weatherState;
    }
    
    /**
     * Load weather details from intent data
     */
    public void loadWeatherDetails(String cityName, String temperature, 
                                   String condition, String tempRange) {
        WeatherDetailsState state = new WeatherDetailsState(
            cityName != null ? cityName : "Unknown City",
            temperature != null ? temperature : "--",
            condition != null ? condition : "Unknown",
            tempRange != null ? tempRange : "-- / --"
        );
        weatherState.setValue(state);
    }
    
    /**
     * Get current city name
     */
    public String getCurrentCityName() {
        WeatherDetailsState state = weatherState.getValue();
        return state != null ? state.cityName : "";
    }
    
    /**
     * State holder for weather details
     */
    public static class WeatherDetailsState {
        public final String cityName;
        public final String temperature;
        public final String condition;
        public final String tempRange;
        
        public WeatherDetailsState(String cityName, String temperature, 
                                  String condition, String tempRange) {
            this.cityName = cityName;
            this.temperature = temperature;
            this.condition = condition;
            this.tempRange = tempRange;
        }
    }
}
