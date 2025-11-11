package com.example.weatherapp.domain.usecase;

import com.example.weatherapp.domain.repository.WeatherRepository;

/**
 * UseCase for getting UV index
 */
public class GetUVIndexUseCase {
    private final WeatherRepository repository;
    
    public GetUVIndexUseCase(WeatherRepository repository) {
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
        repository.getUVIndex(latitude, longitude,
            new WeatherRepository.UVIndexCallback() {
                @Override
                public void onSuccess(int uvIndex) {
                    callback.onSuccess(uvIndex);
                }
                
                @Override
                public void onError(String message) {
                    callback.onError(message);
                }
            });
    }
    
    public interface Callback {
        void onSuccess(int uvIndex);
        void onError(String message);
    }
}
