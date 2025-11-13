package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.databinding.ActivityActivitySuggestionsBinding;
import com.example.weatherapp.domain.model.ActivitySuggestion;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.services.ActivitySuggestionService;
import com.example.weatherapp.ui.adapters.ActivitySuggestionAdapter;
import com.example.weatherapp.utils.CalendarHelper;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity Suggestions Activity
 * Shows weather-based activity recommendations using Gemini AI
 */
public class ActivitySuggestionsActivity extends AppCompatActivity 
        implements ActivitySuggestionAdapter.OnActivityClickListener {

    private static final String TAG = "ActivitySuggestions";
    private ActivityActivitySuggestionsBinding binding;
    private ActivitySuggestionAdapter adapter;
    private ActivitySuggestionService suggestionService;
    private WeatherData currentWeather;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Calendar permission launcher
    private final ActivityResultLauncher<String> calendarPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Calendar permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Calendar permission denied. Using system calendar picker instead.", 
                                 Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActivitySuggestionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        setupRecyclerView();
        setupClickListeners();
        loadWeatherData();
        loadSuggestions();
    }

    private void initializeComponents() {
        suggestionService = ActivitySuggestionService.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new ActivitySuggestionAdapter(this, this);
        binding.recyclerActivities.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerActivities.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // iOS-style back button
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnRefresh.setOnClickListener(v -> {
            loadSuggestions();
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            loadSuggestions();
        });
    }

    private int currentUvIndex = 5; // Default
    private int currentAqi = 50; // Default

    private void loadWeatherData() {
        // Get weather data from SharedPreferences or Intent
        Intent intent = getIntent();
        if (intent.hasExtra("weather_data")) {
            String weatherJson = intent.getStringExtra("weather_data");
            currentWeather = new Gson().fromJson(weatherJson, WeatherData.class);
            
            // Get UV Index from Intent
            if (intent.hasExtra("uv_index")) {
                currentUvIndex = intent.getIntExtra("uv_index", 5);
            }
            
            // Get AQI from Intent
            if (intent.hasExtra("aqi")) {
                currentAqi = intent.getIntExtra("aqi", 50);
            }
        } else {
            // Load from SharedPreferences (cached weather)
            SharedPreferences prefs = getSharedPreferences("WeatherApp", MODE_PRIVATE);
            String weatherJson = prefs.getString("cached_weather_data", null);
            if (weatherJson != null) {
                currentWeather = new Gson().fromJson(weatherJson, WeatherData.class);
            }
        }

        if (currentWeather != null) {
            updateWeatherDisplay();
        }
    }

    private void updateWeatherDisplay() {
        binding.txtTemperature.setText(String.format("%.0f°C", currentWeather.getTemperature()));
        binding.txtWeatherCondition.setText(currentWeather.getWeatherDescription());
        binding.txtLocation.setText("📍 " + currentWeather.getCityName());
        
        // Weather details with real UV Index
        String details = String.format(
            "💧 %d%% • 💨 %.1f m/s • ☀️ UV %d",
            currentWeather.getHumidity(),
            currentWeather.getWindSpeed(),
            currentUvIndex
        );
        binding.txtWeatherDetails.setText(details);
    }

    private void loadSuggestions() {
        if (currentWeather == null) {
            Toast.makeText(this, "No weather data available", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        
        // Log the data being sent
        Log.d(TAG, "==============================================");
        Log.d(TAG, "📊 LOADING ACTIVITY SUGGESTIONS");
        Log.d(TAG, "==============================================");
        Log.d(TAG, "City: " + currentWeather.getCityName());
        Log.d(TAG, "Temperature: " + currentWeather.getTemperature() + "°C");
        Log.d(TAG, "Feels Like: " + currentWeather.getFeelsLike() + "°C");
        Log.d(TAG, "Humidity: " + currentWeather.getHumidity() + "%");
        Log.d(TAG, "Wind Speed: " + currentWeather.getWindSpeed() + " m/s");
        Log.d(TAG, "Condition: " + currentWeather.getWeatherDescription());
        Log.d(TAG, "UV Index: " + currentUvIndex);
        Log.d(TAG, "AQI: " + currentAqi);
        Log.d(TAG, "==============================================");

        executorService.execute(() -> {
            try {
                // Call Gemini AI to get suggestions with real UV and AQI
                List<ActivitySuggestion> suggestions = 
                    suggestionService.getActivitySuggestions(currentWeather, currentUvIndex, currentAqi);

                mainHandler.post(() -> {
                    hideLoading();
                    if (suggestions != null && !suggestions.isEmpty()) {
                        adapter.setSuggestions(suggestions);
                        binding.txtEmptyState.setVisibility(View.GONE);
                        binding.recyclerActivities.setVisibility(View.VISIBLE);
                        
                        // Show toast to indicate source
                        Toast.makeText(this, 
                            "✨ " + suggestions.size() + " activities suggested", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        showEmptyState();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "❌ Error loading suggestions: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    hideLoading();
                    Toast.makeText(this, "Error loading suggestions: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerActivities.setVisibility(View.GONE);
        binding.txtEmptyState.setVisibility(View.GONE);
        binding.btnRefresh.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.swipeRefresh.setRefreshing(false);
        binding.btnRefresh.setEnabled(true);
    }

    private void showEmptyState() {
        binding.txtEmptyState.setVisibility(View.VISIBLE);
        binding.recyclerActivities.setVisibility(View.GONE);
    }

    @Override
    public void onActivityClick(ActivitySuggestion suggestion) {
        // Show detail dialog or toast
        String message = String.format(
            "%s\n\n%s\n\nScore: %d%%\nBest time: %s",
            suggestion.getTitle(),
            suggestion.getDescription(),
            suggestion.getSuitabilityScore(),
            suggestion.getBestTime()
        );
        
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAddToCalendarClick(ActivitySuggestion suggestion) {
        // Check calendar permission (for Android 6.0+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR);
        }
        
        // Add to calendar (uses Intent, works without permission)
        CalendarHelper.addActivityToCalendar(this, suggestion);
        
        Toast.makeText(this, 
            "Adding '" + suggestion.getTitle() + "' to calendar...", 
            Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
