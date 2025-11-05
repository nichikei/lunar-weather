package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.R;
import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.data.models.HourlyForecast;
import com.example.weatherapp.data.models.WeatherAlert;
import com.example.weatherapp.data.models.WeeklyForecast;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.UVIndexResponse;
import com.example.weatherapp.data.responses.WeatherAlertsResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.notification.WeatherNotificationWorker;
import com.example.weatherapp.utils.BlurHelper;
import com.example.weatherapp.utils.FavoriteCitiesManager;
import com.example.weatherapp.utils.LocaleHelper;
import com.example.weatherapp.widget.WeatherWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    private boolean isSearchVisible = false;
    private boolean isHourlyView = true;
    private WeatherResponse currentWeatherData;
    private HourlyForecastResponse hourlyForecastData;
    private String currentCityName = "Hanoi";
    private double currentLat = 0;
    private double currentLon = 0;
    private int currentUVIndex = 0;
    private AirQualityResponse.AirQualityData currentAQIData; // Add this
    private SharedPreferences sharedPreferences;
    private FavoriteCitiesManager favoritesManager; // Add favorite cities manager
    private List<WeatherAlert> currentAlerts; // Add weather alerts list

    // Unit settings
    private String temperatureUnit = "celsius";
    private String windSpeedUnit = "ms";
    private String pressureUnit = "hpa";

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    // Notification permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted - schedule notifications
                    scheduleWeatherNotifications();
                    Toast.makeText(this, "Weather notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    // Activity Result API (modern way - no memory leak)
    private final ActivityResultLauncher<Intent> searchLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Check if using GPS location
                    boolean useGPS = result.getData().getBooleanExtra(SearchActivity.EXTRA_USE_GPS, false);

                    if (useGPS) {
                        // Get coordinates from GPS
                        double latitude = result.getData().getDoubleExtra(SearchActivity.EXTRA_LATITUDE, 0);
                        double longitude = result.getData().getDoubleExtra(SearchActivity.EXTRA_LONGITUDE, 0);

                        if (latitude != 0 && longitude != 0) {
                            // Fetch weather by coordinates
                            fetchWeatherByCoordinates(latitude, longitude);
                        }
                    } else {
                        // Get city name from search
                        String cityName = result.getData().getStringExtra(SearchActivity.EXTRA_CITY_NAME);
                        if (cityName != null && !cityName.isEmpty()) {
                            currentCityName = cityName;
                            fetchAllWeatherData(cityName);
                        }
                    }
                }
            });

    // Settings launcher
    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Check if language changed
                    boolean languageChanged = result.getData() != null &&
                            result.getData().getBooleanExtra("language_changed", false);

                    if (languageChanged) {
                        // Language changed - recreate MainActivity to apply new language
                        recreate();
                        return;
                    }

                    // Store old temperature unit to check if it changed
                    String oldTempUnit = temperatureUnit;

                    // Reload settings
                    loadSettings();

                    // If temperature unit changed, refetch data from API with correct unit
                    if (!oldTempUnit.equals(temperatureUnit)) {
                        // Temperature unit changed - need to fetch new data
                        fetchAllWeatherData(currentCityName);
                    } else {
                        // Only wind/pressure unit changed - just update UI
                        if (currentWeatherData != null) {
                            updateUI(currentWeatherData);
                        }
                        if (hourlyForecastData != null) {
                            updateForecastView();
                        }
                    }
                }
            });

    // Location permission launcher
    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted - get location
                    getCurrentLocation();
                } else {
                    // Permission denied
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved language first
        applyLanguagePreference();

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize favorites manager
        favoritesManager = new FavoriteCitiesManager(this);

        // Load settings
        loadSettings();

        // Request notification permission for Android 13+
        checkNotificationPermission();

        // Schedule weather notifications
        scheduleWeatherNotifications();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();

        // Apply glass morphism effects to UI elements
        applyGlassMorphismEffects();

        // Apply blur effect to top glass bar (API 31+)
        applyTopBarBlurEffect();

        fetchAllWeatherData(currentCityName);
    }

    /**
     * Apply blur effect to the glass capsule top bar using native Android blur
     * Uses setBackgroundBlurRadius (Android 12+) - only blurs backdrop, content stays sharp
     */
    private void applyTopBarBlurEffect() {
        View topGlassBar = findViewById(R.id.topGlassBar);
        if (topGlassBar == null) return;

        // Calculate corner radius in pixels
        float density = getResources().getDisplayMetrics().density;
        float cornerRadiusPx = 32f * density; // 32dp corner radius

        // Set outline provider for rounded corners
        topGlassBar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        topGlassBar.setClipToOutline(true);

        // Create glass morphism background with higher opacity for better visibility
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(android.graphics.Color.argb(40, 255, 255, 255)); // ~16% white opacity - more visible
        background.setCornerRadius(cornerRadiusPx);
        background.setStroke((int)(1.5f * density), android.graphics.Color.argb(80, 255, 255, 255)); // ~31% white border
        topGlassBar.setBackground(background);

        // Set elevation for depth
        topGlassBar.setElevation(12f * density);
    }

    private void loadSettings() {
        temperatureUnit = SettingsActivity.getTemperatureUnit(sharedPreferences);
        windSpeedUnit = SettingsActivity.getWindSpeedUnit(sharedPreferences);
        pressureUnit = SettingsActivity.getPressureUnit(sharedPreferences);
    }

    private void applyLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", "vi"); // Fixed: Changed default from "en" to "vi"
        LocaleHelper.setLocale(this, language);
    }

    private void setupListeners() {
        // Top Bar Glass Capsule - Settings Icon
        binding.btnSettingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            settingsLauncher.launch(intent);
        });

        // Top Bar Glass Capsule - Search Icon
        binding.btnSearchIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            searchLauncher.launch(intent);
        });

        // Top Bar City Name - tap to search location
        binding.tvTopBarCityName.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            searchLauncher.launch(intent);
        });


        // Search button click
        binding.btnSearch.setOnClickListener(v -> {
            String cityName = binding.etCityName.getText().toString().trim();
            if (!cityName.isEmpty()) {
                currentCityName = cityName;
                fetchAllWeatherData(cityName);
                toggleSearchBar();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        // Current Location button click - NEW
        binding.btnCurrentLocation.setOnClickListener(v -> {
            checkLocationPermissionAndGetLocation();
            toggleSearchBar();
        });

        // View Charts button click - NEW
        binding.btnViewCharts.setOnClickListener(v -> {
            openChartsActivity();
        });

        // Outfit Suggestion button click - NEW
        binding.btnOutfitSuggestion.setOnClickListener(v -> {
            openOutfitSuggestionActivity();
        });

        // Hourly/Weekly toggle buttons
        binding.btnHourly.setOnClickListener(v -> {
            if (!isHourlyView) {
                isHourlyView = true;
                animateTabSelection(true);
                updateForecastView();
            }
        });

        binding.btnWeekly.setOnClickListener(v -> {
            if (isHourlyView) {
                isHourlyView = false;
                animateTabSelection(false);
                updateForecastView();
            }
        });
    }

    private void toggleSearchBar() {
        isSearchVisible = !isSearchVisible;
        binding.searchContainer.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Apply glass morphism effects to UI elements for a modern, translucent look
     * BLUR EFFECT ĐÃ TẮT - chỉ dùng background drawable để UI rõ ràng
     */
    private void applyGlassMorphismEffects() {
        // TẮT blur effect - gây mờ quá mức
        // Chỉ dùng glass panel background (semi-transparent white)
        // Background drawables already have overlay #33FFFFFF and stroke #4DFFFFFF

        // KHÔNG apply blur nữa để UI rõ ràng
        /* DISABLED - causes blur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            applyBlurEffect(binding.searchContainer);

            // Apply blur to the main forecast card container
            View forecastCard = findViewById(R.id.weatherDetailsSection);
            if (forecastCard != null) {
                applyBlurEffect(forecastCard);
            }
        }
        */
    }

    /**
     * Apply blur effect to a view (API 31+)
     * DISABLED - causes too much blur
     */
    private void applyBlurEffect(View view) {
        // KHÔNG apply blur để UI rõ ràng
        /* DISABLED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.graphics.RenderEffect blurEffect = android.graphics.RenderEffect.createBlurEffect(
                    10f, 10f, android.graphics.Shader.TileMode.CLAMP
            );
            view.setRenderEffect(blurEffect);
        }
        */
    }

    private void fetchAllWeatherData(String cityName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);

        // Save current city to preferences for notifications
        sharedPreferences.edit().putString("last_city", cityName).apply();

        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();

        // Use metric for Celsius, imperial for Fahrenheit
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";

        // Log API call for debugging
        android.util.Log.d("WeatherApp", "Fetching weather for city: " + cityName + ", units: " + units + ", API_KEY: " + API_KEY.substring(0, 8) + "...");

        // Fetch current weather
        Call<WeatherResponse> weatherCall = apiService.getWeatherByCity(cityName, API_KEY, units);
        weatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                android.util.Log.d("WeatherApp", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    currentWeatherData = response.body();
                    android.util.Log.d("WeatherApp", "Weather data received for: " + currentWeatherData.getName());

                    // Get coordinates for UV Index API
                    if (currentWeatherData.getCoord() != null) {
                        currentLat = currentWeatherData.getCoord().getLat();
                        currentLon = currentWeatherData.getCoord().getLon();
                    }

                    updateUI(currentWeatherData);

                    // After current weather, fetch hourly forecast, UV index, and Air Quality
                    fetchHourlyForecast(cityName);
                    fetchUVIndex(currentLat, currentLon);
                    fetchAirQuality(currentLat, currentLon);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    String errorMsg = "City not found";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                            android.util.Log.e("WeatherApp", "Error response: " + errorMsg);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("WeatherApp", "Error reading error body", e);
                    }
                    showError("City not found. Please check the city name and try again.");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                android.util.Log.e("WeatherApp", "Network error", t);
                binding.progressBar.setVisibility(View.GONE);
                showError(getNetworkErrorMessage(t));
            }
        });
    }

    private void fetchUVIndex(double lat, double lon) {
        if (lat == 0 && lon == 0) return;

        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        Call<UVIndexResponse> uvCall = apiService.getUVIndex(lat, lon, API_KEY);

        uvCall.enqueue(new Callback<UVIndexResponse>() {
            @Override
            public void onResponse(Call<UVIndexResponse> call, Response<UVIndexResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUVIndex = (int) Math.round(response.body().getValue());
                    updateUVIndexCard();
                } else {
                    // Keep default UV index if API fails
                    currentUVIndex = 0;
                    updateUVIndexCard();
                }
            }

            @Override
            public void onFailure(Call<UVIndexResponse> call, Throwable t) {
                // Keep default UV index if API fails
                currentUVIndex = 0;
                updateUVIndexCard();
            }
        });
    }

    private void updateUVIndexCard() {
        String uvDesc;
        if (currentUVIndex < 3) {
            uvDesc = "Low";
        } else if (currentUVIndex < 6) {
            uvDesc = "Moderate";
        } else if (currentUVIndex < 8) {
            uvDesc = "High";
        } else if (currentUVIndex < 11) {
            uvDesc = "Very High";
        } else {
            uvDesc = "Extreme";
        }

        updateCard(R.id.cardUvIndex, "☀️", "UV INDEX",
                String.valueOf(currentUVIndex), uvDesc);
    }

    // NEW: Fetch Air Quality Index
    private void fetchAirQuality(double lat, double lon) {
        if (lat == 0 && lon == 0) return;

        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        Call<AirQualityResponse> aqiCall = apiService.getAirQuality(lat, lon, API_KEY);

        aqiCall.enqueue(new Callback<AirQualityResponse>() {
            @Override
            public void onResponse(Call<AirQualityResponse> call, Response<AirQualityResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getList() != null && !response.body().getList().isEmpty()) {
                    currentAQIData = response.body().getList().get(0);
                    updateAirQualityCard();
                }
            }

            @Override
            public void onFailure(Call<AirQualityResponse> call, Throwable t) {
                // Keep default AQI if API fails
            }
        });
    }

    // NEW: Update Air Quality card
    private void updateAirQualityCard() {
        if (currentAQIData == null) return;

        View aqiCard = findViewById(R.id.card_air_quality);
        if (aqiCard == null) return;

        int aqi = currentAQIData.getMain().getAqi();
        String status;
        String description;

        switch (aqi) {
            case 1:
                status = "Good";
                description = "Air quality is satisfactory";
                break;
            case 2:
                status = "Fair";
                description = "Air quality is acceptable";
                break;
            case 3:
                status = "Moderate";
                description = "Sensitive groups may be affected";
                break;
            case 4:
                status = "Poor";
                description = "Everyone may experience health effects";
                break;
            case 5:
                status = "Very Poor";
                description = "Health alert: everyone may be affected";
                break;
            default:
                status = "Unknown";
                description = "No data available";
        }

        TextView tvAqiValue = aqiCard.findViewById(R.id.tvAqiValue);
        TextView tvAqiStatus = aqiCard.findViewById(R.id.tvAqiStatus);
        TextView tvAqiDescription = aqiCard.findViewById(R.id.tvAqiDescription);
        TextView tvPm25 = aqiCard.findViewById(R.id.tvPm25);
        TextView tvPm10 = aqiCard.findViewById(R.id.tvPm10);
        TextView tvO3 = aqiCard.findViewById(R.id.tvO3);

        if (tvAqiValue != null) tvAqiValue.setText(String.valueOf(aqi));
        if (tvAqiStatus != null) tvAqiStatus.setText(status);
        if (tvAqiDescription != null) tvAqiDescription.setText(description);

        if (currentAQIData.getComponents() != null) {
            if (tvPm25 != null) tvPm25.setText(String.format(Locale.getDefault(), "%.1f", currentAQIData.getComponents().getPm2_5()));
            if (tvPm10 != null) tvPm10.setText(String.format(Locale.getDefault(), "%.1f", currentAQIData.getComponents().getPm10()));
            if (tvO3 != null) tvO3.setText(String.format(Locale.getDefault(), "%.1f", currentAQIData.getComponents().getO3()));
        }
    }

    private void fetchHourlyForecast(String cityName) {
        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Call<HourlyForecastResponse> hourlyCall = apiService.getHourlyForecast(cityName, API_KEY, units);

        hourlyCall.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    hourlyForecastData = response.body();
                    updateForecastView();
                    binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                } else {
                    // If hourly forecast fails, still show weather details
                    binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Could not load hourly forecast", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Could not load hourly forecast", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NEW: Fetch hourly forecast by GPS coordinates
    private void fetchHourlyForecastByCoordinates(double lat, double lon) {
        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";
        Call<HourlyForecastResponse> hourlyCall = apiService.getHourlyForecastByCoordinates(lat, lon, API_KEY, units);

        hourlyCall.enqueue(new Callback<HourlyForecastResponse>() {
            @Override
            public void onResponse(Call<HourlyForecastResponse> call, Response<HourlyForecastResponse> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    hourlyForecastData = response.body();
                    updateForecastView();
                    binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                } else {
                    // If hourly forecast fails, still show weather details
                    binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this, "Could not load hourly forecast", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HourlyForecastResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Could not load hourly forecast", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NEW: Open Charts Activity
    private void openChartsActivity() {
        if (hourlyForecastData == null || currentWeatherData == null) {
            Toast.makeText(this, "Weather data not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChartsActivity.class);
        intent.putExtra("hourly_data", hourlyForecastData);
        intent.putExtra("current_data", currentWeatherData);
        intent.putExtra("uv_index", currentUVIndex);
        startActivity(intent);
    }

    // NEW: Open Outfit Suggestion Activity
    private void openOutfitSuggestionActivity() {
        if (currentWeatherData == null) {
            Toast.makeText(this, "Weather data not available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, OutfitSuggestionActivity.class);
        intent.putExtra("weather_data", currentWeatherData);
        startActivity(intent);
    }

    private void updateForecastView() {
        if (currentWeatherData == null) return;

        if (isHourlyView) {
            if (hourlyForecastData != null) {
                createHourlyForecastFromAPI(hourlyForecastData);
            } else {
                // Show message that forecast data is not available
                showForecastUnavailable();
            }
        } else {
            if (hourlyForecastData != null) {
                createWeeklyForecastFromAPI(hourlyForecastData);
            } else {
                // Show message that forecast data is not available
                showForecastUnavailable();
            }
        }
    }

    private void showForecastUnavailable() {
        binding.hourlyForecastContainer.removeAllViews();
        // Could add a TextView here to show "Forecast data unavailable"
        Toast.makeText(this, "Forecast data is currently unavailable", Toast.LENGTH_SHORT).show();
    }

    private void createHourlyForecastFromAPI(HourlyForecastResponse forecastData) {
        binding.hourlyForecastContainer.removeAllViews();

        List<HourlyForecastResponse.HourlyItem> hourlyItems = forecastData.getList();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        boolean isFirst = true;

        // Show next 8 items (24 hours, since API returns 3-hour intervals)
        int itemsToShow = Math.min(8, hourlyItems.size());

        for (int i = 0; i < itemsToShow; i++) {
            HourlyForecastResponse.HourlyItem item = hourlyItems.get(i);

            View itemView;
            if (isFirst) {
                itemView = LayoutInflater.from(this).inflate(R.layout.item_hourly_forecast_now, binding.hourlyForecastContainer, false);
                isFirst = false;
            } else {
                itemView = LayoutInflater.from(this).inflate(R.layout.item_hourly_forecast, binding.hourlyForecastContainer, false);
            }

            TextView txtHour = itemView.findViewById(R.id.tvHour);
            ImageView ivHourlyIcon = itemView.findViewById(R.id.ivHourlyIcon);
            TextView tvHourlyTemp = itemView.findViewById(R.id.tvHourlyTemp);
            TextView tvRainProbability = itemView.findViewById(R.id.tvRainProbability);

            // Format time
            Calendar itemTime = Calendar.getInstance();
            itemTime.setTimeInMillis(item.getDt() * 1000);

            if (i == 0) {
                txtHour.setText("Now");
            } else {
                int hour = itemTime.get(Calendar.HOUR);
                if (hour == 0) hour = 12;
                String amPm = itemTime.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
                txtHour.setText(hour + amPm); // Removed space between hour and AM/PM
            }

            // Set weather icon
            String weatherCondition = item.getWeather().get(0).getMain().toLowerCase();
            ivHourlyIcon.setImageResource(getWeatherIconResource(weatherCondition));

            // Set temperature with unit symbol
            int temp = (int) Math.round(item.getMain().getTemp());
            String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
            tvHourlyTemp.setText(String.format(Locale.getDefault(), "%d%s", temp, tempSymbol));

            // Set rain probability
            if (tvRainProbability != null) {
                int rainProb = (int) (item.getPop() * 100);
                if (rainProb > 0 && i > 0) { // Don't show for "Now"
                    tvRainProbability.setText(String.format(Locale.getDefault(), "%d%%", rainProb));
                    tvRainProbability.setVisibility(View.VISIBLE);
                } else {
                    tvRainProbability.setVisibility(View.INVISIBLE);
                }
            }

            binding.hourlyForecastContainer.addView(itemView);
        }
    }

    private void createWeeklyForecastFromAPI(HourlyForecastResponse forecastData) {
        binding.hourlyForecastContainer.removeAllViews();

        List<HourlyForecastResponse.HourlyItem> hourlyItems = forecastData.getList();
        if (hourlyItems == null || hourlyItems.isEmpty()) {
            return;
        }

        String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
        Calendar calendar = Calendar.getInstance();

        // Group by day and get max temp for each day
        List<DailyData> dailyDataList = new ArrayList<>();
        String currentDay = "";
        DailyData currentDailyData = null;

        for (HourlyForecastResponse.HourlyItem item : hourlyItems) {
            calendar.setTimeInMillis(item.getDt() * 1000);
            String dayKey = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

            if (!dayKey.equals(currentDay)) {
                if (currentDailyData != null) {
                    dailyDataList.add(currentDailyData);
                }
                currentDailyData = new DailyData(dayKey);
                currentDay = dayKey;
            }

            currentDailyData.addItem(item);

            if (dailyDataList.size() >= 7) break;
        }

        if (currentDailyData != null && dailyDataList.size() < 7) {
            dailyDataList.add(currentDailyData);
        }

        // Display daily forecasts
        boolean isFirst = true;
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";

        for (DailyData dailyData : dailyDataList) {
            View itemView;

            if (isFirst) {
                itemView = LayoutInflater.from(this).inflate(R.layout.item_weekly_forecast_today, binding.hourlyForecastContainer, false);
                isFirst = false;
            } else {
                itemView = LayoutInflater.from(this).inflate(R.layout.item_weekly_forecast, binding.hourlyForecastContainer, false);
            }

            TextView tvDay = itemView.findViewById(R.id.tvDay);
            ImageView ivWeeklyIcon = itemView.findViewById(R.id.ivWeeklyIcon);
            TextView tvWeeklyTemp = itemView.findViewById(R.id.tvWeeklyTemp);
            TextView tvWeeklyRainProbability = itemView.findViewById(R.id.tvWeeklyRainProbability);

            tvDay.setText(dailyData.dayName);
            ivWeeklyIcon.setImageResource(getWeatherIconResource(dailyData.mainWeatherCondition));
            tvWeeklyTemp.setText(String.format(Locale.getDefault(), "%d%s", dailyData.maxTemp, tempSymbol));

            if (tvWeeklyRainProbability != null) {
                if (dailyData.maxRainProbability > 0) {
                    tvWeeklyRainProbability.setText(String.format(Locale.getDefault(), "%d%%", dailyData.maxRainProbability));
                    tvWeeklyRainProbability.setVisibility(View.VISIBLE);
                } else {
                    tvWeeklyRainProbability.setVisibility(View.INVISIBLE);
                }
            }

            binding.hourlyForecastContainer.addView(itemView);
        }
    }

    // Helper class to group hourly data by day
    private static class DailyData {
        String dayName;
        int maxTemp = Integer.MIN_VALUE;
        int maxRainProbability = 0;
        String mainWeatherCondition = "clear";

        DailyData(String dayName) {
            this.dayName = dayName;
        }

        void addItem(HourlyForecastResponse.HourlyItem item) {
            int temp = (int) Math.round(item.getMain().getTemp());
            if (temp > maxTemp) {
                maxTemp = temp;
                mainWeatherCondition = item.getWeather().get(0).getMain().toLowerCase();
            }

            int rainProb = (int) (item.getPop() * 100);
            if (rainProb > maxRainProbability) {
                maxRainProbability = rainProb;
            }
        }
    }


    private void updateUI(WeatherResponse weatherData) {
        // Update city name
        binding.tvCityName.setText(weatherData.getName());

        // Update top bar city name (the visible one)
        binding.tvTopBarCityName.setText(weatherData.getName());

        // Update weather description
        if (weatherData.getWeather() != null && !weatherData.getWeather().isEmpty()) {
            String description = weatherData.getWeather().get(0).getDescription();
            binding.tvWeatherDescription.setText(capitalizeWords(description));
        }

        // Update main temperature with correct unit
        double temp = weatherData.getMain().getTemp();
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
        binding.tvTemperature.setText(String.format(Locale.getDefault(), "%.0f%s", temp, tempSymbol));

        // Update temperature range
        double tempMax = weatherData.getMain().getTempMax();
        double tempMin = weatherData.getMain().getTempMin();
        binding.tvTempRange.setText(String.format(Locale.getDefault(),
                "H:%.0f%s   L:%.0f%s", tempMax, tempSymbol, tempMin, tempSymbol));

        // Update 10-Day Forecast Summary
        TextView tvForecastSummary = binding.getRoot().findViewById(R.id.tvForecastSummary);
        if (tvForecastSummary != null) {
            String weatherCondition = weatherData.getWeather().get(0).getMain().toLowerCase();
            String summary = generateForecastSummary(weatherCondition);
            tvForecastSummary.setText(summary);
        }

        // Update Weather Details Cards
        updateWeatherDetailsCards(weatherData);

        // Update widget
        updateWeatherWidget(weatherData);
    }

    private String generateForecastSummary(String weatherCondition) {
        // Use actual forecast data if available
        if (hourlyForecastData != null && hourlyForecastData.getList() != null) {
            // Group by day to count properly
            java.util.Map<Integer, Integer> rainyItemsPerDay = new java.util.HashMap<>();
            java.util.Map<String, Integer> conditionCounts = new java.util.HashMap<>();

            Calendar calendar = Calendar.getInstance();

            for (HourlyForecastResponse.HourlyItem item : hourlyForecastData.getList()) {
                if (item.getWeather() != null && !item.getWeather().isEmpty()) {
                    calendar.setTimeInMillis(item.getDt() * 1000);
                    int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

                    String condition = item.getWeather().get(0).getMain().toLowerCase();

                    // Count rain/snow items per day
                    if (condition.contains("rain") || condition.contains("drizzle") ||
                            condition.contains("thunderstorm")) {
                        rainyItemsPerDay.put(dayOfYear, rainyItemsPerDay.getOrDefault(dayOfYear, 0) + 1);
                    }

                    // Count condition occurrences
                    conditionCounts.put(condition, conditionCounts.getOrDefault(condition, 0) + 1);
                }
            }

            // Count how many days have significant rain (at least 2 items = 6 hours)
            int rainyDays = 0;
            for (int count : rainyItemsPerDay.values()) {
                if (count >= 2) { // At least 6 hours of rain
                    rainyDays++;
                }
            }

            // Find dominant condition
            String dominantCondition = "clear";
            int dominantCount = 0;
            for (java.util.Map.Entry<String, Integer> entry : conditionCounts.entrySet()) {
                if (entry.getValue() > dominantCount) {
                    dominantCount = entry.getValue();
                    dominantCondition = entry.getKey();
                }
            }

            int totalDays = hourlyForecastData.getList().size() / 8; // 8 items per day (3-hour intervals)
            int forecastDays = Math.min(totalDays, 5); // Show max 5 days

            // Generate summary based on actual data
            if (rainyDays >= 3) {
                return String.format(Locale.getDefault(),
                        "Rain expected %d out of %d days ahead",
                        rainyDays, forecastDays);
            } else if (rainyDays >= 1) {
                return String.format(Locale.getDefault(),
                        "Scattered rain in the next %d days",
                        forecastDays);
            } else if (dominantCondition.contains("snow")) {
                return "Possible snow in the coming days";
            } else if (dominantCondition.contains("clear")) {
                return "Clear skies this week";
            } else if (dominantCondition.contains("cloud")) {
                return "Mostly cloudy this week";
            } else {
                return "Changing weather in the coming days";
            }
        }

        // Fallback to simple summary (only if no API data)
        if (weatherCondition.contains("rain") || weatherCondition.contains("drizzle")) {
            return "Rain in the coming days";
        } else if (weatherCondition.contains("snow")) {
            return "Possible snow in the coming days";
        } else if (weatherCondition.contains("clear")) {
            return "Clear skies this week";
        } else if (weatherCondition.contains("cloud")) {
            return "Mostly cloudy this week";
        } else {
            return "Changing weather in the coming days";
        }
    }

    private void updateWeatherDetailsCards(WeatherResponse weatherData) {
        // Update Feels Like Card with correct unit
        String tempSymbol = temperatureUnit.equals("celsius") ? "°" : "°F";
        updateCard(R.id.cardFeelsLike, "🌡️", "FEELS LIKE",
                String.format(Locale.getDefault(), "%.0f%s", weatherData.getMain().getFeelsLike(), tempSymbol),
                "Similar to actual temp");

        // Update Humidity Card
        int humidity = weatherData.getMain().getHumidity();
        String humidityDesc = humidity > 70 ? "High level" : humidity > 40 ? "Comfortable" : "Low level";
        updateCard(R.id.cardHumidity, "💧", "HUMIDITY",
                String.format(Locale.getDefault(), "%d%%", humidity), humidityDesc);

        // Update Wind Card with unit conversion
        double windSpeed = weatherData.getWind().getSpeed();
        String windValue, windUnit;

        if (windSpeedUnit.equals("kmh")) {
            // Convert m/s to km/h (multiply by 3.6)
            if (temperatureUnit.equals("imperial")) {
                // API returns mph for imperial, convert to km/h
                windSpeed = windSpeed * 1.60934;
            } else {
                // API returns m/s for metric, convert to km/h
                windSpeed = windSpeed * 3.6;
            }
            windValue = String.format(Locale.getDefault(), "%.1f", windSpeed);
            windUnit = "km/h";
        } else {
            // Use m/s
            if (temperatureUnit.equals("imperial")) {
                // API returns mph for imperial, convert to m/s
                windSpeed = windSpeed * 0.44704;
            }
            windValue = String.format(Locale.getDefault(), "%.1f", windSpeed);
            windUnit = "m/s";
        }

        updateCard(R.id.cardWind, "💨", "WIND", windValue, windUnit);

        // Update Pressure Card with unit
        int pressure = weatherData.getMain().getPressure();
        String pressureUnitText = pressureUnit.equals("mbar") ? "mbar" : "hPa";
        updateCard(R.id.cardPressure, "📊", "PRESSURE",
                String.format(Locale.getDefault(), "%d", pressure), pressureUnitText);

        // UV Index will be updated by fetchUVIndex() callback
        updateUVIndexCard();

        // Update Sunrise/Sunset Card (using real data if available)
        long sunrise = weatherData.getSys() != null ? weatherData.getSys().getSunrise() : 0;
        long sunset = weatherData.getSys() != null ? weatherData.getSys().getSunset() : 0;
        String sunriseTime = formatTime(sunrise);
        String sunsetTime = formatTime(sunset);
        updateCard(R.id.cardSunrise, "🌅", "SUNRISE", sunriseTime, "Sunset: " + sunsetTime);

        // Update Visibility Card (using real data)
        int visibility = weatherData.getVisibility() != null ? weatherData.getVisibility() / 1000 : 10;
        updateCard(R.id.cardVisibility, "👁️", "VISIBILITY", String.valueOf(visibility), "km");

        // Update Rainfall Card (using real data if available)
        double rainfall = 0;
        if (weatherData.getRain() != null && weatherData.getRain().get1h() != null) {
            rainfall = weatherData.getRain().get1h();
        }
        updateCard(R.id.cardRainfall, "🌧️", "RAINFALL",
                String.format(Locale.getDefault(), "%.1f", rainfall), "mm last hour");
    }

    // Helper method to reduce code duplication
    private void updateCard(int cardId, String icon, String title, String value, String description) {
        View card = binding.getRoot().findViewById(cardId);
        if (card != null) {
            // For CardView with included layout, need to find the included layout first
            View contentView = card;
            if (card instanceof ViewGroup && ((ViewGroup) card).getChildCount() > 0) {
                contentView = ((ViewGroup) card).getChildAt(0);
            }

            TextView tvIcon = contentView.findViewById(R.id.tvDetailIcon);
            TextView tvTitle = contentView.findViewById(R.id.tvDetailTitle);
            TextView tvValue = contentView.findViewById(R.id.tvDetailValue);
            TextView tvDesc = contentView.findViewById(R.id.tvDetailDescription);

            if (tvIcon != null) tvIcon.setText(icon);
            if (tvTitle != null) tvTitle.setText(title);
            if (tvValue != null) tvValue.setText(value);
            if (tvDesc != null) {
                tvDesc.setText(description);
                tvDesc.setVisibility(View.VISIBLE);
            }
        }
    }

    private String formatTime(long timestamp) {
        if (timestamp == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(timestamp * 1000);
    }

    private void animateTabSelection(boolean isHourly) {
        View indicator = binding.getRoot().findViewById(R.id.tabIndicator);

        if (indicator == null) return;

        indicator.animate().cancel();

        if (isHourly) {
            binding.btnHourly.setTextColor(getResources().getColor(R.color.text_primary, null));
            binding.btnWeekly.setTextColor(getResources().getColor(R.color.text_secondary, null));
        } else {
            binding.btnHourly.setTextColor(getResources().getColor(R.color.text_secondary, null));
            binding.btnWeekly.setTextColor(getResources().getColor(R.color.text_primary, null));
        }

        indicator.post(() -> {
            float targetX = isHourly ? 0 : ((View) indicator.getParent()).getWidth() - indicator.getWidth();
            indicator.animate()
                    .translationX(targetX)
                    .setDuration(250)
                    .start();
        });
    }

    private int getWeatherIconResource(String weatherCondition) {
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

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Helper method để xử lý lỗi network chi tiết
    private String getNetworkErrorMessage(Throwable t) {
        if (t instanceof java.net.SocketTimeoutException) {
            return "Kết nối bị timeout. Vui lòng kiểm tra:\n" +
                   "• Tốc độ mạng của bạn\n" +
                   "• Thử lại sau vài giây";
        } else if (t instanceof java.net.UnknownHostException) {
            return "Không thể kết nối đến server. Vui lòng kiểm tra kết nối internet.";
        } else if (t instanceof java.net.ConnectException) {
            return "Không thể kết nối. Vui lòng kiểm tra:\n" +
                   "• Bạn đã bật internet chưa\n" +
                   "• Thử chuyển đổi giữa WiFi/4G";
        } else if (t instanceof java.io.IOException) {
            return "Lỗi kết nối mạng. Vui lòng thử lại.";
        } else {
            return "Lỗi: " + t.getMessage() + "\nVui lòng thử lại sau.";
        }
    }

    private String capitalizeWords(String str) {
        String[] words = str.split("\\s");
        StringBuilder capitalizedWords = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                capitalizedWords.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return capitalizedWords.toString().trim();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void scheduleWeatherNotifications() {
        // Check if notifications are enabled in settings
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications", true);

        if (notificationsEnabled) {
            // For testing: Use OneTimeWorkRequest with 2 minute delay (PeriodicWork minimum is 15 minutes)
            // This will trigger once after 2 minutes
            androidx.work.OneTimeWorkRequest weatherWorkRequest = new androidx.work.OneTimeWorkRequest.Builder(
                    WeatherNotificationWorker.class)
                    .setInitialDelay(2, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(this).enqueueUniqueWork(
                    "WeatherNotification",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    weatherWorkRequest
            );

            android.util.Log.d("MainActivity", "Weather notification scheduled for 2 minutes from now");
        } else {
            // Cancel all scheduled notifications
            WorkManager.getInstance(this).cancelUniqueWork("WeatherNotification");
        }
    }

    // NEW: Check location permission and get current location
    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            getCurrentLocation();
        } else {
            // Request permission
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // NEW: Get current location using GPS
    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, R.string.getting_location, Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // Check location accuracy
                        float accuracy = location.getAccuracy();

                        // If location is too inaccurate (>1000m), request fresh location
                        if (accuracy > 1000) {
                            Toast.makeText(this, "Location not accurate, requesting fresh location...", Toast.LENGTH_SHORT).show();
                            requestFreshLocation();
                            return;
                        }

                        // Got location - fetch weather by coordinates
                        currentLat = location.getLatitude();
                        currentLon = location.getLongitude();

                        // Debug: Show coordinates to user
                        String coordsMsg = String.format(Locale.getDefault(),
                                "Location: %.4f, %.4f (±%.0fm)",
                                currentLat, currentLon, accuracy);
                        Toast.makeText(this, coordsMsg, Toast.LENGTH_LONG).show();

                        fetchWeatherByCoordinates(currentLat, currentLon);
                    } else {
                        // Last location not available, request fresh location
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(this, R.string.location_not_available, Toast.LENGTH_SHORT).show();
                });
    }

    // Request fresh location when last location is not available
    private void requestFreshLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(this, "Requesting fresh location...", Toast.LENGTH_SHORT).show();

        com.google.android.gms.location.LocationRequest locationRequest =
                new com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 10000)
                        .setMinUpdateIntervalMillis(5000)
                        .setMaxUpdates(1)
                        .build();

        fusedLocationClient.requestLocationUpdates(locationRequest,
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult != null && locationResult.getLastLocation() != null) {
                            android.location.Location location = locationResult.getLastLocation();
                            currentLat = location.getLatitude();
                            currentLon = location.getLongitude();
                            float accuracy = location.getAccuracy();

                            // Debug: Show coordinates to user
                            String coordsMsg = String.format(Locale.getDefault(),
                                    "Fresh location: %.4f, %.4f (±%.0fm)",
                                    currentLat, currentLon, accuracy);
                            Toast.makeText(MainActivity.this, coordsMsg, Toast.LENGTH_LONG).show();

                            fetchWeatherByCoordinates(currentLat, currentLon);
                            // Stop location updates after getting location
                            fusedLocationClient.removeLocationUpdates(this);
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Unable to get your location. Please enable location services.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, getMainLooper());
    }

    // NEW: Fetch weather by GPS coordinates
    private void fetchWeatherByCoordinates(double lat, double lon) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);

        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        String units = temperatureUnit.equals("celsius") ? "metric" : "imperial";

        // Debug log
        String apiUrl = String.format(Locale.getDefault(),
                "Fetching weather for: lat=%.4f, lon=%.4f", lat, lon);
        Toast.makeText(this, apiUrl, Toast.LENGTH_SHORT).show();

        Call<WeatherResponse> weatherCall = apiService.getWeatherByCoordinates(lat, lon, API_KEY, units);
        weatherCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentWeatherData = response.body();
                    currentCityName = currentWeatherData.getName();

                    // Update coordinates
                    currentLat = lat;
                    currentLon = lon;

                    // Debug: Show what API returned
                    String debugMsg = String.format(Locale.getDefault(),
                            "API returned: %s (%.4f, %.4f) - %.0f°",
                            currentCityName,
                            currentWeatherData.getCoord() != null ? currentWeatherData.getCoord().getLat() : 0,
                            currentWeatherData.getCoord() != null ? currentWeatherData.getCoord().getLon() : 0,
                            currentWeatherData.getMain().getTemp());
                    Toast.makeText(MainActivity.this, debugMsg, Toast.LENGTH_LONG).show();

                    // Save city name for notifications
                    sharedPreferences.edit().putString("last_city", currentCityName).apply();

                    updateUI(currentWeatherData);
                    // Use coordinates for hourly forecast to get accurate data
                    fetchHourlyForecastByCoordinates(lat, lon);
                    fetchUVIndex(lat, lon);
                    fetchAirQuality(lat, lon);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    showError(getString(R.string.error_city_not_found));
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showError(getString(R.string.error_network));
            }
        });
    }

    // NEW: Update weather widget
    private void updateWeatherWidget(WeatherResponse weatherData) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, WeatherWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            // Update each widget instance
            WeatherWidget.updateWidget(this, appWidgetManager, appWidgetId, weatherData);
        }
    }


    // NEW: Add current city to favorites
    @SuppressWarnings("unused")
    private void addCurrentCityToFavorites() {
        if (currentWeatherData == null) {
            Toast.makeText(this, "No city data available", Toast.LENGTH_SHORT).show();
            return;
        }

        String country = currentWeatherData.getSys() != null ?
                currentWeatherData.getSys().getCountry() : "Unknown";

        FavoriteCity favoriteCity = new FavoriteCity(
                currentCityName,
                country,
                currentLat,
                currentLon
        );

        favoriteCity.setCurrentTemp(currentWeatherData.getMain().getTemp());
        favoriteCity.setWeatherCondition(
                currentWeatherData.getWeather() != null && !currentWeatherData.getWeather().isEmpty() ?
                        currentWeatherData.getWeather().get(0).getMain() : "Unknown"
        );
        favoriteCity.setWeatherDescription(
                currentWeatherData.getWeather() != null && !currentWeatherData.getWeather().isEmpty() ?
                        currentWeatherData.getWeather().get(0).getDescription() : "Unknown"
        );

        if (favoritesManager.addFavoriteCity(favoriteCity)) {
            Toast.makeText(this, currentCityName + " added to favorites", Toast.LENGTH_SHORT).show();
        } else {
            if (favoritesManager.isFavorite(currentCityName)) {
                Toast.makeText(this, currentCityName + " is already in favorites", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Maximum 10 favorite cities allowed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // NEW: Open Favorite Cities Activity
    @SuppressWarnings("unused")
    private void openFavoriteCitiesActivity() {
        Intent intent = new Intent(this, FavoriteCitiesActivity.class);
        startActivity(intent);
    }

    // NEW: Fetch Weather Alerts
    @SuppressWarnings("unused")
    private void fetchWeatherAlerts(double lat, double lon) {
        if (lat == 0 && lon == 0) return;

        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();
        Call<WeatherAlertsResponse> alertsCall = apiService.getWeatherAlerts(
                lat, lon, API_KEY, "current,minutely,hourly,daily"
        );

        alertsCall.enqueue(new Callback<WeatherAlertsResponse>() {
            @Override
            public void onResponse(Call<WeatherAlertsResponse> call, Response<WeatherAlertsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().hasAlerts()) {
                    currentAlerts = response.body().getAlerts();
                    updateWeatherAlertsCard();
                } else {
                    // No alerts - hide the card
                    hideWeatherAlertsCard();
                }
            }

            @Override
            public void onFailure(Call<WeatherAlertsResponse> call, Throwable t) {
                // Failed to fetch alerts - hide the card
                hideWeatherAlertsCard();
            }
        });
    }

    // NEW: Update Weather Alerts Card
    private void updateWeatherAlertsCard() {
        if (currentAlerts == null || currentAlerts.isEmpty()) {
            hideWeatherAlertsCard();
            return;
        }

        View alertCard = findViewById(R.id.card_weather_alerts);
        if (alertCard == null) return;

        alertCard.setVisibility(View.VISIBLE);

        // Get the most severe alert
        WeatherAlert mainAlert = currentAlerts.get(0);
        for (WeatherAlert alert : currentAlerts) {
            if (getSeverityPriority(alert.getSeverity()) > getSeverityPriority(mainAlert.getSeverity())) {
                mainAlert = alert;
            }
        }

        // Update severity badge
        TextView tvAlertSeverity = alertCard.findViewById(R.id.tvAlertSeverity);
        if (tvAlertSeverity != null) {
            tvAlertSeverity.setText(mainAlert.getSeverity());
            tvAlertSeverity.setBackgroundColor(mainAlert.getSeverityColor());
        }

        // Update alert details
        TextView tvAlertEvent = alertCard.findViewById(R.id.tvAlertEvent);
        TextView tvAlertDescription = alertCard.findViewById(R.id.tvAlertDescription);
        TextView tvAlertTime = alertCard.findViewById(R.id.tvAlertTime);

        if (tvAlertEvent != null) {
            tvAlertEvent.setText(mainAlert.getEvent());
        }

        if (tvAlertDescription != null) {
            String description = mainAlert.getDescription();
            if (description != null && description.length() > 100) {
                description = description.substring(0, 100) + "...";
            }
            tvAlertDescription.setText(description);
        }

        if (tvAlertTime != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            String timeStr = "Valid until " + sdf.format(mainAlert.getEnd() * 1000);
            tvAlertTime.setText(timeStr);
        }

        // Setup click listener for "View More"
        TextView btnViewMore = alertCard.findViewById(R.id.btnViewMoreAlerts);
        if (btnViewMore != null && currentAlerts.size() > 1) {
            btnViewMore.setVisibility(View.VISIBLE);
            btnViewMore.setText(String.format(Locale.getDefault(),
                    "View All Alerts (%d) ›", currentAlerts.size()));
            btnViewMore.setOnClickListener(v -> showAllAlertsDialog());
        } else if (btnViewMore != null) {
            btnViewMore.setVisibility(View.GONE);
        }
    }

    private void hideWeatherAlertsCard() {
        View alertCard = findViewById(R.id.card_weather_alerts);
        if (alertCard != null) {
            alertCard.setVisibility(View.GONE);
        }
    }

    private int getSeverityPriority(String severity) {
        switch (severity) {
            case "EXTREME": return 4;
            case "HIGH": return 3;
            case "MODERATE": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    private void showAllAlertsDialog() {
        if (currentAlerts == null || currentAlerts.isEmpty()) return;

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < currentAlerts.size(); i++) {
            WeatherAlert alert = currentAlerts.get(i);
            message.append((i + 1)).append(". ").append(alert.getEvent()).append("\n");
            message.append(alert.getDescription()).append("\n\n");
        }

        new android.app.AlertDialog.Builder(this)
                .setTitle("Weather Alerts")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

