package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.repository.WeatherRepository;

/**
 * UseCase for getting air quality data
 */
public class GetAirQualityUseCase {
    private final WeatherRepository repository;
    
    public GetAirQualityUseCase(WeatherRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Execute the use case
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback to handle result
     */
    public void execute(double latitude, double longitude, Callback callback) {
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
        repository.getAirQuality(latitude, longitude,
            new WeatherRepository.AirQualityCallback() {
                @Override
                public void onSuccess(AirQualityData airQualityData) {
                    callback.onSuccess(airQualityData);
                }
                
                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
    }
    
    public interface Callback {
        void onSuccess(AirQualityData airQualityData);
        void onError(String message);
    }
}
