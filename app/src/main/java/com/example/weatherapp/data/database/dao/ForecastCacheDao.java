package com.example.weatherapp.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherapp.data.database.entities.ForecastCacheEntity;

import java.util.List;

/**
 * DAO for ForecastCacheEntity
 * Handles database operations for cached forecast data
 */
@Dao
public interface ForecastCacheDao {
    
    /**
     * Insert forecast data (replace if exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertForecast(ForecastCacheEntity forecast);
    
    /**
     * Insert multiple forecast entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertForecasts(List<ForecastCacheEntity> forecasts);
    
    /**
     * Get hourly forecasts for a city
     * @param cityName City name
     * @param forecastType "hourly" or "daily"
     * @param maxAge Maximum age in milliseconds (e.g., 10 minutes)
     * @return List of cached forecasts
     */
    @Query("SELECT * FROM forecast_cache " +
           "WHERE city_name = :cityName " +
           "AND forecast_type = :forecastType " +
           "AND ((:currentTime - cached_at) < :maxAge) " +
           "ORDER BY timestamp ASC")
    List<ForecastCacheEntity> getForecastsByCity(String cityName, String forecastType, 
                                                  long currentTime, long maxAge);
    
    /**
     * Get forecasts by coordinates
     */
    @Query("SELECT * FROM forecast_cache " +
           "WHERE ABS(latitude - :lat) < 0.1 AND ABS(longitude - :lon) < 0.1 " +
           "AND forecast_type = :forecastType " +
           "AND ((:currentTime - cached_at) < :maxAge) " +
           "ORDER BY timestamp ASC")
    List<ForecastCacheEntity> getForecastsByCoordinates(double lat, double lon, 
                                                         String forecastType,
                                                         long currentTime, long maxAge);
    
    /**
     * Delete old forecast cache (older than maxAge)
     */
    @Query("DELETE FROM forecast_cache WHERE (:currentTime - cached_at) > :maxAge")
    void deleteOldForecasts(long currentTime, long maxAge);
    
    /**
     * Delete forecasts for a specific city
     */
    @Query("DELETE FROM forecast_cache WHERE city_name = :cityName")
    void deleteForecastsByCity(String cityName);
    
    /**
     * Delete all forecast cache
     */
    @Query("DELETE FROM forecast_cache")
    void deleteAllForecasts();
}
