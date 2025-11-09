package com.example.weatherapp.data.mapper;

import com.example.weatherapp.data.models.HourlyForecast;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Mapper class để chuyển đổi giữa API Response models và Domain models
 * Giúp tách biệt data layer và domain layer
 */
public class WeatherMapper {

    /**
     * Chuyển đổi WeatherResponse từ API thành domain model
     */
    public static String getCityName(WeatherResponse response) {
        return response != null ? response.getName() : "";
    }

    /**
     * Lấy nhiệt độ từ WeatherResponse
     */
    public static double getTemperature(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getTemp();
        }
        return 0.0;
    }

    /**
     * Lấy mô tả thời tiết
     */
    public static String getWeatherDescription(WeatherResponse response) {
        if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
            return response.getWeather().get(0).getDescription();
        }
        return "";
    }

    /**
     * Lấy weather condition
     */
    public static String getWeatherCondition(WeatherResponse response) {
        if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
            return response.getWeather().get(0).getMain();
        }
        return "";
    }

    /**
     * Lấy icon code
     */
    public static String getWeatherIcon(WeatherResponse response) {
        if (response != null && response.getWeather() != null && !response.getWeather().isEmpty()) {
            return response.getWeather().get(0).getIcon();
        }
        return "";
    }

    /**
     * Lấy độ ẩm
     */
    public static int getHumidity(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getHumidity();
        }
        return 0;
    }

    /**
     * Lấy tốc độ gió
     */
    public static double getWindSpeed(WeatherResponse response) {
        if (response != null && response.getWind() != null) {
            return response.getWind().getSpeed();
        }
        return 0.0;
    }

    /**
     * Lấy áp suất
     */
    public static int getPressure(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getPressure();
        }
        return 0;
    }

    /**
     * Lấy tầm nhìn xa
     */
    public static int getVisibility(WeatherResponse response) {
        if (response != null && response.getVisibility() != null) {
            return response.getVisibility();
        }
        return 0;
    }

    /**
     * Lấy nhiệt độ cảm nhận
     */
    public static double getFeelsLike(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getFeelsLike();
        }
        return 0.0;
    }

    /**
     * Lấy nhiệt độ cao nhất
     */
    public static double getTempMax(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getTempMax();
        }
        return 0.0;
    }

    /**
     * Lấy nhiệt độ thấp nhất
     */
    public static double getTempMin(WeatherResponse response) {
        if (response != null && response.getMain() != null) {
            return response.getMain().getTempMin();
        }
        return 0.0;
    }

    /**
     * Chuyển đổi HourlyForecastResponse thành List<HourlyForecast>
     */
    public static List<HourlyForecast> mapHourlyForecast(HourlyForecastResponse response) {
        List<HourlyForecast> forecasts = new ArrayList<>();

        if (response == null || response.getList() == null) {
            return forecasts;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int i = 0; i < response.getList().size() && i < 9; i++) {
            HourlyForecastResponse.HourlyItem item = response.getList().get(i);

            String hour = timeFormat.format(new Date(item.getDt() * 1000L));
            String icon = item.getWeather().get(0).getIcon();
            int iconResource = getWeatherIconResource(icon);
            int temp = (int) Math.round(item.getMain().getTemp());
            boolean isNow = (i == 0);
            int rainProb = (int) (item.getPop() * 100);

            HourlyForecast forecast = new HourlyForecast(hour, icon, iconResource, temp, isNow, rainProb);
            forecasts.add(forecast);
        }

        return forecasts;
    }

    /**
     * Map weather icon code to drawable resource
     */
    private static int getWeatherIconResource(String iconCode) {
        // Implement icon mapping logic here
        // Return appropriate drawable resource ID based on icon code
        return 0; // Placeholder
    }

    // Prevent instantiation
    private WeatherMapper() {
        throw new AssertionError("Cannot instantiate WeatherMapper class");
    }
}
