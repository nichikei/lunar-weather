package com.example.weatherapp.ui.helpers;

import com.example.weatherapp.domain.model.ForecastData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates forecast summary text based on weather data
 * REFACTORED: Now uses domain model ForecastData.HourlyForecast instead of data layer HourlyForecastResponse
 */
public class ForecastSummaryGenerator {

    /**
     * Generate forecast summary from weather condition and forecast data
     * @param weatherCondition Current weather condition
     * @param hourlyForecasts List of HourlyForecast from domain layer
     * @return Summary text describing forecast
     */
    public static String generateSummary(String weatherCondition, List<ForecastData.HourlyForecast> hourlyForecasts) {
        if (hourlyForecasts != null && !hourlyForecasts.isEmpty()) {
            return generateFromForecastData(hourlyForecasts);
        }

        // Fallback to simple summary
        return generateFromWeatherCondition(weatherCondition);
    }

    private static String generateFromForecastData(List<ForecastData.HourlyForecast> hourlyForecasts) {
        Map<Integer, Integer> rainyItemsPerDay = new HashMap<>();
        Map<String, Integer> conditionCounts = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        for (ForecastData.HourlyForecast item : hourlyForecasts) {
            calendar.setTimeInMillis(item.getTimestamp() * 1000);
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

            String condition = item.getWeatherDescription().toLowerCase();

            // Count rain/snow items per day
            if (condition.contains("rain") || condition.contains("drizzle") ||
                    condition.contains("thunderstorm")) {
                rainyItemsPerDay.put(dayOfYear, rainyItemsPerDay.getOrDefault(dayOfYear, 0) + 1);
            }

            // Count condition occurrences
            conditionCounts.put(condition, conditionCounts.getOrDefault(condition, 0) + 1);
        }

        // Count days with significant rain
        int rainyDays = 0;
        for (int count : rainyItemsPerDay.values()) {
            if (count >= 2) { // At least 6 hours of rain
                rainyDays++;
            }
        }

        // Find dominant condition
        String dominantCondition = "clear";
        int dominantCount = 0;
        for (Map.Entry<String, Integer> entry : conditionCounts.entrySet()) {
            if (entry.getValue() > dominantCount) {
                dominantCount = entry.getValue();
                dominantCondition = entry.getKey();
            }
        }

        int totalDays = hourlyForecasts.size() / 8;
        int forecastDays = Math.min(totalDays, 5);

        // Generate summary based on actual data
        if (rainyDays >= 3) {
            return String.format(Locale.getDefault(),
                    "Rain expected %d out of %d days ahead",
                    rainyDays, forecastDays);
        } else if (rainyDays >= 1) {
            return String.format(Locale.getDefault(),
                    "Scattered rain in the next %d days",
                    forecastDays);
        } else if (dominantCondition.contains("snow")) {
            return "Possible snow in the coming days";
        } else if (dominantCondition.contains("clear")) {
            return "Clear skies this week";
        } else if (dominantCondition.contains("cloud")) {
            return "Mostly cloudy this week";
        } else {
            return "Changing weather in the coming days";
        }
    }

    /**
     * LEGACY: Generate summary from HourlyForecastResponse
     * @deprecated Migrate to domain model version using ForecastData.HourlyForecast
     */
    @Deprecated
    public static String generateSummary(String weatherCondition, com.example.weatherapp.data.responses.HourlyForecastResponse hourlyForecastData) {
        if (hourlyForecastData != null && hourlyForecastData.getList() != null) {
            return generateFromLegacyForecastData(hourlyForecastData);
        }
        return generateFromWeatherCondition(weatherCondition);
    }
    
    @Deprecated
    private static String generateFromLegacyForecastData(com.example.weatherapp.data.responses.HourlyForecastResponse forecastData) {
        Map<Integer, Integer> rainyItemsPerDay = new HashMap<>();
        Map<String, Integer> conditionCounts = new HashMap<>();
        Calendar calendar = Calendar.getInstance();

        for (com.example.weatherapp.data.responses.HourlyForecastResponse.HourlyItem item : forecastData.getList()) {
            if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                calendar.setTimeInMillis(item.getDt() * 1000);
                int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

                String condition = item.getWeather().get(0).getMain().toLowerCase();

                if (condition.contains("rain") || condition.contains("drizzle") ||
                        condition.contains("thunderstorm")) {
                    rainyItemsPerDay.put(dayOfYear, rainyItemsPerDay.getOrDefault(dayOfYear, 0) + 1);
                }

                conditionCounts.put(condition, conditionCounts.getOrDefault(condition, 0) + 1);
            }
        }

        int rainyDays = 0;
        for (int count : rainyItemsPerDay.values()) {
            if (count >= 2) {
                rainyDays++;
            }
        }

        String dominantCondition = "clear";
        int dominantCount = 0;
        for (Map.Entry<String, Integer> entry : conditionCounts.entrySet()) {
            if (entry.getValue() > dominantCount) {
                dominantCount = entry.getValue();
                dominantCondition = entry.getKey();
            }
        }

        int totalDays = forecastData.getList().size() / 8;
        int forecastDays = Math.min(totalDays, 5);

        if (rainyDays >= 3) {
            return String.format(Locale.getDefault(),
                    "Rain expected %d out of %d days ahead",
                    rainyDays, forecastDays);
        } else if (rainyDays >= 1) {
            return String.format(Locale.getDefault(),
                    "Scattered rain in the next %d days",
                    forecastDays);
        } else if (dominantCondition.contains("snow")) {
            return "Possible snow in the coming days";
        } else if (dominantCondition.contains("clear")) {
            return "Clear skies this week";
        } else if (dominantCondition.contains("cloud")) {
            return "Mostly cloudy this week";
        } else {
            return "Changing weather in the coming days";
        }
    }

    private static String generateFromWeatherCondition(String weatherCondition) {
        if (weatherCondition.contains("rain") || weatherCondition.contains("drizzle")) {
            return "Rain in the coming days";
        } else if (weatherCondition.contains("snow")) {
            return "Possible snow in the coming days";
        } else if (weatherCondition.contains("clear")) {
            return "Clear skies this week";
        } else if (weatherCondition.contains("cloud")) {
            return "Mostly cloudy this week";
        } else {
            return "Changing weather in the coming days";
        }
    }
}
