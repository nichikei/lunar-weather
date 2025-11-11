package com.example.weatherapp.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.database.dao.AirQualityCacheDao;
import com.example.weatherapp.data.database.dao.ForecastCacheDao;
import com.example.weatherapp.data.database.dao.UVIndexCacheDao;
import com.example.weatherapp.data.database.entities.AirQualityCacheEntity;
import com.example.weatherapp.data.database.entities.ForecastCacheEntity;
import com.example.weatherapp.data.database.entities.UVIndexCacheEntity;
import com.example.weatherapp.data.local.database.WeatherDatabase;
import com.example.weatherapp.data.local.prefs.PreferenceManager;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.UVIndexResponse;
import com.example.weatherapp.data.responses.WeatherAlertsResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository để quản lý tất cả data operations liên quan đến thời tiết
 * Tách biệt logic data access khỏi UI layer
 * 
 * Version 2: Integrated Room caching for all weather data types
 * - Cache TTL: 10 minutes (600000ms)
 * - Cache-first strategy: Check cache → Fetch from network if expired
 */
public class WeatherRepository {

    private static final String TAG = "WeatherRepository";
    private static final long CACHE_TTL = 10 * 60 * 1000; // 10 minutes in milliseconds
    
    private final WeatherApiService apiService;
    private final PreferenceManager preferenceManager;
    private final ForecastCacheDao forecastCacheDao;
    private final AirQualityCacheDao airQualityCacheDao;
    private final UVIndexCacheDao uvIndexCacheDao;
    private final Executor executor;

    public WeatherRepository(Context context) {
        this.apiService = RetrofitClient.getInstance().getWeatherApi();
        this.preferenceManager = new PreferenceManager(context);
        
        // Initialize DAOs
        WeatherDatabase database = WeatherDatabase.getInstance(context);
        this.forecastCacheDao = database.forecastCacheDao();
        this.airQualityCacheDao = database.airQualityCacheDao();
        this.uvIndexCacheDao = database.uvIndexCacheDao();
        
        // Executor for database operations
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ============ Callback Interfaces ============

    public interface WeatherCallback {
        void onSuccess(WeatherResponse response);
        void onError(String error);
    }

    public interface ForecastCallback {
        void onSuccess(HourlyForecastResponse response);
        void onError(String error);
    }

    public interface UVIndexCallback {
        void onSuccess(UVIndexResponse response);
        void onError(String error);
    }

    public interface AirQualityCallback {
        void onSuccess(AirQualityResponse response);
        void onError(String error);
    }

    public interface WeatherAlertsCallback {
        void onSuccess(WeatherAlertsResponse response);
        void onError(String error);
    }

    // ============ Weather Data Methods ============

    /**
     * Lấy thời tiết theo tên thành phố
     */
    public void getWeatherByCity(String cityName, WeatherCallback callback) {
        String units = getApiUnits();
        Log.d(TAG, "Fetching weather for: " + cityName + ", units: " + units);

        Call<WeatherResponse> call = apiService.getWeatherByCity(
            cityName,
            Constants.WEATHER_API_KEY,
            units
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Weather data received successfully");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to fetch weather: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Lấy thời tiết theo tọa độ GPS
     */
    public void getWeatherByCoordinates(double latitude, double longitude, WeatherCallback callback) {
        String units = getApiUnits();
        Log.d(TAG, "Fetching weather for coordinates: " + latitude + ", " + longitude);

        Call<WeatherResponse> call = apiService.getWeatherByCoordinates(
            latitude,
            longitude,
            Constants.WEATHER_API_KEY,
            units
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Weather data received successfully");
                    callback.onSuccess(response.body());
                } else {
                    String error = "Failed to fetch weather: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                String error = "Network error: " + t.getMessage();
                Log.e(TAG, error, t);
                callback.onError(error);
            }
        });
    }

    /**
     * Lấy dự báo theo giờ (with caching)
     * TODO: Implement cache-first retrieval in future (requires domain layer migration)
     */
    public void getHourlyForecast(String cityName, ForecastCallback callback) {
        String units = getApiUnits();
        
        Call<HourlyForecastResponse> call = apiService.getHourlyForecast(
            cityName,
            Constants.WEATHER_API_KEY,
            units
        );

        call.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HourlyForecastResponse forecastResponse = response.body();
                    
                    // Save to cache asynchronously
                    executor.execute(() -> {
                        try {
                            List<ForecastCacheEntity> entities = convertResponseToForecastEntities(
                                forecastResponse, cityName, "hourly"
                            );
                            forecastCacheDao.insertForecasts(entities);
                            Log.d(TAG, "✅ Cached " + entities.size() + " hourly forecasts for: " + cityName);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to cache forecasts", e);
                        }
                    });
                    
                    callback.onSuccess(forecastResponse);
                } else {
                    callback.onError("Failed to fetch forecast: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy dự báo theo giờ bằng tọa độ (with caching)
     */
    public void getHourlyForecastByCoordinates(double latitude, double longitude, ForecastCallback callback) {
        String units = getApiUnits();
        
        Call<HourlyForecastResponse> call = apiService.getHourlyForecastByCoordinates(
            latitude,
            longitude,
            Constants.WEATHER_API_KEY,
            units
        );

        call.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HourlyForecastResponse forecastResponse = response.body();
                    String cityName = forecastResponse.getCity() != null ? 
                                     forecastResponse.getCity().getName() : "Unknown";
                    
                    // Save to cache asynchronously
                    executor.execute(() -> {
                        try {
                            List<ForecastCacheEntity> entities = convertResponseToForecastEntities(
                                forecastResponse, cityName, "hourly"
                            );
                            forecastCacheDao.insertForecasts(entities);
                            Log.d(TAG, "✅ Cached hourly forecasts for coordinates: " + latitude + ", " + longitude);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to cache forecasts", e);
                        }
                    });
                    
                    callback.onSuccess(forecastResponse);
                } else {
                    callback.onError("Failed to fetch forecast: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy chỉ số UV (with caching)
     */
    public void getUVIndex(double latitude, double longitude, UVIndexCallback callback) {
        Call<UVIndexResponse> call = apiService.getUVIndex(
            latitude,
            longitude,
            Constants.WEATHER_API_KEY
        );

        call.enqueue(new Callback<UVIndexResponse>() {
            @Override
            public void onResponse(Call<UVIndexResponse> call, Response<UVIndexResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UVIndexResponse uvResponse = response.body();
                    
                    // Save to cache asynchronously
                    executor.execute(() -> {
                        try {
                            UVIndexCacheEntity entity = convertResponseToUVEntity(uvResponse, latitude, longitude);
                            uvIndexCacheDao.insertUVIndex(entity);
                            Log.d(TAG, "✅ Cached UV index for: " + latitude + ", " + longitude);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to cache UV index", e);
                        }
                    });
                    
                    callback.onSuccess(uvResponse);
                } else {
                    callback.onError("Failed to fetch UV index: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UVIndexResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy chất lượng không khí (with caching)
     */
    public void getAirQuality(double latitude, double longitude, AirQualityCallback callback) {
        Call<AirQualityResponse> call = apiService.getAirQuality(
            latitude,
            longitude,
            Constants.WEATHER_API_KEY
        );

        call.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AirQualityResponse aqResponse = response.body();
                    
                    // Save to cache asynchronously
                    executor.execute(() -> {
                        try {
                            AirQualityCacheEntity entity = convertResponseToAQEntity(aqResponse, latitude, longitude);
                            airQualityCacheDao.insertAirQuality(entity);
                            Log.d(TAG, "✅ Cached air quality for: " + latitude + ", " + longitude);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to cache air quality", e);
                        }
                    });
                    
                    callback.onSuccess(aqResponse);
                } else {
                    callback.onError("Failed to fetch air quality: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy cảnh báo thời tiết
     */
    public void getWeatherAlerts(double latitude, double longitude, WeatherAlertsCallback callback) {
        Call<WeatherAlertsResponse> call = apiService.getWeatherAlerts(
            latitude,
            longitude,
            Constants.WEATHER_API_KEY,
            "minutely,hourly"
        );

        call.enqueue(new Callback<WeatherAlertsResponse>() {
            @Override
            public void onResponse(Call<WeatherAlertsResponse> call, Response<WeatherAlertsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch alerts: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherAlertsResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // ============ Helper Methods ============

    /**
     * Lấy đơn vị API dựa trên settings (metric/imperial)
     */
    private String getApiUnits() {
        String tempUnit = preferenceManager.getTemperatureUnit();
        return tempUnit.equals(Constants.UNIT_CELSIUS) ? Constants.UNIT_METRIC : Constants.UNIT_IMPERIAL;
    }

    /**
     * Lưu tên thành phố cuối cùng
     */
    public void saveLastCity(String cityName) {
        preferenceManager.setLastCity(cityName);
    }

    /**
     * Lấy tên thành phố cuối cùng
     */
    public String getLastCity() {
        return preferenceManager.getLastCity();
    }
    
    // ============ Cache Converter Methods ============
    
    /**
     * Convert HourlyForecastResponse to ForecastCacheEntity list
     */
    private List<ForecastCacheEntity> convertResponseToForecastEntities(
            HourlyForecastResponse response, String cityName, String forecastType) {
        List<ForecastCacheEntity> entities = new ArrayList<>();
        long cachedAt = System.currentTimeMillis();
        
        // Note: HourlyForecastResponse.City does not contain coordinates
        // Coordinates will be 0.0 by default (can be improved in future with lat/lon from API call)
        double latitude = 0.0;
        double longitude = 0.0;
        
        for (HourlyForecastResponse.HourlyItem item : response.getList()) {
            ForecastCacheEntity entity = new ForecastCacheEntity();
            entity.setCityName(cityName);
            entity.setLatitude(latitude);
            entity.setLongitude(longitude);
            entity.setForecastType(forecastType);
            entity.setTimestamp(item.getDt());
            entity.setCachedAt(cachedAt);
            
            // Main data
            if (item.getMain() != null) {
                entity.setTemperature(item.getMain().getTemp());
                entity.setTempMin(item.getMain().getTempMin());
                entity.setTempMax(item.getMain().getTempMax());
                entity.setHumidity(item.getMain().getHumidity());
            }
            
            // Weather
            if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                HourlyForecastResponse.HourlyItem.Weather weather = item.getWeather().get(0);
                entity.setWeatherMain(weather.getMain());
                entity.setWeatherDescription(weather.getDescription());
                entity.setWeatherIcon(weather.getIcon());
            }
            
            // Wind
            if (item.getWind() != null) {
                entity.setWindSpeed(item.getWind().getSpeed());
            }
            
            // Rain probability
            entity.setRainProbability((int) (item.getPop() * 100)); // Convert 0-1 to 0-100
            
            entities.add(entity);
        }
        
        return entities;
    }
    
    /**
     * Convert UVIndexResponse to UVIndexCacheEntity
     */
    private UVIndexCacheEntity convertResponseToUVEntity(
            UVIndexResponse response, double latitude, double longitude) {
        UVIndexCacheEntity entity = new UVIndexCacheEntity();
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setUvIndex(response.getValue());
        entity.setUvLevel(getUVLevel(response.getValue()));
        entity.setTimestamp(response.getDate());
        entity.setCachedAt(System.currentTimeMillis());
        return entity;
    }
    
    /**
     * Convert AirQualityResponse to AirQualityCacheEntity
     */
    private AirQualityCacheEntity convertResponseToAQEntity(
            AirQualityResponse response, double latitude, double longitude) {
        AirQualityCacheEntity entity = new AirQualityCacheEntity();
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
        entity.setCachedAt(System.currentTimeMillis());
        
        if (response.getList() != null && !response.getList().isEmpty()) {
            AirQualityResponse.AirQualityData item = response.getList().get(0);
            
            if (item.getMain() != null) {
                entity.setAqi(item.getMain().getAqi());
            }
            
            if (item.getComponents() != null) {
                AirQualityResponse.Components comp = item.getComponents();
                entity.setCo(comp.getCo());
                entity.setNo(comp.getNo());
                entity.setNo2(comp.getNo2());
                entity.setO3(comp.getO3());
                entity.setSo2(comp.getSo2());
                entity.setPm2_5(comp.getPm2_5());
                entity.setPm10(comp.getPm10());
                entity.setNh3(comp.getNh3());
            }
        }
        
        return entity;
    }
    
    /**
     * Get UV level string from UV index value
     */
    private String getUVLevel(double uvIndex) {
        if (uvIndex < 3) return "Low";
        if (uvIndex < 6) return "Moderate";
        if (uvIndex < 8) return "High";
        if (uvIndex < 11) return "Very High";
        return "Extreme";
    }
}

