package com.example.weatherapp.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weatherapp.data.repository.implementation.WeatherRepositoryImpl;
import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.repository.WeatherRepository;
import com.example.weatherapp.utils.LocationHelper;
import com.example.weatherapp.utils.Constants;

import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Worker to periodically check weather and send smart alerts
 * Checks weather conditions every 2 minutes and sends notifications
 */
public class SmartWeatherAlertWorker extends Worker {
    private static final String TAG = "SmartWeatherAlertWorker";
    
    private final SmartWeatherNotificationManager notificationManager;
    private final WeatherRepositoryImpl weatherRepository;
    
    public SmartWeatherAlertWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.notificationManager = new SmartWeatherNotificationManager(context);
        this.weatherRepository = new WeatherRepositoryImpl(context, Constants.WEATHER_API_KEY);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Smart Weather Alert Worker started");
        
        try {
            // Get current location
            LocationHelper locationHelper = new LocationHelper(getApplicationContext());
            double[] location = locationHelper.getLastKnownLocation();
            
            // Fallback to default coordinates if no GPS location available
            double latitude, longitude;
            if (location == null) {
                Log.w(TAG, "No GPS location available, using default location");
                latitude = 21.0285;  // Default: Hanoi
                longitude = 105.8542;
            } else {
                latitude = location[0];
                longitude = location[1];
            }
            
            // Create latch to wait for async calls
            CountDownLatch latch = new CountDownLatch(1);
            
            // Check weather and send alerts
            checkWeatherAlerts(latitude, longitude, latch);
            
            // Wait up to 30 seconds for all checks to complete
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            
            if (completed) {
                Log.d(TAG, "Smart Weather Alert Worker completed successfully");
                return Result.success();
            } else {
                Log.w(TAG, "Smart Weather Alert Worker timed out");
                return Result.retry();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in SmartWeatherAlertWorker", e);
            return Result.failure();
        }
    }
    
    /**
     * Check weather conditions and send appropriate alerts
     */
    private void checkWeatherAlerts(double latitude, double longitude, CountDownLatch latch) {
        // Get current weather
        weatherRepository.getWeatherByCoordinates(latitude, longitude, "celsius", 
            new WeatherRepository.WeatherCallback() {
                @Override
                public void onSuccess(WeatherData weatherData) {
                    Log.d(TAG, "Weather data received: " + weatherData.getTemperature() + "°C, " + weatherData.getWeatherMain());
                    
                    // Check for sudden temperature change
                    double lastTemp = notificationManager.getLastTemperature();
                    if (!Double.isNaN(lastTemp)) {
                        Log.d(TAG, "Checking temperature change: " + lastTemp + "°C -> " + weatherData.getTemperature() + "°C");
                        notificationManager.checkSuddenWeatherChange(
                            weatherData.getTemperature(),
                            lastTemp,
                            weatherData.getWeatherMain(), // Use weatherMain as condition
                            "Unknown" // Previous condition - you may want to store this
                        );
                    }
                    
                    // Save current temperature for next check
                    notificationManager.saveLastTemperature(weatherData.getTemperature());
                    
                    // Check UV index
                    int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    checkUVAlert(latitude, longitude, currentHour);
                    
                    // Check air quality
                    checkAirQualityAlert(latitude, longitude);
                    
                    // Check forecast for rain warning
                    checkRainAlert(latitude, longitude);
                    
                    // Release latch after all checks initiated
                    latch.countDown();
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error fetching weather: " + error);
                    latch.countDown(); // Release even on error
                }
            }
        );
    }
    
    /**
     * Check UV index and send alert if high
     */
    private void checkUVAlert(double latitude, double longitude, int currentHour) {
        weatherRepository.getUVIndex(latitude, longitude, 
            new WeatherRepository.UVIndexCallback() {
                @Override
                public void onSuccess(int uvIndex) {
                    notificationManager.checkUVIndex(uvIndex, currentHour);
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error fetching UV index: " + error);
                }
            }
        );
    }
    
    /**
     * Check air quality and send alert if poor
     */
    private void checkAirQualityAlert(double latitude, double longitude) {
        weatherRepository.getAirQuality(latitude, longitude, 
            new WeatherRepository.AirQualityCallback() {
                @Override
                public void onSuccess(AirQualityData airQualityData) {
                    notificationManager.checkAirQuality(
                        airQualityData.getUSAQI(), // Convert to US AQI scale
                        airQualityData.getMainPollutant()
                    );
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error fetching air quality: " + error);
                }
            }
        );
    }
    
    /**
     * Check forecast for rain warning
     */
    private void checkRainAlert(double latitude, double longitude) {
        weatherRepository.getForecast(latitude, longitude, "celsius",
            new WeatherRepository.ForecastCallback() {
                @Override
                public void onSuccess(com.example.weatherapp.domain.model.ForecastData forecastData) {
                    // Analyze forecast for rain in next 60 minutes
                    analyzeRainForecast(forecastData);
                }
                
                @Override
                public void onError(String error) {
                    Log.w(TAG, "Error fetching forecast: " + error);
                }
            }
        );
    }
    
    /**
     * Analyze forecast data for rain probability
     */
    private void analyzeRainForecast(com.example.weatherapp.domain.model.ForecastData forecastData) {
        if (forecastData == null || forecastData.getHourlyForecasts() == null || 
            forecastData.getHourlyForecasts().isEmpty()) {
            return;
        }
        
        // Check first 2 hours for rain
        for (int i = 0; i < Math.min(2, forecastData.getHourlyForecasts().size()); i++) {
            ForecastData.HourlyForecast hourlyForecast = 
                forecastData.getHourlyForecasts().get(i);
            String condition = hourlyForecast.getWeatherDescription().toLowerCase();
            int rainProb = hourlyForecast.getRainProbability();
            
            if (condition.contains("rain") || condition.contains("drizzle") || 
                condition.contains("shower") || rainProb >= 60) {
                int minutesUntilRain = (i + 1) * 30; // Rough estimate (30 min per interval)
                
                notificationManager.checkRainWarning(rainProb, minutesUntilRain);
                break;
            }
        }
    }
}
