package com.example.weatherapp.data.mapper;

import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.model.WeatherData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper class to convert API responses to domain models
 * Follows Clean Architecture - separates data layer from domain layer
 */
public class DomainMapper {
    
    /**
     * Map WeatherResponse (API) to WeatherData (Domain)
     */
    public static WeatherData toWeatherData(WeatherResponse response, String temperatureUnit) {
        if (response == null) {
            return null;
        }
        
        WeatherData.Builder builder = new WeatherData.Builder()
                .setCityName(response.getName())
                .setCountryCode(response.getSys() != null ? response.getSys().getCountry() : "")
                .setTemperature(response.getMain() != null ? response.getMain().getTemp() : 0)
                .setFeelsLike(response.getMain() != null ? response.getMain().getFeelsLike() : 0)
                .setMinTemperature(response.getMain() != null ? response.getMain().getTempMin() : 0)
                .setMaxTemperature(response.getMain() != null ? response.getMain().getTempMax() : 0)
                .setHumidity(response.getMain() != null ? response.getMain().getHumidity() : 0)
                .setPressure(response.getMain() != null ? response.getMain().getPressure() : 0)
                .setWindSpeed(response.getWind() != null ? response.getWind().getSpeed() : 0)
                .setWindDegree(response.getWind() != null ? response.getWind().getDeg() : 0)
                .setCloudiness(response.getClouds() != null ? response.getClouds().getAll() : 0)
                .setVisibility(response.getVisibility())
                .setSunrise(response.getSys() != null ? response.getSys().getSunrise() : 0)
                .setSunset(response.getSys() != null ? response.getSys().getSunset() : 0)
                .setTimestamp(response.getDt())
                .setLatitude(response.getCoord() != null ? response.getCoord().getLat() : 0)
                .setLongitude(response.getCoord() != null ? response.getCoord().getLon() : 0)
                .setTemperatureUnit(temperatureUnit);
        
        // Weather condition
        if (response.getWeather() != null && !response.getWeather().isEmpty()) {
            WeatherResponse.Weather weather = response.getWeather().get(0);
            builder.setWeatherMain(weather.getMain())
                   .setWeatherDescription(weather.getDescription())
                   .setWeatherIcon(weather.getIcon());
        }
        
        // Rain volume (optional)
        if (response.getRain() != null) {
            builder.setRainVolume(response.getRain().get1h());
        }
        
        return builder.build();
    }
    
    /**
     * Map HourlyForecastResponse (API) to ForecastData (Domain)
     */
    public static ForecastData toForecastData(HourlyForecastResponse response) {
        if (response == null || response.getList() == null) {
            return null;
        }
        
        List<ForecastData.HourlyForecast> hourlyForecasts = new ArrayList<>();
        Map<String, DailyForecastAggregate> dailyAggregateMap = new HashMap<>();
        
        for (HourlyForecastResponse.HourlyItem item : response.getList()) {
            // Create hourly forecast
            ForecastData.HourlyForecast hourly = new ForecastData.HourlyForecast(
                    item.getDt(),
                    item.getMain() != null ? item.getMain().getTemp() : 0,
                    item.getWeather() != null && !item.getWeather().isEmpty() ? 
                            item.getWeather().get(0).getIcon() : "",
                    item.getWeather() != null && !item.getWeather().isEmpty() ? 
                            item.getWeather().get(0).getDescription() : "",
                    item.getMain() != null ? item.getMain().getHumidity() : 0,
                    item.getWind() != null ? item.getWind().getSpeed() : 0,
                    (int) (item.getPop() * 100),
                    item.getMain() != null ? item.getMain().getPressure() : 1013.0
            );
            hourlyForecasts.add(hourly);
            
            // Aggregate daily data
            String dateKey = getDayKey(item.getDt());
            DailyForecastAggregate aggregate = dailyAggregateMap.get(dateKey);
            if (aggregate == null) {
                aggregate = new DailyForecastAggregate(item.getDt());
                dailyAggregateMap.put(dateKey, aggregate);
            }
            
            // Update min/max temperatures
            double temp = item.getMain() != null ? item.getMain().getTemp() : 0;
            aggregate.updateTemperature(temp);
            
            // Use the most common weather icon for the day
            if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                aggregate.addWeatherIcon(item.getWeather().get(0).getIcon());
                aggregate.addWeatherDescription(item.getWeather().get(0).getDescription());
            }
            
            // Average rain probability
            aggregate.addRainProbability(item.getPop());
        }
        
        // Convert aggregates to daily forecasts
        List<ForecastData.DailyForecast> dailyForecasts = new ArrayList<>();
        for (DailyForecastAggregate aggregate : dailyAggregateMap.values()) {
            dailyForecasts.add(aggregate.toDailyForecast());
        }
        
        String cityName = response.getCity() != null ? response.getCity().getName() : "";
        return new ForecastData(hourlyForecasts, dailyForecasts, cityName);
    }
    
    /**
     * Map AirQualityResponse (API) to AirQualityData (Domain)
     */
    public static AirQualityData toAirQualityData(AirQualityResponse response) {
        if (response == null || response.getList() == null || response.getList().isEmpty()) {
            return null;
        }
        
        AirQualityResponse.AirQualityData apiData = response.getList().get(0);
        AirQualityResponse.Components components = apiData.getComponents();
        
        if (components == null) {
            return null;
        }
        
        return new AirQualityData(
                apiData.getMain().getAqi(),
                components.getCo(),
                components.getNo(),
                components.getNo2(),
                components.getO3(),
                components.getSo2(),
                components.getPm2_5(),
                components.getPm10(),
                components.getNh3()
        );
    }
    
    /**
     * Helper: Get day key from timestamp
     */
    private static String getDayKey(long timestamp) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTimeInMillis(timestamp * 1000);
        return calendar.get(java.util.Calendar.YEAR) + "-" + 
               calendar.get(java.util.Calendar.MONTH) + "-" + 
               calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }
    
    /**
     * Helper class to aggregate daily forecast data
     */
    private static class DailyForecastAggregate {
        private final long timestamp;
        private double tempMin = Double.MAX_VALUE;
        private double tempMax = Double.MIN_VALUE;
        private final List<String> weatherIcons = new ArrayList<>();
        private final List<String> weatherDescriptions = new ArrayList<>();
        private final List<Double> rainProbabilities = new ArrayList<>();
        
        DailyForecastAggregate(long timestamp) {
            this.timestamp = timestamp;
        }
        
        void updateTemperature(double temp) {
            if (temp < tempMin) tempMin = temp;
            if (temp > tempMax) tempMax = temp;
        }
        
        void addWeatherIcon(String icon) {
            weatherIcons.add(icon);
        }
        
        void addWeatherDescription(String description) {
            weatherDescriptions.add(description);
        }
        
        void addRainProbability(double pop) {
            rainProbabilities.add(pop);
        }
        
        ForecastData.DailyForecast toDailyForecast() {
            // Get most common icon
            String icon = weatherIcons.isEmpty() ? "" : weatherIcons.get(0);
            String description = weatherDescriptions.isEmpty() ? "" : weatherDescriptions.get(0);
            
            // Average rain probability
            double avgPop = 0;
            if (!rainProbabilities.isEmpty()) {
                for (double pop : rainProbabilities) {
                    avgPop += pop;
                }
                avgPop /= rainProbabilities.size();
            }
            
            return new ForecastData.DailyForecast(
                    timestamp,
                    tempMin == Double.MAX_VALUE ? 0 : tempMin,
                    tempMax == Double.MIN_VALUE ? 0 : tempMax,
                    icon,
                    description,
                    (int) (avgPop * 100)
            );
        }
    }
}
