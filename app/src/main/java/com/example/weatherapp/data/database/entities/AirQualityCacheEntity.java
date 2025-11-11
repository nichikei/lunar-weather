package com.example.weatherapp.data.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room entity for caching air quality data
 * Stores AQI, CO, NO2, O3, PM2.5, PM10 levels
 */
@Entity(tableName = "air_quality_cache")
public class AirQualityCacheEntity {
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    @ColumnInfo(name = "latitude")
    private double latitude;
    
    @ColumnInfo(name = "longitude")
    private double longitude;
    
    @ColumnInfo(name = "aqi")
    private int aqi; // Air Quality Index (1-5)
    
    @ColumnInfo(name = "co")
    private double co; // Carbon monoxide (μg/m3)
    
    @ColumnInfo(name = "no")
    private double no; // Nitrogen monoxide (μg/m3)
    
    @ColumnInfo(name = "no2")
    private double no2; // Nitrogen dioxide (μg/m3)
    
    @ColumnInfo(name = "o3")
    private double o3; // Ozone (μg/m3)
    
    @ColumnInfo(name = "so2")
    private double so2; // Sulphur dioxide (μg/m3)
    
    @ColumnInfo(name = "pm2_5")
    private double pm2_5; // Fine particles matter (μg/m3)
    
    @ColumnInfo(name = "pm10")
    private double pm10; // Coarse particulate matter (μg/m3)
    
    @ColumnInfo(name = "nh3")
    private double nh3; // Ammonia (μg/m3)
    
    @ColumnInfo(name = "cached_at")
    private long cachedAt; // When this data was cached
    
    // Constructors
    public AirQualityCacheEntity() {
    }
    
    @Ignore
    public AirQualityCacheEntity(double latitude, double longitude, int aqi,
                                double co, double no, double no2, double o3,
                                double so2, double pm2_5, double pm10, double nh3,
                                long cachedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.aqi = aqi;
        this.co = co;
        this.no = no;
        this.no2 = no2;
        this.o3 = o3;
        this.so2 = so2;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
        this.nh3 = nh3;
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
    
    public int getAqi() {
        return aqi;
    }
    
    public void setAqi(int aqi) {
        this.aqi = aqi;
    }
    
    public double getCo() {
        return co;
    }
    
    public void setCo(double co) {
        this.co = co;
    }
    
    public double getNo() {
        return no;
    }
    
    public void setNo(double no) {
        this.no = no;
    }
    
    public double getNo2() {
        return no2;
    }
    
    public void setNo2(double no2) {
        this.no2 = no2;
    }
    
    public double getO3() {
        return o3;
    }
    
    public void setO3(double o3) {
        this.o3 = o3;
    }
    
    public double getSo2() {
        return so2;
    }
    
    public void setSo2(double so2) {
        this.so2 = so2;
    }
    
    public double getPm2_5() {
        return pm2_5;
    }
    
    public void setPm2_5(double pm2_5) {
        this.pm2_5 = pm2_5;
    }
    
    public double getPm10() {
        return pm10;
    }
    
    public void setPm10(double pm10) {
        this.pm10 = pm10;
    }
    
    public double getNh3() {
        return nh3;
    }
    
    public void setNh3(double nh3) {
        this.nh3 = nh3;
    }
    
    public long getCachedAt() {
        return cachedAt;
    }
    
    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
