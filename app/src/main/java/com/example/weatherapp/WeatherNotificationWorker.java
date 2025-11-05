package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;

public class WeatherNotificationWorker extends Worker {

    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_LAST_CITY = "last_city";
    private static final String KEY_NOTIFICATIONS = "notifications";

    public WeatherNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if notifications are enabled
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        if (!notificationsEnabled) {
            return Result.success();
        }

        // Get last searched city (default to Hanoi)
        String cityName = prefs.getString(KEY_LAST_CITY, "Hanoi");

        // Get temperature unit
        String tempUnit = SettingsActivity.getTemperatureUnit(prefs);
        String units = tempUnit.equals("celsius") ? "metric" : "imperial";

        try {
            // Fetch weather data
            WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
            Call<WeatherResponse> call = apiService.getWeatherByCity(cityName, API_KEY, units);
            Response<WeatherResponse> response = call.execute();

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse weather = response.body();

                // Get weather details
                double temp = weather.getMain().getTemp();
                String condition = weather.getWeather().get(0).getDescription();
                String tempSymbol = tempUnit.equals("celsius") ? "°C" : "°F";
                String temperature = String.format(Locale.getDefault(), "%.0f%s", temp, tempSymbol);

                // Show notification
                WeatherNotificationManager notificationManager = new WeatherNotificationManager(context);
                notificationManager.showWeatherNotification(
                        weather.getName(),
                        temperature,
                        capitalizeWords(condition),
                        weather.getWeather().get(0).getIcon()
                );

                // Check for weather alerts
                checkWeatherAlerts(weather, notificationManager);

                return Result.success();
            } else {
                return Result.retry();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private void checkWeatherAlerts(WeatherResponse weather, WeatherNotificationManager notificationManager) {
        // Check for extreme weather conditions
        String mainCondition = weather.getWeather().get(0).getMain().toLowerCase();
        double temp = weather.getMain().getTemp();
        int humidity = weather.getMain().getHumidity();

        // Temperature alerts
        if (temp > 35) {
            notificationManager.showWeatherAlert(
                    "⚠️ High Temperature Alert",
                    "Temperature is extremely high (" + (int)temp + "°). Stay hydrated and avoid direct sunlight.",
                    "high_temp"
            );
        } else if (temp < 0) {
            notificationManager.showWeatherAlert(
                    "❄️ Freezing Temperature Alert",
                    "Temperature is below freezing (" + (int)temp + "°). Dress warmly and be careful on roads.",
                    "low_temp"
            );
        }

        // Storm/Rain alerts
        if (mainCondition.contains("thunderstorm")) {
            notificationManager.showWeatherAlert(
                    "⚡ Thunderstorm Alert",
                    "Thunderstorm detected in your area. Stay indoors and avoid outdoor activities.",
                    "thunderstorm"
            );
        } else if (mainCondition.contains("rain") && weather.getRain() != null) {
            Double rainfall = weather.getRain().get1h();
            if (rainfall != null && rainfall > 5) {
                notificationManager.showWeatherAlert(
                        "🌧️ Heavy Rain Alert",
                        "Heavy rainfall expected. Carry an umbrella and drive carefully.",
                        "heavy_rain"
                );
            }
        }

        // High humidity alert
        if (humidity > 85) {
            notificationManager.showWeatherAlert(
                    "💧 High Humidity Alert",
                    "Humidity is very high (" + humidity + "%). It may feel uncomfortable outside.",
                    "high_humidity"
            );
        }
    }

    private String capitalizeWords(String str) {
        String[] words = str.split("\\s");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalizedWords.toString().trim();
    }
}

