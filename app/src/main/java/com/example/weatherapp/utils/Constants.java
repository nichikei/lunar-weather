package com.example.weatherapp.utils;

/**
 * Class chứa tất cả các hằng số trong ứng dụng
 * Giúp dễ quản lý và tránh hardcode
 */
public class Constants {

    // ============ API Configuration ============
    public static final String WEATHER_API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    public static final String GEMINI_API_KEY = "AIzaSyAPtCim4ke9C8SwsY2bXszsQotGfxE-XH4";
    public static final String WEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/";
    public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    // ============ SharedPreferences Keys ============
    public static final String PREFS_NAME = "WeatherAppPrefs";
    public static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    public static final String KEY_WIND_SPEED_UNIT = "wind_speed_unit";
    public static final String KEY_PRESSURE_UNIT = "pressure_unit";
    public static final String KEY_DARK_MODE = "dark_mode";
    public static final String KEY_NOTIFICATIONS = "notifications";
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_LAST_CITY = "last_city";

    // ============ Intent Extra Keys ============
    public static final String EXTRA_CITY_NAME = "CITY_NAME";
    public static final String EXTRA_WEATHER_DATA = "weather_data";
    public static final String EXTRA_HOURLY_DATA = "hourly_data";
    public static final String EXTRA_CURRENT_DATA = "current_data";
    public static final String EXTRA_UV_INDEX = "uv_index";
    public static final String EXTRA_USE_GPS = "use_gps";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static final String EXTRA_LANGUAGE_CHANGED = "language_changed";

    // ============ Temperature Units ============
    public static final String UNIT_CELSIUS = "celsius";
    public static final String UNIT_FAHRENHEIT = "fahrenheit";
    public static final String UNIT_METRIC = "metric";
    public static final String UNIT_IMPERIAL = "imperial";

    // ============ Wind Speed Units ============
    public static final String WIND_UNIT_MS = "ms";
    public static final String WIND_UNIT_KMH = "kmh";

    // ============ Pressure Units ============
    public static final String PRESSURE_UNIT_HPA = "hpa";
    public static final String PRESSURE_UNIT_MBAR = "mbar";

    // ============ Language Codes ============
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_VIETNAMESE = "vi";

    // ============ Notification ============
    public static final String NOTIFICATION_CHANNEL_ID = "weather_alerts";
    public static final String NOTIFICATION_CHANNEL_NAME = "Weather Alerts";
    public static final int NOTIFICATION_ID = 1001;
    public static final String ACTION_REFRESH_WEATHER = "com.example.weatherapp.ACTION_REFRESH_WEATHER";

    // ============ WorkManager ============
    public static final String WEATHER_NOTIFICATION_WORK = "weather_notification_work";
    public static final long NOTIFICATION_REPEAT_INTERVAL = 6; // hours

    // ============ Favorite Cities ============
    public static final String FAVORITE_CITIES_PREFS = "FavoriteCities";
    public static final String KEY_FAVORITE_CITIES = "cities";
    public static final int MAX_FAVORITE_CITIES = 10;

    // ============ Default Values ============
    public static final String DEFAULT_CITY = "Hanoi";
    public static final String DEFAULT_LANGUAGE = "vi";
    public static final String DEFAULT_TEMPERATURE_UNIT = "celsius";
    public static final String DEFAULT_WIND_UNIT = "ms";
    public static final String DEFAULT_PRESSURE_UNIT = "hpa";

    // ============ API Timeouts (seconds) ============
    public static final int CONNECT_TIMEOUT = 30;
    public static final int READ_TIMEOUT = 30;
    public static final int WRITE_TIMEOUT = 30;

    // ============ Gemini API Config ============
    public static final int GEMINI_MAX_TOKENS = 2048;
    public static final int GEMINI_TIMEOUT = 60;

    // ============ Chart Configuration ============
    public static final int MAX_HOURLY_FORECAST_ITEMS = 9;
    public static final int MAX_WEEKLY_FORECAST_ITEMS = 7;

    // ============ UV Index Levels ============
    public static final int UV_LOW = 2;
    public static final int UV_MODERATE = 5;
    public static final int UV_HIGH = 7;
    public static final int UV_VERY_HIGH = 10;

    // ============ AQI Levels ============
    public static final int AQI_GOOD = 50;
    public static final int AQI_MODERATE = 100;
    public static final int AQI_UNHEALTHY_SENSITIVE = 150;
    public static final int AQI_UNHEALTHY = 200;
    public static final int AQI_VERY_UNHEALTHY = 300;

    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}

