package com.example.weatherapp.ui.activities;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.weatherapp.R;
import com.example.weatherapp.data.responses.AirQualityResponse;
import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.databinding.ActivityMainBinding;
import com.example.weatherapp.ui.helpers.FavoritesHelper;
import com.example.weatherapp.ui.helpers.ForecastSummaryGenerator;
import com.example.weatherapp.ui.helpers.ForecastViewManager;
import com.example.weatherapp.ui.helpers.LocationHelper;
import com.example.weatherapp.ui.helpers.NavigationHelper;
import com.example.weatherapp.ui.helpers.NotificationHelper;
import com.example.weatherapp.ui.helpers.UISetupHelper;
import com.example.weatherapp.ui.helpers.UIUpdateHelper;
import com.example.weatherapp.ui.helpers.WeatherDataManager;
import com.example.weatherapp.utils.FavoriteCitiesManager;
import com.example.weatherapp.utils.LocaleHelper;
import com.example.weatherapp.widget.WeatherWidget;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String API_KEY = "4f8cf691daad596ac4e465c909868d0d";
    
    // Helper classes
    private WeatherDataManager weatherDataManager;
    private UIUpdateHelper uiUpdateHelper;
    private LocationHelper locationHelper;
    private ForecastViewManager forecastViewManager;
    private UISetupHelper uiSetupHelper;
    private NavigationHelper navigationHelper;
    private FavoritesHelper favoritesHelper;
    private NotificationHelper notificationHelper;
    
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
        notificationHelper.checkNotificationPermission();

        // Schedule weather notifications
        notificationHelper.scheduleWeatherNotifications();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();

        // Apply glass morphism effects to UI elements
        uiSetupHelper.applyGlassMorphismEffects();

        // Apply blur effect to top glass bar (API 31+)
        uiSetupHelper.applyTopBarBlurEffect();

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



    private void initializeHelpers() {
        weatherDataManager = new WeatherDataManager(this, API_KEY);
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
                    currentCityName = cityName;
                    fetchAllWeatherData(cityName);
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
                favoritesHelper.toggleFavorite(currentCityName, currentWeatherData, currentLat, currentLon);
            }

            @Override
            public void onViewChartsRequested() {
                navigationHelper.openChartsActivity(hourlyForecastData, currentWeatherData, currentUVIndex);
            }

            @Override
            public void onOutfitSuggestionRequested() {
                navigationHelper.openOutfitSuggestionActivity(currentWeatherData);
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
                    updateForecastView();
                }
            }
        });
    }

    private void toggleSearchBar() {
        isSearchVisible = !isSearchVisible;
        uiSetupHelper.toggleSearchBar(isSearchVisible);
    }



    private void fetchAllWeatherData(String cityName) {
        uiSetupHelper.showLoading();

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
                uiSetupHelper.showError(message);
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
                hourlyForecastData = response;
                updateForecastView();
                uiSetupHelper.showWeatherDetails();
            }

            @Override
            public void onForecastError(String message) {
                uiSetupHelper.showWeatherDetails();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchHourlyForecastByCoordinates(double lat, double lon) {
        weatherDataManager.fetchHourlyForecastByCoordinates(lat, lon, temperatureUnit, new WeatherDataManager.ForecastCallback() {
            @Override
            public void onForecastSuccess(HourlyForecastResponse response) {
                hourlyForecastData = response;
                updateForecastView();
                uiSetupHelper.showWeatherDetails();
            }

            @Override
            public void onForecastError(String message) {
                uiSetupHelper.showWeatherDetails();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
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
        favoritesHelper.updateFavoriteIcon(currentCityName);

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
        uiSetupHelper.showLoading();

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
                uiSetupHelper.showError(message);
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHelper != null) {
            locationHelper.cleanup();
        }
        binding = null;
    }
}
