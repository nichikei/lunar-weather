package com.example.weatherapp.domain.repository;

import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.model.WeatherData;

/**
 * Repository interface for Weather data operations
 * Defines contract for data operations - implementation in data layer
 * Follows Clean Architecture: Domain layer doesn't depend on data layer
 */
public interface WeatherRepository {
    
    /**
     * Get current weather by city name
     * @param cityName Name of the city
     * @param temperatureUnit Unit for temperature (celsius/fahrenheit)
     * @param callback Callback to handle result
     */
    void getWeatherByCity(String cityName, String temperatureUnit, WeatherCallback callback);
    
    /**
     * Get current weather by GPS coordinates
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param temperatureUnit Unit for temperature (celsius/fahrenheit)
     * @param callback Callback to handle result
     */
    void getWeatherByCoordinates(double latitude, double longitude, String temperatureUnit, WeatherCallback callback);
    
    /**
     * Get forecast data (hourly and daily)
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param temperatureUnit Unit for temperature (celsius/fahrenheit)
     * @param callback Callback to handle result
     */
    void getForecast(double latitude, double longitude, String temperatureUnit, ForecastCallback callback);
    
    /**
     * Get UV index for location
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback to handle result
     */
    void getUVIndex(double latitude, double longitude, UVIndexCallback callback);
    
    /**
     * Get air quality data for location
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param callback Callback to handle result
     */
    void getAirQuality(double latitude, double longitude, AirQualityCallback callback);
    
    // Callback interfaces
    interface WeatherCallback {
        void onSuccess(WeatherData weatherData);
        void onError(String message);
    }
    
    interface ForecastCallback {
        void onSuccess(ForecastData forecastData);
        void onError(String message);
    }
    
    interface UVIndexCallback {
        void onSuccess(int uvIndex);
        void onError(String message);
    }
    
    interface AirQualityCallback {
        void onSuccess(AirQualityData airQualityData);
        void onError(String message);
    }
}
