package com.example.weatherapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherapp.data.local.entity.WeatherCacheEntity;

/**
 * Data Access Object for weather cache operations
 */
@Dao
public interface WeatherDao {
    
    /**
     * Insert or replace weather cache
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWeather(WeatherCacheEntity weather);
    
    /**
     * Get cached weather by city name
     */
    @Query("SELECT * FROM weather_cache WHERE cityName = :cityName LIMIT 1")
    WeatherCacheEntity getWeatherByCity(String cityName);
    
    /**
     * Get cached weather by coordinates (with tolerance)
     */
    @Query("SELECT * FROM weather_cache WHERE " +
           "ABS(latitude - :lat) < 0.01 AND ABS(longitude - :lon) < 0.01 " +
           "LIMIT 1")
    WeatherCacheEntity getWeatherByCoordinates(double lat, double lon);
    
    /**
     * Delete weather cache older than specified timestamp
     */
    @Query("DELETE FROM weather_cache WHERE cachedAt < :timestamp")
    void deleteOldCache(long timestamp);
    
    /**
     * Delete all cached weather
     */
    @Query("DELETE FROM weather_cache")
    void deleteAll();
    
    /**
     * Get all cached cities
     */
    @Query("SELECT * FROM weather_cache ORDER BY cachedAt DESC")
    java.util.List<WeatherCacheEntity> getAllCachedWeather();
}
