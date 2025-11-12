package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.weatherapp.R;
import com.example.weatherapp.data.repository.implementation.WeatherRepositoryImpl;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.domain.model.AirQualityData;
import com.example.weatherapp.domain.model.ForecastData;
import com.example.weatherapp.domain.model.WeatherData;
import com.example.weatherapp.domain.repository.FavoriteCitiesManager;
import com.example.weatherapp.domain.repository.WeatherRepository;
import com.example.weatherapp.presentation.state.UIState;
import com.example.weatherapp.presentation.viewmodel.MainViewModel;
import com.example.weatherapp.presentation.viewmodel.MainViewModelFactory;
import com.example.weatherapp.ui.dialogs.MoreFeaturesBottomSheet;
import com.example.weatherapp.ui.helpers.ChartAnimationHelper;
import com.example.weatherapp.ui.helpers.FavoritesHelper;
import com.example.weatherapp.ui.helpers.FlagshipEffectsHelper;
import com.example.weatherapp.ui.helpers.ForecastSummaryGenerator;
import com.example.weatherapp.ui.helpers.ForecastViewManager;
import com.example.weatherapp.ui.helpers.LocationHelper;
import com.example.weatherapp.ui.helpers.NavigationHelper;
import com.example.weatherapp.ui.helpers.NotificationHelper;
import com.example.weatherapp.ui.helpers.ParallaxAnimationHelper;
import com.example.weatherapp.ui.helpers.UISetupHelper;
import com.example.weatherapp.ui.helpers.UIUpdateHelper;
import com.example.weatherapp.ui.views.charts.AnimatedProgressRing;
import com.example.weatherapp.ui.views.charts.WeatherLineChart;
import com.example.weatherapp.ui.views.charts.WindSpeedGauge;
import com.example.weatherapp.utils.LocaleHelper;
import com.example.weatherapp.widget.WeatherWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity - Refactored with MVVM Architecture
 * View Layer: Only handles UI, delegates business logic to ViewModel
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private WeatherRepositoryImpl repository; // Keep reference to access cached responses
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    
    // Weather Background Views - Removed for iOS-style glassmorphism
    
    // Helper classes (UI only)
    private UIUpdateHelper uiUpdateHelper;
    private LocationHelper locationHelper;
    private ForecastViewManager forecastViewManager;
    private UISetupHelper uiSetupHelper;
    private NavigationHelper navigationHelper;
    private FavoritesHelper favoritesHelper;
    private NotificationHelper notificationHelper;
    private ChartAnimationHelper chartAnimHelper;
    
    // Managers
    private SharedPreferences sharedPreferences;
    private FavoriteCitiesManager favoritesManager;
    private FusedLocationProviderClient fusedLocationClient;

    // Chart components
    private WeatherLineChart chartTemperature;
    private AnimatedProgressRing ringHumidity;
    private AnimatedProgressRing ringUvIndex;
    private WindSpeedGauge gaugeWindSpeed;

    // UI State only
    private boolean isSearchVisible = false;
    private boolean isHourlyView = true;
    
    // Settings
    private String temperatureUnit = "celsius";
    private String windSpeedUnit = "ms";
    private String pressureUnit = "hpa";

    // Notification permission launcher
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (notificationHelper != null) {
                    if (isGranted) {
                        notificationHelper.onPermissionGranted();
                    } else {
                        notificationHelper.onPermissionDenied();
                    }
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
                            // Load weather by coordinates using ViewModel
                            viewModel.loadWeatherByCoordinates(latitude, longitude);
                        }
                    } else {
                        // Get city name from search
                        String cityName = result.getData().getStringExtra(SearchActivity.EXTRA_CITY_NAME);
                        if (cityName != null && !cityName.isEmpty()) {
                            // Load weather by city using ViewModel
                            viewModel.loadWeatherByCity(cityName);
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
                    
                    // If temperature unit changed, update ViewModel and refetch
                    if (!oldTempUnit.equals(temperatureUnit)) {
                        viewModel.setTemperatureUnit(temperatureUnit);
                        String cityName = viewModel.getCurrentCityName();
                        if (cityName != null && !cityName.isEmpty()) {
                            viewModel.loadWeatherByCity(cityName);
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

        // Flagship: Enable edge-to-edge immersive experience
        EdgeToEdge.enable(this);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Weather background removed - using simple gradient for iOS-style glassmorphism

        sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize managers
        favoritesManager = new FavoriteCitiesManager(this);
        
        // Load settings
        loadSettings();
        
        // Initialize ViewModel (MVVM)
        initializeViewModel();
        
        // Initialize helper classes (UI only)
        initializeHelpers();

        // Setup observers (MVVM - Heart of the pattern)
        setupObservers();

        // Request notification permission for Android 13+
        notificationHelper.checkNotificationPermission();

        // Schedule weather notifications
        notificationHelper.scheduleWeatherNotifications();
        
        // Initialize Smart Weather Alerts
        initializeSmartWeatherAlerts();

        // Flagship: Handle system bars with immersive padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // Only apply top padding for status bar, keep bottom for gesture navigation
            v.setPadding(0, systemBars.top, 0, 0);
            
            // Adjust top bar position to account for status bar
            if (binding.topGlassBar != null) {
                android.view.ViewGroup.MarginLayoutParams params = 
                    (android.view.ViewGroup.MarginLayoutParams) binding.topGlassBar.getLayoutParams();
                params.topMargin = systemBars.top + 12; // 12dp extra spacing
                binding.topGlassBar.setLayoutParams(params);
            }
            
            return insets;
        });

        setupListeners();
        
        // Setup scroll listener for top bar city name visibility
        setupScrollListener();

        // Setup iOS-style glassmorphism with real blur
        setupGlassmorphismBlur();

        // Apply glass morphism effects to UI elements
        uiSetupHelper.applyGlassMorphismEffects();

        // Apply blur effect to top glass bar (API 31+)
        uiSetupHelper.applyTopBarBlurEffect();
        
        // Flagship: Apply premium visual effects
        applyFlagshipEffects();
        
        // Add press animations to interactive elements
        setupPressAnimations();

        // Check if opened from FavoriteCitiesActivity with a specific city
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("CITY_NAME")) {
            String cityName = intent.getStringExtra("CITY_NAME");
            if (cityName != null && !cityName.isEmpty()) {
                viewModel.loadWeatherByCity(cityName);
                return;
            }
        }

        // Default: load weather for Hanoi via ViewModel
        viewModel.loadWeatherByCity("Hanoi");
    }

    /**
     * Initialize ViewModel with Repository (MVVM Pattern)
     */
    private void initializeViewModel() {
        repository = new WeatherRepositoryImpl(this, API_KEY); // Keep reference for charts
        MainViewModelFactory factory = new MainViewModelFactory(repository);
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        viewModel.setTemperatureUnit(temperatureUnit);
    }

    /**
     * Setup LiveData Observers (MVVM - Core Pattern)
     */
    private void setupObservers() {
        // Observe Weather State
        viewModel.getWeatherState().observe(this, state -> {
            if (state instanceof UIState.Loading) {
                showLoading();
            } else if (state instanceof UIState.Success) {
                hideLoading();
                WeatherData data = ((UIState.Success<WeatherData>) state).getData();
                updateWeatherUI(data);
            } else if (state instanceof UIState.Error) {
                hideLoading();
                String error = ((UIState.Error<WeatherData>) state).getMessage();
                showError(error);
            }
        });

        // Observe Forecast State
        viewModel.getForecastState().observe(this, state -> {
            if (state instanceof UIState.Success) {
                ForecastData data = ((UIState.Success<ForecastData>) state).getData();
                updateForecastUI(data);
            } else if (state instanceof UIState.Error) {
                String error = ((UIState.Error<ForecastData>) state).getMessage();
                showError("Forecast: " + error);
            }
        });

        // Observe UV Index State
        viewModel.getUVIndexState().observe(this, state -> {
            if (state instanceof UIState.Success) {
                int uvIndex = ((UIState.Success<Integer>) state).getData();
                updateUVIndexUI(uvIndex);
            }
        });

        // Observe Air Quality State
        viewModel.getAirQualityState().observe(this, state -> {
            if (state instanceof UIState.Success) {
                AirQualityData data = ((UIState.Success<AirQualityData>) state).getData();
                updateAirQualityUI(data);
            }
        });
    }

    private void initializeHelpers() {
        uiUpdateHelper = new UIUpdateHelper(binding, temperatureUnit, windSpeedUnit, pressureUnit);
        locationHelper = new LocationHelper(this, fusedLocationClient);
        forecastViewManager = new ForecastViewManager(this, binding.hourlyForecastContainer, temperatureUnit);
        uiSetupHelper = new UISetupHelper(this, binding);
        navigationHelper = new NavigationHelper(this);
        favoritesHelper = new FavoritesHelper(this, favoritesManager, binding.fabAddToFavorites);
        notificationHelper = new NotificationHelper(this, sharedPreferences, requestPermissionLauncher);
        chartAnimHelper = new ChartAnimationHelper();
        
        // Initialize chart components
        initializeCharts();
    }
    
    /**
     * Initialize interactive chart components
     */
    private void initializeCharts() {
        try {
            // Original charts (if they exist in layout)
            chartTemperature = binding.getRoot().findViewById(R.id.chartTemperature);
            ringHumidity = binding.getRoot().findViewById(R.id.ringHumidity);
            ringUvIndex = binding.getRoot().findViewById(R.id.ringUvIndex);
            gaugeWindSpeed = binding.getRoot().findViewById(R.id.gaugeWindSpeed);
            
            // New charts in main section (use these if originals don't exist)
            if (chartTemperature == null) {
                chartTemperature = binding.getRoot().findViewById(R.id.chartTemperatureMain);
            }
            if (ringHumidity == null) {
                ringHumidity = binding.getRoot().findViewById(R.id.ringHumidityMain);
            }
            if (ringUvIndex == null) {
                ringUvIndex = binding.getRoot().findViewById(R.id.ringUvIndexMain);
            }
            if (gaugeWindSpeed == null) {
                gaugeWindSpeed = binding.getRoot().findViewById(R.id.gaugeWindSpeedMain);
            }
            
            if (chartTemperature != null) {
                chartTemperature.setLineColor(Color.parseColor("#FF6B6B"));
                chartTemperature.setGradientColors(
                    Color.parseColor("#80FF6B6B"), 
                    Color.TRANSPARENT
                );
                
                // Set sample data initially (will be replaced with real data)
                List<WeatherLineChart.ChartDataPoint> sampleData = new ArrayList<>();
                sampleData.add(new WeatherLineChart.ChartDataPoint(22f, "12:00", "°C"));
                sampleData.add(new WeatherLineChart.ChartDataPoint(24f, "15:00", "°C"));
                sampleData.add(new WeatherLineChart.ChartDataPoint(26f, "18:00", "°C"));
                sampleData.add(new WeatherLineChart.ChartDataPoint(25f, "21:00", "°C"));
                sampleData.add(new WeatherLineChart.ChartDataPoint(23f, "00:00", "°C"));
                chartTemperature.setData(sampleData);
            }
            
            if (ringHumidity != null) {
                ringHumidity.setGradientColors(
                    Color.parseColor("#4CAF50"),
                    Color.parseColor("#8BC34A"),
                    Color.parseColor("#FFEB3B")
                );
                ringHumidity.setLabel("Humidity");
                ringHumidity.setUnit("%");
                // Set sample value (will be replaced with real data)
                ringHumidity.setProgress(65f);
            }
            
            if (ringUvIndex != null) {
                ringUvIndex.setGradientColors(
                    Color.parseColor("#FFA726"),
                    Color.parseColor("#FF7043"),
                    Color.parseColor("#F44336")
                );
                ringUvIndex.setLabel("UV Index");
                ringUvIndex.setUnit("");
                // Set sample value (will be replaced with real data)
                ringUvIndex.setProgress(50f); // 5/10 UV index
            }
            
            if (gaugeWindSpeed != null) {
                // Set sample value (will be replaced with real data)
                gaugeWindSpeed.setSpeed(15f); // 15 km/h
            }
            
            // Initialize micro-charts in detail cards
            initializeMicroCharts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize micro-charts in weather detail cards
     */
    private void initializeMicroCharts() {
        try {
            // Initialize UV Trend Chart
            com.example.weatherapp.ui.views.charts.UVTrendChart uvTrendChart = 
                binding.getRoot().findViewById(R.id.uvTrendChart);
            if (uvTrendChart != null) {
                // Set sample data (will be replaced with real API data)
                List<Float> sampleUV = new ArrayList<>();
                List<String> sampleHours = new ArrayList<>();
                for (int i = 6; i <= 18; i += 2) {
                    sampleUV.add((float)(Math.random() * 10));
                    sampleHours.add(i + "h");
                }
                uvTrendChart.setUVData(sampleUV, sampleHours);
            }
            
            // Initialize Humidity Area Chart
            com.example.weatherapp.ui.views.charts.HumidityAreaChart humidityChart = 
                binding.getRoot().findViewById(R.id.humidityAreaChart);
            if (humidityChart != null) {
                List<Float> sampleHumidity = new ArrayList<>();
                List<String> sampleHours = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    sampleHumidity.add(60f + (float)(Math.random() * 30));
                    sampleHours.add((i * 3) + "h");
                }
                humidityChart.setHumidityData(sampleHumidity, sampleHours);
            }
            
            // Initialize Pressure Trend Chart
            com.example.weatherapp.ui.views.charts.PressureTrendChart pressureChart = 
                binding.getRoot().findViewById(R.id.pressureTrendChart);
            if (pressureChart != null) {
                List<Float> samplePressure = new ArrayList<>();
                List<String> sampleHours = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    samplePressure.add(1010f + (float)(Math.random() * 15));
                    sampleHours.add((i * 3) + "h");
                }
                pressureChart.setPressureData(samplePressure, sampleHours);
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error initializing micro-charts", e);
        }
    }
    
    /**
     * Animate weather detail cards with staggered glassmorphic entrance
     */
    private void animateWeatherCards() {
        try {
            // Get all card views
            androidx.cardview.widget.CardView[] cards = {
                findViewById(R.id.cardUvIndex),
                findViewById(R.id.cardSunrise),
                findViewById(R.id.cardWind),
                findViewById(R.id.cardRainfall),
                findViewById(R.id.cardFeelsLike),
                findViewById(R.id.cardHumidity),
                findViewById(R.id.cardVisibility),
                findViewById(R.id.cardPressure)
            };
            
            // Staggered animation with glassmorphic effect
            int delayIncrement = 80; // 80ms between each card
            int duration = 500; // 500ms per animation
            
            for (int i = 0; i < cards.length; i++) {
                final androidx.cardview.widget.CardView card = cards[i];
                if (card != null) {
                    // Initial state: invisible and translated down
                    card.setAlpha(0f);
                    card.setTranslationY(40f);
                    card.setScaleX(0.92f);
                    card.setScaleY(0.92f);
                    
                    // Animate with delay
                    card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(i * delayIncrement)
                        .setDuration(duration)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator(1.5f))
                        .start();
                    
                    // Add touch feedback animation
                    setupCardTouchFeedback(card);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error animating weather cards", e);
        }
    }
    
    /**
     * Setup glassmorphic touch feedback for card
     */
    private void setupCardTouchFeedback(androidx.cardview.widget.CardView card) {
        card.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(android.view.View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        // Scale down with smooth animation
                        v.animate()
                            .scaleX(0.96f)
                            .scaleY(0.96f)
                            .setDuration(150)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        // Scale back to normal
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .setInterpolator(new android.view.animation.DecelerateInterpolator())
                            .start();
                        break;
                }
                return false; // Allow click events to pass through
            }
        });
    }

    /**
     * Initialize Smart Weather Alerts system
     */
    private void initializeSmartWeatherAlerts() {
        com.example.weatherapp.utils.WeatherAlertPreferences alertPrefs = 
                new com.example.weatherapp.utils.WeatherAlertPreferences(this);
        
        // Only schedule if alerts are enabled
        if (alertPrefs.areAlertsEnabled()) {
            int frequency = 2; // Check every 2 minutes
            com.example.weatherapp.utils.WeatherAlertScheduler.scheduleWeatherAlerts(this, frequency);
            Log.d(TAG, "🔔 Smart Weather Alerts initialized with " + frequency + " min frequency");
        } else {
            Log.d(TAG, "Smart Weather Alerts are disabled");
        }
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
        uiSetupHelper.setupListeners(settingsLauncher, searchLauncher, new UISetupHelper.UIActionCallbacks() {
            @Override
            public void onSearchRequested() {
                String cityName = binding.etCityName.getText().toString().trim();
                if (!cityName.isEmpty()) {
                    viewModel.loadWeatherByCity(cityName);
                    toggleSearchBar();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLocationRequested() {
                checkLocationPermissionAndGetLocation();
                toggleSearchBar();
            }

            @Override
            public void onToggleFavorite() {
                String cityName = viewModel.getCurrentCityName();
                double lat = viewModel.getCurrentLatitude();
                double lon = viewModel.getCurrentLongitude();
                WeatherData weatherData = viewModel.getCurrentWeatherData();
                favoritesHelper.toggleFavorite(cityName, weatherData, lat, lon);
            }

            @Override
            public void onViewChartsRequested() {
                // Get UV Index
                int uvIndex = 0;
                UIState<Integer> uvState = viewModel.getUVIndexState().getValue();
                if (uvState instanceof UIState.Success) {
                    uvIndex = ((UIState.Success<Integer>) uvState).getData();
                }
                
                // Get cached responses from repository
                com.example.weatherapp.data.responses.WeatherResponse currentData = repository.getLatestWeatherResponse();
                com.example.weatherapp.data.responses.HourlyForecastResponse hourlyData = repository.getLatestHourlyForecastResponse();
                
                // Debug logging
                Log.d(TAG, "Opening charts - currentData: " + (currentData != null ? "✓" : "✗") + 
                          ", hourlyData: " + (hourlyData != null ? "✓" : "✗") + 
                          ", uvIndex: " + uvIndex);
                
                if (currentData == null || hourlyData == null) {
                    // Data not ready yet - refresh to fetch from network
                    Toast.makeText(MainActivity.this, "Refreshing data for charts...", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Charts data not available, triggering refresh");
                    
                    // Trigger refresh to fetch fresh data from network (will populate cache)
                    viewModel.refreshAllData();
                    
                    // Show hint to user
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        Toast.makeText(MainActivity.this, "Please try again in a moment", Toast.LENGTH_SHORT).show();
                    }, 1500);
                    return;
                }
                
                // Open charts activity
                navigationHelper.openChartsActivity(hourlyData, currentData, uvIndex);
            }

            @Override
            public void onWeatherMapsRequested() {
                // Open Weather Maps activity with current location
                Intent intent = new Intent(MainActivity.this, WeatherMapsActivity.class);
                intent.putExtra("latitude", viewModel.getCurrentLatitude());
                intent.putExtra("longitude", viewModel.getCurrentLongitude());
                intent.putExtra("cityName", viewModel.getCurrentCityName());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onVoiceAssistantRequested() {
                // Get current weather data from ViewModel
                WeatherData currentWeather = viewModel.getCurrentWeatherData();
                if (currentWeather != null) {
                    // Open Voice Weather Assistant with weather data
                    Intent intent = new Intent(MainActivity.this, VoiceWeatherActivity.class);
                    intent.putExtra("weather_data", currentWeather);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.fade_scale_out);
                } else {
                    Toast.makeText(MainActivity.this, "Please wait for weather data to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onOutfitSuggestionRequested() {
                // Get current weather data from ViewModel
                WeatherData currentWeather = viewModel.getCurrentWeatherData();
                if (currentWeather != null) {
                    // Convert WeatherData to WeatherResponse for OutfitSuggestionActivity
                    navigationHelper.openOutfitSuggestionActivity(currentWeather);
                } else {
                    Toast.makeText(MainActivity.this, "Please wait for weather data to load", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onActivitySuggestionsRequested() {
                // Open Activity Suggestions with current weather data
                WeatherData currentWeather = viewModel.getCurrentWeatherData();
                if (currentWeather != null) {
                    Intent intent = new Intent(MainActivity.this, ActivitySuggestionsActivity.class);
                    // Pass weather data as JSON
                    intent.putExtra("weather_data", new com.google.gson.Gson().toJson(currentWeather));
                    
                    // Pass UV Index if available
                    UIState<Integer> uvState = viewModel.getUVIndexState().getValue();
                    if (uvState instanceof UIState.Success) {
                        int uvIndex = ((UIState.Success<Integer>) uvState).getData();
                        intent.putExtra("uv_index", uvIndex);
                    }
                    
                    // Pass AQI if available
                    UIState<AirQualityData> aqiState = viewModel.getAirQualityState().getValue();
                    if (aqiState instanceof UIState.Success) {
                        AirQualityData aqiData = ((UIState.Success<AirQualityData>) aqiState).getData();
                        intent.putExtra("aqi", aqiData.getAqi());
                    }
                    
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    Toast.makeText(MainActivity.this, "Loading weather data...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onMoreFeaturesRequested() {
                // Show bottom sheet with all features
                MoreFeaturesBottomSheet bottomSheet = MoreFeaturesBottomSheet.newInstance();
                bottomSheet.setListener(new MoreFeaturesBottomSheet.MoreFeaturesListener() {
                    @Override
                    public void onWeatherMapsClicked() {
                        bottomSheet.dismiss();
                        // Reuse existing weather maps logic
                        WeatherData currentWeather = viewModel.getCurrentWeatherData();
                        if (currentWeather != null) {
                            Intent intent = new Intent(MainActivity.this, WeatherMapsActivity.class);
                            intent.putExtra("latitude", currentWeather.getLatitude());
                            intent.putExtra("longitude", currentWeather.getLongitude());
                            intent.putExtra("cityName", currentWeather.getCityName());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            Toast.makeText(MainActivity.this, "Loading location...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onVoiceAssistantClicked() {
                        bottomSheet.dismiss();
                        // Get current weather data and open Voice Weather Assistant
                        WeatherData currentWeather = viewModel.getCurrentWeatherData();
                        if (currentWeather != null) {
                            Intent intent = new Intent(MainActivity.this, VoiceWeatherActivity.class);
                            intent.putExtra("weather_data", currentWeather);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_scale_out);
                        } else {
                            Toast.makeText(MainActivity.this, "Please wait for weather data to load", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onWeatherAlarmsClicked() {
                        bottomSheet.dismiss();
                        Intent intent = new Intent(MainActivity.this, com.example.weatherapp.ui.WeatherAlarmActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_scale_out);
                    }

                    @Override
                    public void onOutfitSuggestionsClicked() {
                        bottomSheet.dismiss();
                        // Get current weather data and open outfit suggestions
                        WeatherData currentWeather = viewModel.getCurrentWeatherData();
                        if (currentWeather != null) {
                            navigationHelper.openOutfitSuggestionActivity(currentWeather);
                        } else {
                            Toast.makeText(MainActivity.this, "Please wait for weather data to load", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onActivitySuggestionsClicked() {
                        bottomSheet.dismiss();
                        // Reuse existing activity suggestions logic
                        WeatherData currentWeather = viewModel.getCurrentWeatherData();
                        if (currentWeather != null) {
                            Intent intent = new Intent(MainActivity.this, ActivitySuggestionsActivity.class);
                            intent.putExtra("weather_data", new com.google.gson.Gson().toJson(currentWeather));
                            
                            UIState<Integer> uvState = viewModel.getUVIndexState().getValue();
                            if (uvState instanceof UIState.Success) {
                                intent.putExtra("uv_index", ((UIState.Success<Integer>) uvState).getData());
                            }
                            
                            UIState<AirQualityData> aqiState = viewModel.getAirQualityState().getValue();
                            if (aqiState instanceof UIState.Success) {
                                intent.putExtra("aqi", ((UIState.Success<AirQualityData>) aqiState).getData().getAqi());
                            }
                            
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            Toast.makeText(MainActivity.this, "Loading weather data...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                bottomSheet.show(getSupportFragmentManager(), "MoreFeaturesBottomSheet");
            }

            @Override
            public void onFavoritesListRequested() {
                favoritesHelper.openFavoritesList();
            }

            @Override
            public void onTabChanged(boolean isHourly) {
                if (isHourlyView != isHourly) {
                    isHourlyView = isHourly;
                    uiSetupHelper.animateTabSelection(isHourly);
                    
                    // Update forecast display when tab changes
                    UIState<ForecastData> forecastState = viewModel.getForecastState().getValue();
                    if (forecastState instanceof UIState.Success) {
                        ForecastData data = ((UIState.Success<ForecastData>) forecastState).getData();
                        if (forecastViewManager != null && data != null) {
                            Log.d(TAG, "Tab changed to " + (isHourly ? "Hourly" : "Weekly"));
                            if (isHourly) {
                                forecastViewManager.createHourlyForecastView(data);
                            } else {
                                forecastViewManager.createWeeklyForecastView(data);
                            }
                        }
                    }
                }
            }
        });
    }

    private void toggleSearchBar() {
        isSearchVisible = !isSearchVisible;
        uiSetupHelper.toggleSearchBar(isSearchVisible);
    }
    
    /**
     * Setup iOS-style glassmorphism with real BlurView
     */
    private void setupGlassmorphismBlur() {
        com.example.weatherapp.ui.helpers.BlurViewHelper blurHelper = 
                new com.example.weatherapp.ui.helpers.BlurViewHelper(this, binding.getRoot());

        // Setup hourly forecast card blur
        eightbitlab.com.blurview.BlurView blurViewHourly = findViewById(R.id.blurViewHourly);
        if (blurViewHourly != null) {
            blurHelper.setupCardBlur(blurViewHourly);
            Log.d(TAG, "✅ Hourly card glassmorphism applied");
        }

        // Setup 10-day forecast card blur
        eightbitlab.com.blurview.BlurView blurViewForecast = findViewById(R.id.blurViewForecast);
        if (blurViewForecast != null) {
            blurHelper.setupCardBlur(blurViewForecast);
            Log.d(TAG, "✅ Forecast card glassmorphism applied");
        }

        // Setup bottom navigation blur
        eightbitlab.com.blurview.BlurView blurViewBottomNav = findViewById(R.id.blurViewBottomNav);
        if (blurViewBottomNav != null) {
            blurHelper.setupBottomNavBlur(blurViewBottomNav);
            Log.d(TAG, "✅ Bottom nav glassmorphism applied");
        }

        Log.d(TAG, "🎨 iOS-style glassmorphism setup complete!");
    }

    /**
     * Setup scroll listener with advanced parallax effects and smooth UI transitions
     */
    private void setupScrollListener() {
        if (binding.mainScrollView != null) {
            // Initially hide the city name in top bar
            if (binding.tvTopBarCityName != null) {
                binding.tvTopBarCityName.setAlpha(0f);
                binding.tvTopBarCityName.setScaleX(0.9f);
                binding.tvTopBarCityName.setScaleY(0.9f);
            }
            
            binding.mainScrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                // Calculate scroll progress (0 to 1)
                float scrollThreshold = 300f;
                float scrollProgress = Math.min(1f, scrollY / scrollThreshold);
                
                // === 1. CITY NAME FADE IN (Top Bar) ===
                if (binding.tvTopBarCityName != null) {
                    if (scrollY > 50) {
                        float cityNameAlpha = Math.min(1f, scrollY / 200f);
                        binding.tvTopBarCityName.animate()
                                .alpha(cityNameAlpha)
                                .scaleX(0.9f + (0.1f * cityNameAlpha))
                                .scaleY(0.9f + (0.1f * cityNameAlpha))
                                .setDuration(0) // Instant for smooth scroll sync
                                .start();
                    } else {
                        binding.tvTopBarCityName.animate()
                                .alpha(0f)
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .setDuration(0)
                                .start();
                    }
                }
                
                // === 2. PARALLAX BACKGROUND EFFECT ===
                // Removed for iOS-style static gradient background
                
                // === 3. WEATHER INFO FADE OUT (ONLY BIG TEMP) ===
                // Only fade the large temperature/description, keep min opacity at 0.3
                float weatherFadeThreshold = 200f;
                float weatherAlpha = Math.max(0.3f, 1f - (scrollY / weatherFadeThreshold));
                
                if (binding.tvTemperature != null) {
                    binding.tvTemperature.setAlpha(weatherAlpha);
                    binding.tvTemperature.setTranslationY(-scrollY * 0.2f);
                }
                if (binding.tvWeatherDescription != null) {
                    binding.tvWeatherDescription.setAlpha(weatherAlpha);
                    binding.tvWeatherDescription.setTranslationY(-scrollY * 0.15f);
                }
                if (binding.tvTempRange != null) {
                    binding.tvTempRange.setAlpha(weatherAlpha);
                    binding.tvTempRange.setTranslationY(-scrollY * 0.1f);
                }
                
                // === 4. TOP BAR ELEVATION & BACKGROUND ===
                if (binding.topGlassBar != null) {
                    // Increase elevation when scrolled
                    float elevation = 8f + (scrollProgress * 8f); // 8dp to 16dp
                    binding.topGlassBar.setElevation(elevation);
                    
                    // Slightly increase opacity of glass background
                    float alpha = 0.85f + (scrollProgress * 0.15f); // More solid when scrolled
                    binding.topGlassBar.setAlpha(alpha);
                }
                
                // === 5. KEEP FORECAST & CARDS FULLY VISIBLE ===
                // Don't fade out forecast and detail cards - keep them readable
                if (binding.hourlyForecastContainer != null) {
                    binding.hourlyForecastContainer.setAlpha(1f);
                }
                
                // Keep weather detail section fully visible too
                if (binding.weatherDetailsSection != null) {
                    binding.weatherDetailsSection.setAlpha(1f);
                }
                
                // Optional: Add subtle float up effect for cards when scrolling
                if (scrollY > 100 && binding.weatherDetailsSection != null) {
                    float cardTranslation = -(scrollY - 100) * 0.05f; // Very subtle
                    binding.weatherDetailsSection.setTranslationY(Math.max(cardTranslation, -20f));
                } else if (binding.weatherDetailsSection != null) {
                    binding.weatherDetailsSection.setTranslationY(0f);
                }
            });
        }
    }

    /**
     * Update main UI immediately when a city is selected from SearchActivity
     * and play ultra-smooth parallax animation with professional cascading effects.
     */
    // ============ UI Update Methods (MVVM) ============

    /**
     * Update UI with WeatherData from ViewModel
     */
    private void updateWeatherUI(WeatherData data) {
        try {
            // Animate city name change
            binding.tvTopBarCityName.animate()
                    .alpha(0f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        binding.tvTopBarCityName.setText(data.getCityName());
                        binding.tvTopBarCityName.animate().alpha(1f).setDuration(150).start();
                    }).start();

            binding.tvCityName.setText(data.getCityName() + ", " + data.getCountryCode());
            
            String tempSymbol = data.getTemperatureUnit().equals("celsius") ? "°C" : "°F";
            binding.tvTemperature.setText(String.format("%.0f%s", data.getTemperature(), tempSymbol));
            binding.tvWeatherDescription.setText(capitalize(data.getWeatherDescription()));
            binding.tvTempRange.setText(String.format("H: %.0f° L: %.0f°", 
                data.getMaxTemperature(), data.getMinTemperature()));
            
            // Update favorite icon
            favoritesHelper.updateFavoriteIcon(data.getCityName());
            
            // Update charts with current weather data
            updateChartsWithWeatherData(data);
            
            // Update animated weather background
            updateWeatherBackground(data);
            
            // Animate glassmorphic weather cards
            animateWeatherCards();
            
            // Animate entrance
            animateWeatherEntrance();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update UI with ForecastData from ViewModel
     */
    private void updateForecastUI(ForecastData data) {
        uiSetupHelper.showWeatherDetails();
        
        // Update temperature line chart with forecast data
        if (chartTemperature != null && data != null) {
            updateTemperatureChart(data);
        }
        
        // Update micro-charts in detail cards
        if (data != null) {
            updateMicroChartsWithForecast(data);
        }
        
        // Update hourly/weekly forecast cards
        if (forecastViewManager != null && data != null) {
            Log.d(TAG, "Updating forecast cards with " + data.getHourlyForecasts().size() + " hourly items");
            if (isHourlyView) {
                forecastViewManager.createHourlyForecastView(data);
            } else {
                forecastViewManager.createWeeklyForecastView(data);
            }
        }
    }

    /**
     * Update UV Index UI
     */
    private void updateUVIndexUI(int uvIndex) {
        uiUpdateHelper.updateUVIndexCard(uvIndex);
        
        // Update UV index ring
        if (ringUvIndex != null) {
            ringUvIndex.setProgress(uvIndex * 10f); // Scale 0-10 to 0-100
        }
        
        // Update UV trend chart
        updateUVTrendChart(uvIndex);
    }

    /**
     * Update Air Quality UI  
     */
    private void updateAirQualityUI(AirQualityData data) {
        // Air quality data received and ready for display
    }
    
    /**
     * Update charts with current weather data
     */
    private void updateChartsWithWeatherData(WeatherData data) {
        try {
            android.util.Log.d("MainActivity", "Updating charts with weather data:");
            android.util.Log.d("MainActivity", "Humidity: " + data.getHumidity());
            android.util.Log.d("MainActivity", "Wind Speed: " + data.getWindSpeed());
            
            // Update humidity ring
            if (ringHumidity != null) {
                float humidity = (float) data.getHumidity();
                android.util.Log.d("MainActivity", "Setting humidity ring to: " + humidity);
                ringHumidity.setProgress(humidity);
                ringHumidity.setShowParticles(true);
            }
            
            // Update wind gauge
            if (gaugeWindSpeed != null) {
                float windSpeed = (float) data.getWindSpeed();
                android.util.Log.d("MainActivity", "Setting wind gauge to: " + windSpeed);
                gaugeWindSpeed.setSpeed(windSpeed);
            }
            
            // Animate charts entrance with stagger
            if (chartAnimHelper != null) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (chartTemperature != null) {
                        chartAnimHelper.slideInChartFromBottom(chartTemperature, 600);
                    }
                }, 300);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (ringHumidity != null && ringUvIndex != null) {
                        chartAnimHelper.animateProgressRings(200, ringHumidity, ringUvIndex);
                    }
                }, 600);
                
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (gaugeWindSpeed != null) {
                        chartAnimHelper.bounceInChart(gaugeWindSpeed, 800);
                    }
                }, 1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Update temperature line chart with forecast data
     */
    private void updateTemperatureChart(ForecastData data) {
        try {
            List<WeatherLineChart.ChartDataPoint> chartData = new ArrayList<>();
            
            // Get hourly forecasts
            List<ForecastData.HourlyForecast> hourlyForecasts = data.getHourlyForecasts();
            android.util.Log.d("MainActivity", "Updating temperature chart, forecasts: " + 
                (hourlyForecasts != null ? hourlyForecasts.size() : "null"));
            
            if (hourlyForecasts != null && !hourlyForecasts.isEmpty()) {
                // Get first 8 hourly forecasts
                int count = Math.min(8, hourlyForecasts.size());
                android.util.Log.d("MainActivity", "Processing " + count + " forecasts");
                
                for (int i = 0; i < count; i++) {
                    ForecastData.HourlyForecast item = hourlyForecasts.get(i);
                    
                    // Convert timestamp to readable format
                    long timestamp = item.getTimestamp();
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                    String timeLabel = sdf.format(new java.util.Date(timestamp * 1000));
                    
                    String tempUnit = temperatureUnit.equals("celsius") ? "°C" : "°F";
                    float temp = (float) item.getTemperature();
                    
                    android.util.Log.d("MainActivity", "Adding point: " + timeLabel + " = " + temp + tempUnit);
                    chartData.add(new WeatherLineChart.ChartDataPoint(temp, timeLabel, tempUnit));
                }
            }
            
            if (!chartData.isEmpty() && chartTemperature != null) {
                android.util.Log.d("MainActivity", "Setting " + chartData.size() + " data points to temperature chart");
                chartTemperature.setData(chartData);
            } else {
                android.util.Log.e("MainActivity", "Chart data is empty or chart is null!");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error updating temperature chart", e);
            e.printStackTrace();
        }
    }
    
    /**
     * Update micro-charts in detail cards with real forecast data
     */
    private void updateMicroChartsWithForecast(ForecastData data) {
        try {
            List<ForecastData.HourlyForecast> hourlyForecasts = data.getHourlyForecasts();
            if (hourlyForecasts == null || hourlyForecasts.isEmpty()) {
                return;
            }
            
            // Prepare data for charts (use first 8 hours)
            int count = Math.min(8, hourlyForecasts.size());
            List<Float> humidityValues = new ArrayList<>();
            List<Float> pressureValues = new ArrayList<>();
            List<String> hourLabels = new ArrayList<>();
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            
            for (int i = 0; i < count; i++) {
                ForecastData.HourlyForecast item = hourlyForecasts.get(i);
                humidityValues.add((float) item.getHumidity());
                pressureValues.add((float) item.getPressure());
                
                String timeLabel = sdf.format(new java.util.Date(item.getTimestamp() * 1000));
                hourLabels.add(timeLabel);
            }
            
            // Update Humidity Chart
            com.example.weatherapp.ui.views.charts.HumidityAreaChart humidityChart = 
                binding.getRoot().findViewById(R.id.humidityAreaChart);
            if (humidityChart != null) {
                humidityChart.setHumidityData(humidityValues, hourLabels);
                android.util.Log.d("MainActivity", "Updated humidity chart with " + humidityValues.size() + " points");
            }
            
            // Update Pressure Chart
            com.example.weatherapp.ui.views.charts.PressureTrendChart pressureChart = 
                binding.getRoot().findViewById(R.id.pressureTrendChart);
            if (pressureChart != null) {
                pressureChart.setPressureData(pressureValues, hourLabels);
                android.util.Log.d("MainActivity", "Updated pressure chart with " + pressureValues.size() + " points");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error updating micro-charts", e);
        }
    }
    
    /**
     * Update UV trend chart with hourly UV data
     * Note: This requires UV forecast data from API (not available in basic OpenWeather)
     * Using simulated data for demo purposes
     */
    private void updateUVTrendChart(int currentUV) {
        try {
            com.example.weatherapp.ui.views.charts.UVTrendChart uvChart = 
                binding.getRoot().findViewById(R.id.uvTrendChart);
            if (uvChart != null) {
                // Simulate UV trend based on current UV (peaks at noon)
                List<Float> uvValues = new ArrayList<>();
                List<String> hourLabels = new ArrayList<>();
                
                // Generate UV curve (6AM to 6PM)
                for (int hour = 6; hour <= 18; hour += 2) {
                    // UV peaks at noon (12), simulate bell curve
                    float uvValue = currentUV * (1 - Math.abs(hour - 12) / 6f) * 0.9f;
                    uvValues.add(Math.max(0, uvValue));
                    hourLabels.add(hour + "h");
                }
                
                uvChart.setUVData(uvValues, hourLabels);
                android.util.Log.d("MainActivity", "Updated UV trend chart");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Error updating UV trend chart", e);
        }
    }

    /**
     * Show loading state
     */
    private void showLoading() {
        if (binding.weatherDetailsSection != null) {
            binding.weatherDetailsSection.animate()
                    .alpha(0.5f)
                    .setDuration(200)
                    .start();
        }
        uiSetupHelper.showLoading();
    }

    /**
     * Hide loading and show content
     */
    private void hideLoading() {
        if (binding.weatherDetailsSection != null) {
            binding.weatherDetailsSection.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();
        }
        uiSetupHelper.showWeatherDetails();
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        uiSetupHelper.showError(message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }



    /**
     * Animate weather details entrance
     */
    private void animateWeatherEntrance() {
        if (binding.weatherDetailsSection != null) {
            binding.weatherDetailsSection.setAlpha(0f);
            binding.weatherDetailsSection.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .withEndAction(this::animateCardsEntrance)
                    .start();
        } else {
            animateCardsEntrance();
        }
    }
    
    /**
     * Animate cards entrance with staggered bounce effect
     */
    private void animateCardsEntrance() {
        // Find all card views
        android.view.View[] cards = new android.view.View[] {
            binding.getRoot().findViewById(R.id.card_air_quality),
            binding.getRoot().findViewById(R.id.cardUvIndex),
            binding.getRoot().findViewById(R.id.cardSunrise),
            binding.getRoot().findViewById(R.id.cardWind),
            binding.getRoot().findViewById(R.id.cardRainfall),
            binding.getRoot().findViewById(R.id.cardFeelsLike),
            binding.getRoot().findViewById(R.id.cardHumidity),
            binding.getRoot().findViewById(R.id.cardVisibility),
            binding.getRoot().findViewById(R.id.cardPressure)
        };
        
        int delay = 0;
        for (android.view.View card : cards) {
            if (card != null) {
                // Start from invisible and scaled down
                card.setAlpha(0f);
                card.setScaleX(0.9f);
                card.setScaleY(0.9f);
                card.setTranslationY(30f);
                
                // Animate in with bounce
                card.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .translationY(0f)
                        .setStartDelay(delay)
                        .setDuration(500)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(0.8f))
                        .start();
                
                delay += 50; // Stagger by 50ms each
            }
        }
    }
    
    /**
     * Setup press animations for all interactive buttons
     */
    private void setupPressAnimations() {
        // Top bar buttons
        UISetupHelper.addPressAnimation(binding.btnSearchIcon);
        UISetupHelper.addPressAnimation(binding.btnSettingsIcon);
        if (binding.btnFavoritesIcon != null) {
            UISetupHelper.addPressAnimation(binding.btnFavoritesIcon);
        }
        
        // Action buttons
        if (binding.btnMoreFeatures != null) {
            UISetupHelper.addPressAnimation(binding.btnMoreFeatures);
        }
        if (binding.fabAddToFavorites != null) {
            UISetupHelper.addPressAnimation(binding.fabAddToFavorites);
        }
        
        // Tab buttons
        if (binding.btnHourly != null) {
            UISetupHelper.addPressAnimation(binding.btnHourly);
        }
        if (binding.btnWeekly != null) {
            UISetupHelper.addPressAnimation(binding.btnWeekly);
        }
    }
    
    /**
     * Apply flagship-level visual effects
     */
    private void applyFlagshipEffects() {
        // Enhanced top bar with depth
        if (binding.topGlassBar != null) {
            FlagshipEffectsHelper.addDepthShadow(binding.topGlassBar);
        }
        
        // Premium FAB with glow
        if (binding.fabAddToFavorites != null) {
            FlagshipEffectsHelper.addGlow(
                binding.fabAddToFavorites, 
                android.graphics.Color.parseColor("#509E4CFF"), 
                16f
            );
        }
        
        // Enhanced cards
        android.view.View[] premiumCards = new android.view.View[] {
            binding.getRoot().findViewById(R.id.card_air_quality),
            binding.getRoot().findViewById(R.id.btnMoreFeatures)
        };
        
        for (android.view.View card : premiumCards) {
            if (card != null) {
                FlagshipEffectsHelper.enhanceCard(card);
            }
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
                viewModel.loadWeatherByCoordinates(latitude, longitude);
            }

            @Override
            public void onLocationError(String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Update weather background - Simplified for iOS-style glassmorphism
     * Removed fancy effects for clean, minimal iOS aesthetic
     */
    private void updateWeatherBackground(WeatherData data) {
        // iOS Weather uses simple gradient backgrounds - no fancy effects needed
        Log.d(TAG, "iOS-style background: Simple gradient with glassmorphism panels");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.cleanup();
        }
        if (chartAnimHelper != null) {
            chartAnimHelper.cleanup();
        }
        // Clear chart references
        chartTemperature = null;
        ringHumidity = null;
        ringUvIndex = null;
        gaugeWindSpeed = null;
        binding = null;
    }
}
