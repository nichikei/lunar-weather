package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;

/**
 * UseCase for getting weather by city name
 * Encapsulates business logic for weather fetching
 */
public class GetWeatherByCityUseCase {
    private final WeatherRepository repository;
    
    public GetWeatherByCityUseCase(WeatherRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Execute the use case
     * @param cityName Name of the city
     * @param temperatureUnit Temperature unit preference
     * @param callback Callback to handle result
     */
    public void execute(String cityName, String temperatureUnit, Callback callback) {
        // Validate input
        if (cityName == null || cityName.trim().isEmpty()) {
            callback.onError("City name cannot be empty");
            return;
        }
        
        // Call repository
        repository.getWeatherByCity(cityName.trim(), temperatureUnit, 
            new WeatherRepository.WeatherCallback() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    callback.onSuccess(weatherData);
                }
                
                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
    }
    
    public interface Callback {
        void onSuccess(WeatherData weatherData);
        void onError(String message);
    }
}
