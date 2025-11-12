package com.example.weatherapp.data.repository.implementation;

import android.content.Context;
import android.util.Log;

import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.local.dao.WeatherDao;
import com.example.weatherapp.data.local.database.WeatherDatabase;
import com.example.weatherapp.data.local.entity.WeatherCacheEntity;
import com.example.weatherapp.data.local.mapper.CacheMapper;
import com.example.weatherapp.data.mapper.DomainMapper;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.UVIndexResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.WeatherRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Implementation of WeatherRepository with caching
 * Handles data fetching from API, database caching, and conversion to domain models
 * Cache-first strategy: check cache first, then fetch from network if needed
 */
public class WeatherRepositoryImpl implements WeatherRepository {
    private static final String TAG = "WeatherRepository";
    private final WeatherApiService apiService;
    private final String apiKey;
    private final WeatherDao weatherDao;
    private final Executor executor;
    
    // Cache latest responses for ChartsActivity
    private WeatherResponse latestWeatherResponse;
    private HourlyForecastResponse latestHourlyForecastResponse;
    
    public WeatherRepositoryImpl(Context context, String apiKey) {
        this.apiService = RetrofitClient.getInstance().getWeatherApi();
        this.apiKey = apiKey;
        this.weatherDao = WeatherDatabase.getInstance(context).weatherDao();
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    // Getters for cached responses
    public WeatherResponse getLatestWeatherResponse() {
        return latestWeatherResponse;
    }
    
    public HourlyForecastResponse getLatestHourlyForecastResponse() {
        return latestHourlyForecastResponse;
    }
    
    @Override
    public void getWeatherByCity(String cityName, String temperatureUnit, WeatherCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Log.d(TAG, "Fetching weather for city: " + cityName + ", units: " + units);
        
        // First, check cache in background thread
        executor.execute(() -> {
            WeatherCacheEntity cachedEntity = weatherDao.getWeatherByCity(cityName);
            
            if (cachedEntity != null && cachedEntity.isValid()) {
                // Cache hit and still valid - return immediately
                Log.d(TAG, "Cache hit for city: " + cityName);
                WeatherData cachedData = CacheMapper.toDomain(cachedEntity);
                callback.onSuccess(cachedData);
                return;
            }
            
            // Cache miss or expired - fetch from network
            Log.d(TAG, "Cache miss or expired for city: " + cityName + ", fetching from network");
            fetchWeatherFromNetwork(cityName, units, temperatureUnit, callback);
        });
    }
    
    /**
     * Fetch weather from network and cache the result
     */
    private void fetchWeatherFromNetwork(String cityName, String units, String temperatureUnit, WeatherCallback callback) {
        Call<WeatherResponse> call = apiService.getWeatherByCity(cityName, apiKey, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherData weatherData = DomainMapper.toWeatherData(response.body(), temperatureUnit);
                    if (weatherData != null) {
                        // Cache the result in background
                        executor.execute(() -> {
                            WeatherCacheEntity entity = CacheMapper.toEntity(weatherData);
                            if (entity != null) {
                                weatherDao.insertWeather(entity);
                                Log.d(TAG, "Weather cached for city: " + cityName);
                            }
                        });
                        callback.onSuccess(weatherData);
                    } else {
                        callback.onError("Failed to parse weather data");
                    }
                } else {
                    String errorMsg = "City not found. Please check the city name and try again.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorMsg);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }
            
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                // On network failure, try to return cached data even if expired
                executor.execute(() -> {
                    WeatherCacheEntity cachedEntity = weatherDao.getWeatherByCity(cityName);
                    if (cachedEntity != null) {
                        Log.d(TAG, "Network failed, returning expired cache for: " + cityName);
                        WeatherData cachedData = CacheMapper.toDomain(cachedEntity);
                        callback.onSuccess(cachedData);
                    } else {
                        callback.onError(getNetworkErrorMessage(t));
                    }
                });
            }
        });
    }
    
    @Override
    public void getWeatherByCoordinates(double latitude, double longitude, String temperatureUnit, WeatherCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Log.d(TAG, "Fetching weather by coordinates: " + latitude + ", " + longitude);
        
        // Check cache first
        executor.execute(() -> {
            WeatherCacheEntity cachedEntity = weatherDao.getWeatherByCoordinates(latitude, longitude);
            
            if (cachedEntity != null && cachedEntity.isValid()) {
                Log.d(TAG, "Cache hit for coordinates");
                WeatherData cachedData = CacheMapper.toDomain(cachedEntity);
                callback.onSuccess(cachedData);
                return;
            }
            
            Log.d(TAG, "Cache miss for coordinates, fetching from network");
            fetchWeatherByCoordinatesFromNetwork(latitude, longitude, units, temperatureUnit, callback);
        });
    }
    
    /**
     * Fetch weather by coordinates from network and cache
     */
    private void fetchWeatherByCoordinatesFromNetwork(double latitude, double longitude, String units, String temperatureUnit, WeatherCallback callback) {
        Call<WeatherResponse> call = apiService.getWeatherByCoordinates(latitude, longitude, apiKey, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cache the response for ChartsActivity
                    latestWeatherResponse = response.body();
                    Log.d(TAG, "✓ Cached WeatherResponse for charts");
                    
                    WeatherData weatherData = DomainMapper.toWeatherData(response.body(), temperatureUnit);
                    if (weatherData != null) {
                        // Cache the result
                        executor.execute(() -> {
                            WeatherCacheEntity entity = CacheMapper.toEntity(weatherData);
                            if (entity != null) {
                                weatherDao.insertWeather(entity);
                                Log.d(TAG, "Weather cached for coordinates");
                            }
                        });
                        callback.onSuccess(weatherData);
                    } else {
                        callback.onError("Failed to parse weather data");
                    }
                } else {
                    callback.onError("Failed to fetch weather data");
                }
            }
            
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                // Try returning cached data on network failure
                executor.execute(() -> {
                    WeatherCacheEntity cachedEntity = weatherDao.getWeatherByCoordinates(latitude, longitude);
                    if (cachedEntity != null) {
                        Log.d(TAG, "Network failed, returning expired cache for coordinates");
                        WeatherData cachedData = CacheMapper.toDomain(cachedEntity);
                        callback.onSuccess(cachedData);
                    } else {
                        callback.onError(getNetworkErrorMessage(t));
                    }
                });
            }
        });
    }
    
    @Override
    public void getForecast(double latitude, double longitude, String temperatureUnit, ForecastCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Log.d(TAG, "Fetching forecast for coordinates: " + latitude + ", " + longitude);
        
        Call<HourlyForecastResponse> call = apiService.getHourlyForecastByCoordinates(latitude, longitude, apiKey, units);
        call.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cache the response for ChartsActivity
                    latestHourlyForecastResponse = response.body();
                    Log.d(TAG, "✓ Cached HourlyForecastResponse for charts");
                    
                    ForecastData forecastData = DomainMapper.toForecastData(response.body());
                    if (forecastData != null) {
                        callback.onSuccess(forecastData);
                    } else {
                        callback.onError("Failed to parse forecast data");
                    }
                } else {
                    callback.onError("Failed to fetch forecast data");
                }
            }
            
            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError(getNetworkErrorMessage(t));
            }
        });
    }
    
    @Override
    public void getUVIndex(double latitude, double longitude, UVIndexCallback callback) {
        Log.d(TAG, "Fetching UV index for coordinates: " + latitude + ", " + longitude);
        
        Call<UVIndexResponse> call = apiService.getUVIndex(latitude, longitude, apiKey);
        call.enqueue(new Callback<UVIndexResponse>() {
            @Override
            public void onResponse(Call<UVIndexResponse> call, Response<UVIndexResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int uvIndex = (int) Math.round(response.body().getValue());
                    callback.onSuccess(uvIndex);
                } else {
                    Log.w(TAG, "UV Index API returned error");
                    callback.onError("Failed to fetch UV index");
                }
            }
            
            @Override
            public void onFailure(Call<UVIndexResponse> call, Throwable t) {
                Log.e(TAG, "UV Index network error", t);
                callback.onError(getNetworkErrorMessage(t));
            }
        });
    }
    
    @Override
    public void getAirQuality(double latitude, double longitude, AirQualityCallback callback) {
        Log.d(TAG, "Fetching air quality for coordinates: " + latitude + ", " + longitude);
        
        Call<AirQualityResponse> call = apiService.getAirQuality(latitude, longitude, apiKey);
        call.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AirQualityData airQualityData = DomainMapper.toAirQualityData(response.body());
                    if (airQualityData != null) {
                        callback.onSuccess(airQualityData);
                    } else {
                        callback.onError("Failed to parse air quality data");
                    }
                } else {
                    Log.w(TAG, "Air Quality API returned error");
                    callback.onError("Failed to fetch air quality data");
                }
            }
            
            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                Log.e(TAG, "Air Quality network error", t);
                callback.onError(getNetworkErrorMessage(t));
            }
        });
    }
    
    /**
     * Get user-friendly network error message
     */
    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.UnknownHostException) {
            return "No internet connection. Please check your network.";
        } else if (t instanceof java.net.SocketTimeoutException) {
            return "Connection timeout. Please try again.";
        } else {
            return "Network error: " + t.getMessage();
        }
    }
}
