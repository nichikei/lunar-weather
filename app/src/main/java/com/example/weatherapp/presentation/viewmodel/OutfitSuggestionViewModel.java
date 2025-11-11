package com.example.weatherapp.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.weatherapp.data.models.OutfitSuggestion;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.model.UIState;
import com.example.weatherapp.domain.services.OutfitSuggestionService;

import java.util.List;

/**
 * ViewModel for OutfitSuggestionActivity
 * Manages outfit suggestions from AI based on weather data
 */
public class OutfitSuggestionViewModel extends ViewModel {
    
    private final MutableLiveData<WeatherInfo> weatherInfoState = new MutableLiveData<>();
    private final MutableLiveData<UIState<List<OutfitSuggestion>>> outfitSuggestionsState = new MutableLiveData<>();
    
    private OutfitSuggestionService outfitService;
    
    public LiveData<WeatherInfo> getWeatherInfoState() {
        return weatherInfoState;
    }
    
    public LiveData<UIState<List<OutfitSuggestion>>> getOutfitSuggestionsState() {
        return outfitSuggestionsState;
    }
    
    /**
     * Initialize service
     */
    public void initService(OutfitSuggestionService service) {
        this.outfitService = service;
    }
    
    /**
     * Load weather info for display
     */
    public void loadWeatherInfo(WeatherResponse weatherData) {
        if (weatherData == null) {
            return;
        }
        
        double temp = weatherData.getMain().getTemp();
        String condition = weatherData.getWeather().get(0).getDescription();
        String cityName = weatherData.getName();
        String weatherMain = weatherData.getWeather().get(0).getMain().toLowerCase();
        
        WeatherInfo info = new WeatherInfo(cityName, temp, condition, weatherMain);
        weatherInfoState.setValue(info);
    }
    
    /**
     * Fetch outfit suggestions from AI
     */
    public void fetchOutfitSuggestions(WeatherResponse weatherData) {
        if (outfitService == null || weatherData == null) {
            outfitSuggestionsState.setValue(UIState.error("Service not initialized"));
            return;
        }
        
        outfitSuggestionsState.setValue(UIState.loading());
        
        outfitService.getOutfitSuggestions(weatherData, new OutfitSuggestionService.OutfitSuggestionCallback() {
            @Override
            public void onSuccess(List<OutfitSuggestion> suggestions, String source) {
                outfitSuggestionsState.postValue(UIState.success(suggestions));
            }
            
            @Override
            public void onError(String errorMessage) {
                outfitSuggestionsState.postValue(UIState.error(errorMessage));
            }
        });
    }
    
    /**
     * Weather info holder
     */
    public static class WeatherInfo {
        public final String cityName;
        public final double temperature;
        public final String condition;
        public final String weatherMain; // For icon
        
        public WeatherInfo(String cityName, double temperature, String condition, String weatherMain) {
            this.cityName = cityName;
            this.temperature = temperature;
            this.condition = condition;
            this.weatherMain = weatherMain;
        }
    }
}
