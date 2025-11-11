package com.example.weatherapp.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for caching UV index data
 * Stores UV index value and related information
 */
@Entity(tableName = "uv_index_cache")
public class UVIndexCacheEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "latitude")
    private double latitude;
    
    @ColumnInfo(name = "longitude")
    private double longitude;
    
    @ColumnInfo(name = "uv_index")
    private double uvIndex; // UV Index value (0-11+)
    
    @ColumnInfo(name = "uv_level")
    private String uvLevel; // "Low", "Moderate", "High", "Very High", "Extreme"
    
    @ColumnInfo(name = "timestamp")
    private long timestamp; // Time of UV index measurement
    
    @ColumnInfo(name = "cached_at")
    private long cachedAt; // When this data was cached
    
    // Constructors
    public UVIndexCacheEntity() {
    }
    
    public UVIndexCacheEntity(double latitude, double longitude, double uvIndex,
                             String uvLevel, long timestamp, long cachedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.uvIndex = uvIndex;
        this.uvLevel = uvLevel;
        this.timestamp = timestamp;
        this.cachedAt = cachedAt;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public double getUvIndex() {
        return uvIndex;
    }
    
    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }
    
    public String getUvLevel() {
        return uvLevel;
    }
    
    public void setUvLevel(String uvLevel) {
        this.uvLevel = uvLevel;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public long getCachedAt() {
        return cachedAt;
    }
    
    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
