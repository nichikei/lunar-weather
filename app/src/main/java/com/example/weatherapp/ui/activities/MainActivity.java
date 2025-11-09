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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingWorkPolicy;
import androidx.work.WorkManager;

import com.example.weatherapp.R;
import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.notification.WeatherNotificationWorker;
import com.example.weatherapp.ui.helpers.ForecastSummaryGenerator;
import com.example.weatherapp.ui.helpers.ForecastViewManager;
import com.example.weatherapp.ui.helpers.LocationHelper;
import com.example.weatherapp.ui.helpers.UIUpdateHelper;
import com.example.weatherapp.ui.helpers.WeatherDataManager;
import com.example.weatherapp.utils.FavoriteCitiesManager;
import com.example.weatherapp.utils.LocaleHelper;
import com.example.weatherapp.widget.WeatherWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    
    // Helper classes
    private WeatherDataManager weatherDataManager;
    private UIUpdateHelper uiUpdateHelper;
    private LocationHelper locationHelper;
    private ForecastViewManager forecastViewManager;
    
    // Data
    private WeatherResponse currentWeatherData;
    private HourlyForecastResponse hourlyForecastData;
    private String currentCityName = "Hanoi";
    private double currentLat = 0;
    private double currentLon = 0;
    private int currentUVIndex = 0;
    private AirQualityResponse.AirQualityData currentAQIData;
    
    // Managers
    private SharedPreferences sharedPreferences;
    private FavoriteCitiesManager favoritesManager;
    private FusedLocationProviderClient fusedLocationClient;

    // UI State
    private boolean isSearchVisible = false;
    private boolean isHourlyView = true;
    
    // Unit settings
    private String temperatureUnit = "celsius";
    private String windSpeedUnit = "ms";
    private String pressureUnit = "hpa";

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

                    // Re-initialize helpers with new settings
                    initializeHelpers();
                    
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

        // Initialize managers
        favoritesManager = new FavoriteCitiesManager(this);
        
        // Load settings
        loadSettings();
        
        // Initialize helper classes
        initializeHelpers();

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

        // Check if opened from FavoriteCitiesActivity with a specific city
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CITY_NAME")) {
            String cityName = intent.getStringExtra("CITY_NAME");
            if (cityName != null && !cityName.isEmpty()) {
                currentCityName = cityName;
                fetchAllWeatherData(cityName);
                return;
            }
        }

        // Default: fetch weather for Hanoi
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

    private void initializeHelpers() {
        weatherDataManager = new WeatherDataManager(this, API_KEY);
        uiUpdateHelper = new UIUpdateHelper(binding, temperatureUnit, windSpeedUnit, pressureUnit);
        locationHelper = new LocationHelper(this, fusedLocationClient);
        forecastViewManager = new ForecastViewManager(this, binding.hourlyForecastContainer, temperatureUnit);
    }

    private void loadSettings() {
        temperatureUnit = SettingsActivity.getTemperatureUnit(sharedPreferences);
        windSpeedUnit = SettingsActivity.getWindSpeedUnit(sharedPreferences);
        pressureUnit = SettingsActivity.getPressureUnit(sharedPreferences);
    }

    private void applyLanguagePreference() {
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", "vi");
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

        // Top Bar Favorites Icon - NEW
        if (binding.btnFavoritesIcon != null) {
            binding.btnFavoritesIcon.setOnClickListener(v -> {
                openFavoriteCitiesActivity();
            });
        }

        // FAB Add to Favorites Button - NEW
        if (binding.fabAddToFavorites != null) {
            binding.fabAddToFavorites.setOnClickListener(v -> {
                toggleFavoriteCity();
            });
        }

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

        weatherDataManager.fetchWeatherByCity(cityName, temperatureUnit, new WeatherDataManager.WeatherDataCallback() {
            @Override
            public void onWeatherSuccess(WeatherResponse response) {
                currentWeatherData = response;
                currentCityName = response.getName();

                // Get coordinates
                if (currentWeatherData.getCoord() != null) {
                    currentLat = currentWeatherData.getCoord().getLat();
                    currentLon = currentWeatherData.getCoord().getLon();
                }

                updateUI(currentWeatherData);

                // Fetch additional data
                fetchHourlyForecast(cityName);
                fetchUVIndex(currentLat, currentLon);
                fetchAirQuality(currentLat, currentLon);
            }

            @Override
            public void onWeatherError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void fetchUVIndex(double lat, double lon) {
        weatherDataManager.fetchUVIndex(lat, lon, new WeatherDataManager.UVIndexCallback() {
            @Override
            public void onUVIndexSuccess(int uvIndex) {
                currentUVIndex = uvIndex;
                uiUpdateHelper.updateUVIndexCard(currentUVIndex);
            }

            @Override
            public void onUVIndexError() {
                currentUVIndex = 0;
                uiUpdateHelper.updateUVIndexCard(currentUVIndex);
            }
        });
    }

    private void fetchAirQuality(double lat, double lon) {
        weatherDataManager.fetchAirQuality(lat, lon, new WeatherDataManager.AirQualityCallback() {
            @Override
            public void onAirQualitySuccess(AirQualityResponse.AirQualityData data) {
                currentAQIData = data;
                uiUpdateHelper.updateAirQualityCard(currentAQIData);
            }

            @Override
            public void onAirQualityError() {
                // Keep default AQI if API fails
            }
        });
    }

    private void fetchHourlyForecast(String cityName) {
        weatherDataManager.fetchHourlyForecast(cityName, temperatureUnit, new WeatherDataManager.ForecastCallback() {
            @Override
            public void onForecastSuccess(HourlyForecastResponse response) {
                binding.progressBar.setVisibility(View.GONE);
                hourlyForecastData = response;
                updateForecastView();
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
            }

            @Override
            public void onForecastError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHourlyForecastByCoordinates(double lat, double lon) {
        weatherDataManager.fetchHourlyForecastByCoordinates(lat, lon, temperatureUnit, new WeatherDataManager.ForecastCallback() {
            @Override
            public void onForecastSuccess(HourlyForecastResponse response) {
                binding.progressBar.setVisibility(View.GONE);
                hourlyForecastData = response;
                updateForecastView();
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
            }

            @Override
            public void onForecastError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                binding.weatherDetailsSection.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

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

        if (hourlyForecastData != null) {
            if (isHourlyView) {
                forecastViewManager.createHourlyForecastView(hourlyForecastData);
            } else {
                forecastViewManager.createWeeklyForecastView(hourlyForecastData);
            }
        } else {
            showForecastUnavailable();
        }
    }

    private void showForecastUnavailable() {
        binding.hourlyForecastContainer.removeAllViews();
        Toast.makeText(this, "Forecast data is currently unavailable", Toast.LENGTH_SHORT).show();
    }


    private void updateUI(WeatherResponse weatherData) {
        // Update main weather info
        uiUpdateHelper.updateMainWeatherInfo(weatherData);

        // Update favorite icon
        updateFavoriteIcon();

        // Update forecast summary
        TextView tvForecastSummary = binding.getRoot().findViewById(R.id.tvForecastSummary);
        if (tvForecastSummary != null && weatherData.getWeather() != null && !weatherData.getWeather().isEmpty()) {
            String weatherCondition = weatherData.getWeather().get(0).getMain().toLowerCase();
            String summary = ForecastSummaryGenerator.generateSummary(weatherCondition, hourlyForecastData);
            tvForecastSummary.setText(summary);
        }

        // Update weather detail cards
        uiUpdateHelper.updateWeatherDetailsCards(weatherData);

        // Update widget
        updateWeatherWidget(weatherData);
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



    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private void checkLocationPermissionAndGetLocation() {
        if (locationHelper.hasLocationPermission()) {
            getCurrentLocation();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void getCurrentLocation() {
        locationHelper.getCurrentLocation(new LocationHelper.OnLocationResultListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                currentLat = latitude;
                currentLon = longitude;
                fetchWeatherByCoordinates(latitude, longitude);
            }

            @Override
            public void onLocationError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchWeatherByCoordinates(double lat, double lon) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.weatherDetailsSection.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);

        weatherDataManager.fetchWeatherByCoordinates(lat, lon, temperatureUnit, new WeatherDataManager.WeatherDataCallback() {
            @Override
            public void onWeatherSuccess(WeatherResponse response) {
                currentWeatherData = response;
                currentCityName = response.getName();
                currentLat = lat;
                currentLon = lon;

                // Save city name
                sharedPreferences.edit().putString("last_city", currentCityName).apply();

                updateUI(currentWeatherData);
                fetchHourlyForecastByCoordinates(lat, lon);
                fetchUVIndex(lat, lon);
                fetchAirQuality(lat, lon);
            }

            @Override
            public void onWeatherError(String message) {
                binding.progressBar.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void updateWeatherWidget(WeatherResponse weatherData) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ComponentName thisWidget = new ComponentName(this, WeatherWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            // Update each widget instance
            WeatherWidget.updateWidget(this, appWidgetManager, appWidgetId, weatherData);
        }
    }


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

    @SuppressWarnings("unused")
    private void openFavoriteCitiesActivity() {
        Intent intent = new Intent(this, FavoriteCitiesActivity.class);
        startActivity(intent);
    }

    // Weather Alerts feature - currently disabled (incomplete implementation)
    // TODO: Implement Weather Alerts feature in future version

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.cleanup();
        }
        binding = null;
    }

    /**
     * Toggle favorite status of current city
     */
    private void toggleFavoriteCity() {
        if (currentCityName == null || currentCityName.isEmpty()) {
            Toast.makeText(this, "No city loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFavorite = favoritesManager.isFavorite(currentCityName);

        if (isFavorite) {
            // Remove from favorites
            boolean removed = favoritesManager.removeFavoriteCity(currentCityName);
            if (removed) {
                binding.fabAddToFavorites.setImageResource(R.drawable.ic_heart_line);
                Toast.makeText(this, currentCityName + " removed from favorites", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add to favorites
            String country = "";
            if (currentWeatherData != null && currentWeatherData.getSys() != null) {
                country = currentWeatherData.getSys().getCountry();
            }

            FavoriteCity favoriteCity = new FavoriteCity(
                    currentCityName,
                    country,
                    currentLat,
                    currentLon
            );

            // Set weather information if available
            if (currentWeatherData != null) {
                favoriteCity.setCurrentTemp(currentWeatherData.getMain().getTemp());
                if (!currentWeatherData.getWeather().isEmpty()) {
                    favoriteCity.setWeatherCondition(currentWeatherData.getWeather().get(0).getMain());
                    favoriteCity.setWeatherDescription(currentWeatherData.getWeather().get(0).getDescription());
                }
            }

            boolean added = favoritesManager.addFavoriteCity(favoriteCity);
            if (added) {
                binding.fabAddToFavorites.setImageResource(R.drawable.ic_heart_filled);
                Toast.makeText(this, currentCityName + " added to favorites", Toast.LENGTH_SHORT).show();

                // Show dialog to open favorites
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Added to Favorites")
                        .setMessage(currentCityName + " has been added to your favorite cities. Would you like to view your favorites list?")
                        .setPositiveButton("View Favorites", (dialog, which) -> {
                            openFavoriteCitiesActivity();
                        })
                        .setNegativeButton("Continue", null)
                        .show();
            } else {
                // Check if it's because of max limit
                if (favoritesManager.getFavoriteCitiesCount() >= 10) {
                    Toast.makeText(this, "Maximum 10 favorite cities allowed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "City already in favorites", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Update FAB icon based on favorite status
     */
    private void updateFavoriteIcon() {
        if (binding.fabAddToFavorites != null && currentCityName != null) {
            boolean isFavorite = favoritesManager.isFavorite(currentCityName);
            binding.fabAddToFavorites.setImageResource(
                    isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_line
            );
        }
    }
}
