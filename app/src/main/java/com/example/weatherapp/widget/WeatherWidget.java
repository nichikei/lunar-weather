package com.example.weatherapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.example.weatherapp.R;
import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.ui.activities.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Weather Widget for home screen
 */
public class WeatherWidget extends AppWidgetProvider {

    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Get saved city from preferences
        SharedPreferences prefs = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE);
        String cityName = prefs.getString("last_city", "Hanoi");
        String tempUnit = prefs.getString("temperature_unit", "celsius");

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        // Set loading state
        views.setTextViewText(R.id.widget_city_name, cityName);
        views.setTextViewText(R.id.widget_temperature, "Loading...");
        views.setTextViewText(R.id.widget_condition, "");

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Update widget immediately with loading state
        appWidgetManager.updateAppWidget(appWidgetId, views);

        // Fetch weather data
        fetchWeatherData(context, appWidgetManager, appWidgetId, cityName, tempUnit);
    }

    // Public method for MainActivity to update widget with weather data
    public static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                   int appWidgetId, WeatherResponse weather) {
        SharedPreferences prefs = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE);
        String tempUnit = prefs.getString("temperature_unit", "celsius");
        updateWidgetWithWeatherData(context, appWidgetManager, appWidgetId, weather, tempUnit);
    }

    private static void fetchWeatherData(Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId, String cityName, String tempUnit) {
        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        String units = tempUnit.equals("celsius") ? "metric" : "imperial";

        Call<WeatherResponse> call = apiService.getWeatherByCity(cityName, API_KEY, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse weather = response.body();
                    updateWidgetWithWeatherData(context, appWidgetManager, appWidgetId, weather, tempUnit);
                } else {
                    updateWidgetWithError(context, appWidgetManager, appWidgetId, cityName);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                updateWidgetWithError(context, appWidgetManager, appWidgetId, cityName);
            }
        });
    }

    private static void updateWidgetWithWeatherData(Context context, AppWidgetManager appWidgetManager,
                                                   int appWidgetId, WeatherResponse weather, String tempUnit) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);

        // Update city name
        views.setTextViewText(R.id.widget_city_name, weather.getName());

        // Update temperature
        double temp = weather.getMain().getTemp();
        String tempSymbol = tempUnit.equals("celsius") ? "°" : "°F";
        views.setTextViewText(R.id.widget_temperature, String.format("%.0f%s", temp, tempSymbol));

        // Update weather condition
        if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            String condition = weather.getWeather().get(0).getMain();
            views.setTextViewText(R.id.widget_condition, condition);

            // Set weather icon
            int iconRes = getWeatherIconResource(condition.toLowerCase());
            views.setImageViewResource(R.id.widget_icon, iconRes);
        }

        // Update temperature range
        double tempMax = weather.getMain().getTempMax();
        double tempMin = weather.getMain().getTempMin();
        views.setTextViewText(R.id.widget_temp_range,
            String.format("H:%.0f%s  L:%.0f%s", tempMax, tempSymbol, tempMin, tempSymbol));

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void updateWidgetWithError(Context context, AppWidgetManager appWidgetManager,
                                             int appWidgetId, String cityName) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        views.setTextViewText(R.id.widget_city_name, cityName);
        views.setTextViewText(R.id.widget_temperature, "N/A");
        views.setTextViewText(R.id.widget_condition, "Unable to load");
        views.setTextViewText(R.id.widget_temp_range, "");

        // Create an Intent to launch MainActivity when clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static int getWeatherIconResource(String weatherCondition) {
        switch (weatherCondition) {
            case "clear":
                return R.drawable.sun_cloud_little_rain;
            case "clouds":
                return R.drawable.moon_cloud_mid_rain;
            case "rain":
                return R.drawable.big_rain_drops;
            case "drizzle":
                return R.drawable.sun_cloud_mid_rain;
            case "thunderstorm":
                return R.drawable.cloud_3_zap;
            case "snow":
                return R.drawable.big_snow;
            case "mist":
            case "fog":
            case "haze":
                return R.drawable.moon_cloud_fast_wind;
            default:
                return R.drawable.sun_cloud_angled_rain;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}
