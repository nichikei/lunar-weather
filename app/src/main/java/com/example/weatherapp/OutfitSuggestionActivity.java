package com.example.weatherapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OutfitSuggestionActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvWeatherInfo;
    private ImageView ivWeatherIcon;
    private View layoutContent;

    private WeatherResponse weatherData;
    private OutfitSuggestionService outfitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_suggestion);

        // Initialize views
        RecyclerView rvOutfitSuggestions = findViewById(R.id.rvOutfitSuggestions);
        progressBar = findViewById(R.id.progressBar);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        ImageView btnBack = findViewById(R.id.btnBack);
        layoutContent = findViewById(R.id.layoutContent);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // Setup RecyclerView
        rvOutfitSuggestions.setLayoutManager(new LinearLayoutManager(this));
        OutfitSuggestionAdapter adapter = new OutfitSuggestionAdapter(new ArrayList<>());
        rvOutfitSuggestions.setAdapter(adapter);

        // Get weather data from intent
        weatherData = (WeatherResponse) getIntent().getSerializableExtra("weather_data");

        if (weatherData == null) {
            Toast.makeText(this, "No weather data available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup back button
        btnBack.setOnClickListener(v -> finish());

        // Display weather info
        displayWeatherInfo();

        // Initialize service and fetch suggestions
        outfitService = new OutfitSuggestionService(this);
        fetchOutfitSuggestions(adapter);
    }

    private void displayWeatherInfo() {
        double temp = weatherData.getMain().getTemp();
        String condition = weatherData.getWeather().get(0).getDescription();
        String cityName = weatherData.getName();

        tvWeatherInfo.setText(String.format(Locale.getDefault(), "%s - %.0f°C, %s", cityName, temp, condition));

        // Set weather icon - use existing drawable resources
        String weatherCondition = weatherData.getWeather().get(0).getMain().toLowerCase();
        ivWeatherIcon.setImageResource(getWeatherIconResource(weatherCondition));
    }

    private void fetchOutfitSuggestions(OutfitSuggestionAdapter adapter) {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        outfitService.getOutfitSuggestions(weatherData, new OutfitSuggestionService.OutfitSuggestionCallback() {
            @Override
            public void onSuccess(List<OutfitSuggestion> suggestions) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);
                    adapter.updateSuggestions(suggestions);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);
                    Toast.makeText(OutfitSuggestionActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private int getWeatherIconResource(String weatherCondition) {
        // Map weather conditions to existing drawable resources
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
            case "tornado":
                return R.drawable.moon_fast_wind;
            default:
                return R.drawable.sun_cloud_angled_rain;
        }
    }
}
