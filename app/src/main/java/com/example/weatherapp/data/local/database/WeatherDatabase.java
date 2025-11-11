package com.example.weatherapp.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.weatherapp.data.local.dao.WeatherDao;
import com.example.weatherapp.data.local.entity.WeatherCacheEntity;

/**
 * Room Database for caching weather data
 * Enables offline access to weather information
 */
@Database(
    entities = {WeatherCacheEntity.class},
    version = 1,
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
