package com.example.weatherapp.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.models.OutfitSuggestion;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.services.OutfitSuggestionService;
import com.example.weatherapp.presentation.viewmodel.OutfitSuggestionViewModel;
import com.example.weatherapp.ui.adapters.OutfitSuggestionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class OutfitSuggestionActivity extends AppCompatActivity {

    // Progress bar hiển thị khi đang load gợi ý từ AI
    private ProgressBar progressBar;

    // TextView hiển thị thông tin thời tiết (nhiệt độ, tình trạng)
    private TextView tvWeatherInfo;

    // Icon hiển thị điều kiện thời tiết (mưa/nắng/tuyết...)
    private ImageView ivWeatherIcon;

    // Layout chứa nội dung chính (ẩn khi đang load)
    private View layoutContent;

    // Dữ liệu thời tiết nhận từ MainActivity
    private WeatherResponse weatherData;

    // Service xử lý logic gọi AI và phân tích kết quả
    private OutfitSuggestionService outfitService;

    // ViewModel manages state and AI suggestions
    private OutfitSuggestionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_suggestion);

        // === INITIALIZE VIEWMODEL ===
        viewModel = new ViewModelProvider(this).get(OutfitSuggestionViewModel.class);

        // === KHỞI TẠO CÁC VIEW ===
        RecyclerView rvOutfitSuggestions = findViewById(R.id.rvOutfitSuggestions);
        progressBar = findViewById(R.id.progressBar);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        ImageView btnBack = findViewById(R.id.btnBack);
        layoutContent = findViewById(R.id.layoutContent);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // === SETUP RECYCLERVIEW ===
        // RecyclerView hiển thị danh sách các gợi ý trang phục
        // Mỗi item bao gồm: icon, tên trang phục, mô tả
        rvOutfitSuggestions.setLayoutManager(new LinearLayoutManager(this));
        OutfitSuggestionAdapter adapter = new OutfitSuggestionAdapter(new ArrayList<>());
        rvOutfitSuggestions.setAdapter(adapter);

        // === NHẬN DỮ LIỆU THỜI TIẾT TỪ INTENT ===
        // MainActivity gửi dữ liệu thời tiết qua Intent khi user bấm nút "Outfit Suggestion"
        // Receive WeatherData from Intent and convert to WeatherResponse
        com.example.weatherapp.domain.model.WeatherData domainWeatherData = 
            (com.example.weatherapp.domain.model.WeatherData) getIntent().getSerializableExtra("weather_data");

        // Kiểm tra dữ liệu có tồn tại không
        if (domainWeatherData == null) {
            Toast.makeText(this, "No weather data available", Toast.LENGTH_SHORT).show();
            finish();  // Đóng Activity nếu không có dữ liệu
            return;
        }
        
        // Convert WeatherData to WeatherResponse for OutfitSuggestionService
        weatherData = convertToWeatherResponse(domainWeatherData);
        
        if (weatherData == null) {
            Toast.makeText(this, "Failed to process weather data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // === SETUP OBSERVERS ===
        setupObservers(adapter);

        // Setup nút Back để quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // === KHỞI TẠO SERVICE VÀ LOAD DATA ===
        // OutfitSuggestionService chứa logic:
        // - Tạo prompt (câu hỏi) gửi cho AI
        // - Gọi Gemini API
        // - Parse (phân tích) kết quả JSON trả về
        outfitService = new OutfitSuggestionService(this);
        viewModel.initService(outfitService);

        // Load weather info and fetch outfit suggestions
        viewModel.loadWeatherInfo(weatherData);
        viewModel.fetchOutfitSuggestions(weatherData);
    }

    /**
     * SETUP LIVEDATA OBSERVERS
     * Observes ViewModel state changes and updates UI accordingly
     */
    private void setupObservers(OutfitSuggestionAdapter adapter) {
        // Observe weather info state
        viewModel.getWeatherInfoState().observe(this, weatherInfo -> {
            if (weatherInfo != null) {
                // Display: "Hanoi - 25°C, partly cloudy"
                tvWeatherInfo.setText(String.format(Locale.getDefault(),
                    "%s - %.0f°C, %s", 
                    weatherInfo.cityName, 
                    weatherInfo.temperature, 
                    weatherInfo.condition));

                // Set weather icon
                ivWeatherIcon.setImageResource(getWeatherIconResource(weatherInfo.weatherMain));
            }
        });

        // Observe outfit suggestions state (Loading/Success/Error)
        viewModel.getOutfitSuggestionsState().observe(this, uiState -> {
            if (uiState.isLoading()) {
                // Show loading state
                progressBar.setVisibility(View.VISIBLE);
                layoutContent.setVisibility(View.GONE);
            } else if (uiState.isSuccess()) {
                // Show success state with data
                progressBar.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                
                List<OutfitSuggestion> suggestions = uiState.getData();
                if (suggestions != null && !suggestions.isEmpty()) {
                    adapter.updateSuggestions(suggestions);
                } else {
                    Toast.makeText(this, "No outfit suggestions available", Toast.LENGTH_SHORT).show();
                }
            } else if (uiState.isError()) {
                // Show error state
                progressBar.setVisibility(View.GONE);
                layoutContent.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Error: " + uiState.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



    /**
     * LẤY ICON TƯƠNG ỨNG VỚI ĐIỀU KIỆN THỜI TIẾT
     *
     * Map các điều kiện thời tiết sang drawable resource
     *
     * @param weatherCondition Điều kiện thời tiết (clear, clouds, rain...)
     * @return Resource ID của icon drawable
     */
    private int getWeatherIconResource(String weatherCondition) {
        // Map weather condition -> drawable resource
        switch (weatherCondition) {
            case "clear":
                // Trời quang đãng
                return R.drawable.sun_cloud_little_rain;

            case "clouds":
                // Trời nhiều mây
                return R.drawable.moon_cloud_mid_rain;

            case "rain":
                // Mưa to
                return R.drawable.big_rain_drops;

            case "drizzle":
                // Mưa phùn
                return R.drawable.sun_cloud_mid_rain;

            case "thunderstorm":
                // Giông bão
                return R.drawable.cloud_3_zap;

            case "snow":
                // Tuyết
                return R.drawable.big_snow;

            case "mist":
            case "fog":
            case "haze":
                // Sương mù
                return R.drawable.moon_cloud_fast_wind;

            case "tornado":
                // Lốc xoáy
                return R.drawable.moon_fast_wind;

            default:
                // Mặc định
                return R.drawable.sun_cloud_angled_rain;
        }
    }
    
    /**
     * Convert WeatherData (domain model) to WeatherResponse (for OutfitSuggestionService)
     * This is necessary because OutfitSuggestionService expects WeatherResponse format
     * Uses Gson to create the JSON representation and deserialize back
     */
    private WeatherResponse convertToWeatherResponse(com.example.weatherapp.domain.model.WeatherData data) {
        try {
            // Build a JSON structure matching WeatherResponse format
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            
            // Main weather info (keep in Celsius - do NOT convert to Kelvin)
            com.google.gson.JsonObject main = new com.google.gson.JsonObject();
            main.addProperty("temp", data.getTemperature()); // Keep as Celsius
            main.addProperty("feels_like", data.getFeelsLike());
            main.addProperty("humidity", data.getHumidity());
            main.addProperty("pressure", (int) data.getPressure());
            main.addProperty("temp_min", data.getMinTemperature());
            main.addProperty("temp_max", data.getMaxTemperature());
            json.add("main", main);
            
            // Wind info
            com.google.gson.JsonObject wind = new com.google.gson.JsonObject();
            wind.addProperty("speed", data.getWindSpeed());
            wind.addProperty("deg", data.getWindDegree());
            json.add("wind", wind);
            
            // Weather condition array
            com.google.gson.JsonArray weatherArray = new com.google.gson.JsonArray();
            com.google.gson.JsonObject weather = new com.google.gson.JsonObject();
            weather.addProperty("main", data.getWeatherMain());
            weather.addProperty("description", data.getWeatherDescription());
            weather.addProperty("icon", data.getWeatherIcon());
            weatherArray.add(weather);
            json.add("weather", weatherArray);
            
            // Clouds
            com.google.gson.JsonObject clouds = new com.google.gson.JsonObject();
            clouds.addProperty("all", data.getCloudiness());
            json.add("clouds", clouds);
            
            // City name
            json.addProperty("name", data.getCityName());
            
            // Visibility
            json.addProperty("visibility", (int) data.getVisibility());
            
            // Timestamp
            json.addProperty("dt", data.getTimestamp());
            
            // Coordinates
            com.google.gson.JsonObject coord = new com.google.gson.JsonObject();
            coord.addProperty("lat", data.getLatitude());
            coord.addProperty("lon", data.getLongitude());
            json.add("coord", coord);
            
            // Sys info
            com.google.gson.JsonObject sys = new com.google.gson.JsonObject();
            sys.addProperty("country", data.getCountryCode());
            sys.addProperty("sunrise", data.getSunrise());
            sys.addProperty("sunset", data.getSunset());
            json.add("sys", sys);
            
            // Rain (if available)
            if (data.getRainVolume() != null) {
                com.google.gson.JsonObject rain = new com.google.gson.JsonObject();
                rain.addProperty("1h", data.getRainVolume());
                json.add("rain", rain);
            }
            
            // Convert JSON to WeatherResponse using Gson
            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(json, WeatherResponse.class);
            
        } catch (Exception e) {
            Log.e("OutfitSuggestionActivity", "Error converting WeatherData to WeatherResponse", e);
            return null;
        }
    }
}
