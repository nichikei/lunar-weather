package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.weatherapp.utils.LocaleHelper;
import com.example.weatherapp.widget.WeatherWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * MainActivity - Refactored with MVVM Architecture
 * View Layer: Only handles UI, delegates business logic to ViewModel
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    
    // Helper classes (UI only)
    private UIUpdateHelper uiUpdateHelper;
    private LocationHelper locationHelper;
    private ForecastViewManager forecastViewManager;
    private UISetupHelper uiSetupHelper;
    private NavigationHelper navigationHelper;
    private FavoritesHelper favoritesHelper;
    private NotificationHelper notificationHelper;
    
    // Managers
    private SharedPreferences sharedPreferences;
    private FavoriteCitiesManager favoritesManager;
    private FusedLocationProviderClient fusedLocationClient;

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
        WeatherRepository repository = new WeatherRepositoryImpl(this, API_KEY);
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
                // Create temporary WeatherResponse for favorites (legacy code)
                favoritesHelper.toggleFavorite(cityName, null, lat, lon);
            }

            @Override
            public void onViewChartsRequested() {
                // Get data from ViewModel states
                navigationHelper.openChartsActivity(null, null, 0);
            }

            @Override
            public void onOutfitSuggestionRequested() {
                navigationHelper.openOutfitSuggestionActivity(null);
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
                }
            }
        });
    }

    private void toggleSearchBar() {
        isSearchVisible = !isSearchVisible;
        uiSetupHelper.toggleSearchBar(isSearchVisible);
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
                if (binding.ivBackgroundHeader != null) {
                    // Background moves slower than scroll (parallax)
                    float parallaxOffset = scrollY * 0.5f;
                    binding.ivBackgroundHeader.setTranslationY(parallaxOffset);
                    
                    // Slight zoom out effect
                    float scale = 1f + (scrollProgress * 0.05f);
                    binding.ivBackgroundHeader.setScaleX(scale);
                    binding.ivBackgroundHeader.setScaleY(scale);
                }
                
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
    }

    /**
     * Update UV Index UI
     */
    private void updateUVIndexUI(int uvIndex) {
        uiUpdateHelper.updateUVIndexCard(uvIndex);
    }

    /**
     * Update Air Quality UI  
     */
    private void updateAirQualityUI(AirQualityData data) {
        // Air quality data received and ready for display
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
        if (binding.btnViewCharts != null) {
            UISetupHelper.addPressAnimation(binding.btnViewCharts);
        }
        if (binding.btnOutfitSuggestion != null) {
            UISetupHelper.addPressAnimation(binding.btnOutfitSuggestion);
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
            binding.getRoot().findViewById(R.id.btnViewCharts),
            binding.getRoot().findViewById(R.id.btnOutfitSuggestion)
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.cleanup();
        }
        binding = null;
    }
}
