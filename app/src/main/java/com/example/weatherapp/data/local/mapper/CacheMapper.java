package com.example.weatherapp.data.local.mapper;

import com.example.weatherapp.data.local.entity.WeatherCacheEntity;
import com.example.weatherapp.domain.model.WeatherData;

/**
 * Mapper between Room Entity and Domain Model
 */
public class CacheMapper {
    
    /**
     * Convert WeatherData domain model to cache entity
     */
    public static WeatherCacheEntity toEntity(WeatherData data) {
        if (data == null) {
            return null;
        }
        
        WeatherCacheEntity entity = new WeatherCacheEntity();
        entity.setCityName(data.getCityName());
        entity.setCountryCode(data.getCountryCode());
        entity.setTemperature(data.getTemperature());
        entity.setFeelsLike(data.getFeelsLike());
        entity.setMinTemperature(data.getMinTemperature());
        entity.setMaxTemperature(data.getMaxTemperature());
        entity.setHumidity(data.getHumidity());
        entity.setPressure(data.getPressure());
        entity.setWindSpeed(data.getWindSpeed());
        entity.setWindDegree(data.getWindDegree());
        entity.setWeatherMain(data.getWeatherMain());
        entity.setWeatherDescription(data.getWeatherDescription());
        entity.setWeatherIcon(data.getWeatherIcon());
        entity.setCloudiness(data.getCloudiness());
        entity.setVisibility(data.getVisibility());
        entity.setSunrise(data.getSunrise());
        entity.setSunset(data.getSunset());
        entity.setTimestamp(data.getTimestamp());
        entity.setLatitude(data.getLatitude());
        entity.setLongitude(data.getLongitude());
        entity.setTemperatureUnit(data.getTemperatureUnit());
        
        return entity;
    }
    
    /**
     * Convert cache entity to WeatherData domain model
     */
    public static WeatherData toDomain(WeatherCacheEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return new WeatherData.Builder()
                .setCityName(entity.getCityName())
                .setCountryCode(entity.getCountryCode())
                .setTemperature(entity.getTemperature())
                .setFeelsLike(entity.getFeelsLike())
                .setMinTemperature(entity.getMinTemperature())
                .setMaxTemperature(entity.getMaxTemperature())
                .setHumidity(entity.getHumidity())
                .setPressure(entity.getPressure())
                .setWindSpeed(entity.getWindSpeed())
                .setWindDegree(entity.getWindDegree())
                .setWeatherMain(entity.getWeatherMain())
                .setWeatherDescription(entity.getWeatherDescription())
                .setWeatherIcon(entity.getWeatherIcon())
                .setCloudiness(entity.getCloudiness())
                .setVisibility(entity.getVisibility())
                .setSunrise(entity.getSunrise())
                .setSunset(entity.getSunset())
                .setTimestamp(entity.getTimestamp())
                .setLatitude(entity.getLatitude())
                .setLongitude(entity.getLongitude())
                .setTemperatureUnit(entity.getTemperatureUnit())
                .build();
    }
}
