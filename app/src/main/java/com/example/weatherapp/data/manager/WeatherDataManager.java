package com.example.weatherapp.data.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.UVIndexResponse;
import com.example.weatherapp.data.responses.WeatherResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Manages all weather data fetching operations
 * Handles API calls for weather, forecast, UV index, and air quality
 */
public class WeatherDataManager {
    private static final String TAG = "WeatherDataManager";
    private final WeatherApiService apiService;
    private final String apiKey;
    private final SharedPreferences sharedPreferences;

    public WeatherDataManager(Context context, String apiKey) {
        this.apiService = RetrofitClient.getInstance().getWeatherApi();
        this.apiKey = apiKey;
        this.sharedPreferences = context.getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE);
    }

    // ============ Callback Interfaces ============

    public interface WeatherDataCallback {
        void onWeatherSuccess(WeatherResponse response);
        void onWeatherError(String message);
    }

    public interface ForecastCallback {
        void onForecastSuccess(HourlyForecastResponse response);
        void onForecastError(String message);
    }

    public interface UVIndexCallback {
        void onUVIndexSuccess(int uvIndex);
        void onUVIndexError();
    }

    public interface AirQualityCallback {
        void onAirQualitySuccess(AirQualityResponse.AirQualityData data);
        void onAirQualityError();
    }

    // ============ Fetch Weather Methods ============

    /**
     * Fetch weather by city name
     */
    public void fetchWeatherByCity(String cityName, String temperatureUnit, WeatherDataCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Log.d(TAG, "Fetching weather for city: " + cityName + ", units: " + units);

        // Save current city for notifications
        sharedPreferences.edit().putString("last_city", cityName).apply();

        Call<WeatherResponse> call = apiService.getWeatherByCity(cityName, apiKey, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    callback.onWeatherSuccess(response.body());
                } else {
                    String errorMsg = "City not found";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                            Log.e(TAG, "Error response: " + errorMsg);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    callback.onWeatherError("City not found. Please check the city name and try again.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onWeatherError(getNetworkErrorMessage(t));
            }
        });
    }

    /**
     * Fetch weather by GPS coordinates
     */
    public void fetchWeatherByCoordinates(double lat, double lon, String temperatureUnit, WeatherDataCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Log.d(TAG, "Fetching weather for coordinates: lat=" + lat + ", lon=" + lon);

        Call<WeatherResponse> call = apiService.getWeatherByCoordinates(lat, lon, apiKey, units);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onWeatherSuccess(response.body());
                } else {
                    callback.onWeatherError("Could not fetch weather for this location");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onWeatherError(getNetworkErrorMessage(t));
            }
        });
    }

    /**
     * Fetch hourly forecast by city name
     */
    public void fetchHourlyForecast(String cityName, String temperatureUnit, ForecastCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Call<HourlyForecastResponse> call = apiService.getHourlyForecast(cityName, apiKey, units);

        call.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onForecastSuccess(response.body());
                } else {
                    callback.onForecastError("Could not load hourly forecast");
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                callback.onForecastError("Could not load hourly forecast");
            }
        });
    }

    /**
     * Fetch hourly forecast by coordinates
     */
    public void fetchHourlyForecastByCoordinates(double lat, double lon, String temperatureUnit, ForecastCallback callback) {
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Call<HourlyForecastResponse> call = apiService.getHourlyForecastByCoordinates(lat, lon, apiKey, units);

        call.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onForecastSuccess(response.body());
                } else {
                    callback.onForecastError("Could not load hourly forecast");
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                callback.onForecastError("Could not load hourly forecast");
            }
        });
    }

    /**
     * Fetch UV index
     */
    public void fetchUVIndex(double lat, double lon, UVIndexCallback callback) {
        if (lat == 0 && lon == 0) {
            callback.onUVIndexError();
            return;
        }

        Call<UVIndexResponse> call = apiService.getUVIndex(lat, lon, apiKey);
        call.enqueue(new Callback<UVIndexResponse>() {
            @Override
            public void onResponse(Call<UVIndexResponse> call, Response<UVIndexResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int uvIndex = (int) Math.round(response.body().getValue());
                    callback.onUVIndexSuccess(uvIndex);
                } else {
                    callback.onUVIndexError();
                }
            }

            @Override
            public void onFailure(Call<UVIndexResponse> call, Throwable t) {
                callback.onUVIndexError();
            }
        });
    }

    /**
     * Fetch air quality
     */
    public void fetchAirQuality(double lat, double lon, AirQualityCallback callback) {
        if (lat == 0 && lon == 0) {
            callback.onAirQualityError();
            return;
        }

        Call<AirQualityResponse> call = apiService.getAirQuality(lat, lon, apiKey);
        call.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getList() != null && !response.body().getList().isEmpty()) {
                    callback.onAirQualitySuccess(response.body().getList().get(0));
                } else {
                    callback.onAirQualityError();
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                callback.onAirQualityError();
            }
        });
    }

    // ============ Helper Methods ============

    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Kết nối bị timeout. Vui lòng kiểm tra:\n• Tốc độ mạng của bạn\n• Thử lại sau vài giây";
        } else if (t instanceof java.net.UnknownHostException) {
            return "Không thể kết nối đến server. Vui lòng kiểm tra kết nối internet.";
        } else if (t instanceof java.net.ConnectException) {
            return "Không thể kết nối. Vui lòng kiểm tra:\n• Bạn đã bật internet chưa\n• Thử chuyển đổi giữa WiFi/4G";
        } else if (t instanceof java.io.IOException) {
            return "Lỗi kết nối mạng. Vui lòng thử lại.";
        } else {
            return "Lỗi: " + t.getMessage() + "\nVui lòng thử lại sau.";
        }
    }
}
