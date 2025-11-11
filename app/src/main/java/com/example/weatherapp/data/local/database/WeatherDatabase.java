package com.example.weatherapp.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.weatherapp.data.database.dao.AirQualityCacheDao;
import com.example.weatherapp.data.database.dao.ForecastCacheDao;
import com.example.weatherapp.data.database.dao.UVIndexCacheDao;
import com.example.weatherapp.data.database.entities.AirQualityCacheEntity;
import com.example.weatherapp.data.database.entities.ForecastCacheEntity;
import com.example.weatherapp.data.database.entities.UVIndexCacheEntity;
import com.example.weatherapp.data.local.dao.WeatherDao;
import com.example.weatherapp.data.local.entity.WeatherCacheEntity;

/**
 * Room Database for caching weather data
 * Enables offline access to weather information
 * 
 * Version 2: Added ForecastCacheEntity, AirQualityCacheEntity, UVIndexCacheEntity
 */
@Database(
    entities = {
        WeatherCacheEntity.class,
        ForecastCacheEntity.class,
        AirQualityCacheEntity.class,
        UVIndexCacheEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class WeatherDatabase extends RoomDatabase {
    
    private static WeatherDatabase INSTANCE;
    private static final String DATABASE_NAME = "weather_database";
    
    /**
     * Get Weather DAO
     */
    public abstract WeatherDao weatherDao();
    
    /**
     * Get Forecast DAO
     */
    public abstract ForecastCacheDao forecastCacheDao();
    
    /**
     * Get Air Quality DAO
     */
    public abstract AirQualityCacheDao airQualityCacheDao();
    
    /**
     * Get UV Index DAO
     */
    public abstract UVIndexCacheDao uvIndexCacheDao();
    
    /**
     * Get singleton instance of database
     */
    public static synchronized WeatherDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                context.getApplicationContext(),
                WeatherDatabase.class,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return INSTANCE;
    }
    
    /**
     * Destroy instance (for testing)
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
