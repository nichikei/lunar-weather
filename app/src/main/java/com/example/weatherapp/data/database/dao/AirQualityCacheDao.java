package com.example.weatherapp.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherapp.data.database.entities.AirQualityCacheEntity;

/**
 * DAO for AirQualityCacheEntity
 * Handles database operations for cached air quality data
 */
@Dao
public interface AirQualityCacheDao {
    
    /**
     * Insert air quality data (replace if exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAirQuality(AirQualityCacheEntity airQuality);
    
    /**
     * Get air quality data by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param currentTime Current timestamp
     * @param maxAge Maximum age in milliseconds (e.g., 10 minutes = 600000)
     * @return Cached air quality data or null
     */
    @Query("SELECT * FROM air_quality_cache " +
           "WHERE ABS(latitude - :lat) < 0.1 AND ABS(longitude - :lon) < 0.1 " +
           "AND ((:currentTime - cached_at) < :maxAge) " +
           "ORDER BY cached_at DESC " +
           "LIMIT 1")
    AirQualityCacheEntity getAirQualityByCoordinates(double lat, double lon, 
                                                      long currentTime, long maxAge);
    
    /**
     * Delete old air quality cache (older than maxAge)
     */
    @Query("DELETE FROM air_quality_cache WHERE (:currentTime - cached_at) > :maxAge")
    void deleteOldAirQuality(long currentTime, long maxAge);
    
    /**
     * Delete all air quality cache
     */
    @Query("DELETE FROM air_quality_cache")
    void deleteAllAirQuality();
}
