package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.repository.WeatherRepository;

/**
 * UseCase for getting weather forecast
 */
public class GetForecastUseCase {
    private final WeatherRepository repository;
    
    public GetForecastUseCase(WeatherRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Execute the use case
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param temperatureUnit Temperature unit preference
     * @param callback Callback to handle result
     */
    public void execute(double latitude, double longitude, String temperatureUnit, Callback callback) {
        // Validate coordinates
        if (latitude < -90 || latitude > 90) {
            callback.onError("Invalid latitude: must be between -90 and 90");
            return;
        }
        
        if (longitude < -180 || longitude > 180) {
            callback.onError("Invalid longitude: must be between -180 and 180");
            return;
        }
        
        // Call repository
        repository.getForecast(latitude, longitude, temperatureUnit,
            new WeatherRepository.ForecastCallback() {
                @Override
                public void onSuccess(ForecastData forecastData) {
                    callback.onSuccess(forecastData);
                }
                
                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
    }
    
    public interface Callback {
        void onSuccess(ForecastData forecastData);
        void onError(String message);
    }
}
