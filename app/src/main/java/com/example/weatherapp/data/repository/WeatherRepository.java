package com.example.weatherapp.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.local.prefs.PreferenceManager;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.UVIndexResponse;
import com.example.weatherapp.data.responses.WeatherAlertsResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository để quản lý tất cả data operations liên quan đến thời tiết
 * Tách biệt logic data access khỏi UI layer
 */
public class WeatherRepository {

    private static final String TAG = "WeatherRepository";
    private final WeatherApiService apiService;
    private final PreferenceManager preferenceManager;

    public WeatherRepository(Context context) {
        this.apiService = RetrofitClient.getInstance().getWeatherApi();
        this.preferenceManager = new PreferenceManager(context);
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
     * Lấy dự báo theo giờ
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
                    callback.onSuccess(response.body());
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
     * Lấy dự báo theo giờ bằng tọa độ
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
                    callback.onSuccess(response.body());
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
     * Lấy chỉ số UV
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
                    callback.onSuccess(response.body());
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
     * Lấy chất lượng không khí
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
                    callback.onSuccess(response.body());
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
}

