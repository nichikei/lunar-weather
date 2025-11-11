package com.example.weatherapp.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.weatherapp.data.database.entities.UVIndexCacheEntity;

/**
 * DAO for UVIndexCacheEntity
 * Handles database operations for cached UV index data
 */
@Dao
public interface UVIndexCacheDao {
    
    /**
     * Insert UV index data (replace if exists)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUVIndex(UVIndexCacheEntity uvIndex);
    
    /**
     * Get UV index data by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param currentTime Current timestamp
     * @param maxAge Maximum age in milliseconds (e.g., 10 minutes = 600000)
     * @return Cached UV index data or null
     */
    @Query("SELECT * FROM uv_index_cache " +
           "WHERE ABS(latitude - :lat) < 0.1 AND ABS(longitude - :lon) < 0.1 " +
           "AND ((:currentTime - cached_at) < :maxAge) " +
           "ORDER BY cached_at DESC " +
           "LIMIT 1")
    UVIndexCacheEntity getUVIndexByCoordinates(double lat, double lon, 
                                                long currentTime, long maxAge);
    
    /**
     * Delete old UV index cache (older than maxAge)
     */
    @Query("DELETE FROM uv_index_cache WHERE (:currentTime - cached_at) > :maxAge")
    void deleteOldUVIndex(long currentTime, long maxAge);
    
    /**
     * Delete all UV index cache
     */
    @Query("DELETE FROM uv_index_cache")
    void deleteAllUVIndex();
}
