package com.example.weatherapp.data.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    private RetrofitClient() {
        // Cấu hình OkHttpClient với timeout để tránh network timeout
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Timeout khi kết nối - 30 giây
                .readTimeout(30, TimeUnit.SECONDS)    // Timeout khi đọc dữ liệu - 30 giây
                .writeTimeout(30, TimeUnit.SECONDS)   // Timeout khi ghi dữ liệu - 30 giây
                .retryOnConnectionFailure(true)       // Tự động retry khi lỗi kết nối
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)  // Thêm OkHttpClient với timeout config
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public WeatherApiService getWeatherApi() {
        return retrofit.create(WeatherApiService.class);
    }
}

