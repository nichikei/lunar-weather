package com.example.weatherapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkManager;

import com.example.weatherapp.databinding.ActivitySettingsBinding;
import com.example.weatherapp.notification.WeatherNotificationManager;
import com.example.weatherapp.notification.WeatherNotificationWorker;
import com.example.weatherapp.presentation.viewmodel.SettingsViewModel;
import com.example.weatherapp.ui.helpers.PreferenceHelper;
import com.example.weatherapp.utils.LocaleHelper;

/**
 * SettingsActivity with MVVM pattern
 * Manages app settings with reactive updates
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;
    private SharedPreferences sharedPreferences;
    private PreferenceHelper preferenceHelper;
    
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_SPEED_UNIT = "wind_speed_unit";
    private static final String KEY_PRESSURE_UNIT = "pressure_unit";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LANGUAGE = "language";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        viewModel.init(sharedPreferences);
        
        preferenceHelper = new PreferenceHelper(sharedPreferences, this::onPreferenceChanged);

        setupToolbar();
        setupObservers();
        setupListeners();
    }
    
    /**
     * Setup LiveData observers (MVVM pattern)
     */
    private void setupObservers() {
        // Observe settings changes
        viewModel.getSettingsChanged().observe(this, changed -> {
            if (changed) {
                onPreferenceChanged();
            }
        });
        
        // Observe temperature unit changes
        viewModel.getTemperatureUnit().observe(this, unit -> {
            // UI already updated by PreferenceHelper
        });
        
        // Observe other settings if needed
        viewModel.getNotificationsEnabled().observe(this, enabled -> {
            // Handle notification state changes
        });
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void onPreferenceChanged() {
        setResult(RESULT_OK); // Notify MainActivity to refresh
    }

    private void setupListeners() {
        // Smart Alert Switches
        setupSmartAlertSwitches();
        
        // Temperature Unit - Using PreferenceHelper for switch pair
        preferenceHelper.setupSwitchPair(
                binding.switchCelsius,
                binding.switchFahrenheit,
                KEY_TEMPERATURE_UNIT,
                "celsius",
                "fahrenheit"
        );

        // Wind Speed Unit - Using PreferenceHelper for switch pair
        preferenceHelper.setupSwitchPair(
                binding.switchWindMs,
                binding.switchWindKmh,
                KEY_WIND_SPEED_UNIT,
                "ms",
                "kmh"
        );

        // Pressure Unit - Using PreferenceHelper for switch pair
        preferenceHelper.setupSwitchPair(
                binding.switchPressureHpa,
                binding.switchPressureMbar,
                KEY_PRESSURE_UNIT,
                "hpa",
                "mbar"
        );

        // Dark Mode - Simple boolean switch
        preferenceHelper.setupBooleanSwitch(
                binding.switchDarkMode,
                KEY_DARK_MODE,
                true,
                null
        );

        // Notifications - Boolean switch with callback
        preferenceHelper.setupBooleanSwitch(
                binding.switchNotifications,
                KEY_NOTIFICATIONS,
                true,
                this::scheduleNotifications
        );

        // Language Settings - Special switch with recreation
        preferenceHelper.setupLanguageSwitch(
                binding.switchEnglish,
                KEY_LANGUAGE,
                "en",
                this::applyLanguage
        );

        // About button
        binding.btnAbout.setOnClickListener(v -> {
            // TODO: Open About activity or dialog
        });
    }

    private void applyLanguage(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
        
        // Notify MainActivity that language changed
        Intent resultIntent = new Intent();
        resultIntent.putExtra("language_changed", true);
        setResult(RESULT_OK, resultIntent);
        
        recreate(); // Recreate activity to apply language
    }

    private void scheduleNotifications(boolean enabled) {
        WorkManager workManager = WorkManager.getInstance(this);

        if (enabled) {
            // Schedule weather notification check
            androidx.work.OneTimeWorkRequest weatherWorkRequest =
                    new androidx.work.OneTimeWorkRequest.Builder(
                            WeatherNotificationWorker.class)
                            .setInitialDelay(2, java.util.concurrent.TimeUnit.MINUTES)
                            .build();

            workManager.enqueueUniqueWork(
                    "WeatherNotification",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    weatherWorkRequest
            );

            android.widget.Toast.makeText(this, "Weather notifications enabled",
                    android.widget.Toast.LENGTH_SHORT).show();
        } else {
            // Cancel all scheduled notifications
            workManager.cancelUniqueWork("WeatherNotification");

            // Also cancel any shown notifications
            WeatherNotificationManager notificationManager = new WeatherNotificationManager(this);
            notificationManager.cancelAllNotifications();

            android.widget.Toast.makeText(this, "Weather notifications disabled",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup Smart Weather Alert switches
     */
    private void setupSmartAlertSwitches() {
        com.example.weatherapp.utils.WeatherAlertPreferences alertPrefs = 
                new com.example.weatherapp.utils.WeatherAlertPreferences(this);
        com.example.weatherapp.utils.WeatherAlertScheduler scheduler = 
                new com.example.weatherapp.utils.WeatherAlertScheduler();
        
        // Load saved preferences
        binding.switchRainAlerts.setChecked(alertPrefs.areRainAlertsEnabled());
        binding.switchUVAlerts.setChecked(alertPrefs.areUVAlertsEnabled());
        binding.switchAirQualityAlerts.setChecked(alertPrefs.areAirQualityAlertsEnabled());
        binding.switchWeatherChangeAlerts.setChecked(alertPrefs.areWeatherChangeAlertsEnabled());
        
        // Rain alerts
        binding.switchRainAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alertPrefs.setRainAlertsEnabled(isChecked);
            android.widget.Toast.makeText(this, 
                    isChecked ? "Rain alerts enabled" : "Rain alerts disabled", 
                    android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // UV alerts
        binding.switchUVAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alertPrefs.setUVAlertsEnabled(isChecked);
            android.widget.Toast.makeText(this, 
                    isChecked ? "UV alerts enabled" : "UV alerts disabled", 
                    android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // Air quality alerts
        binding.switchAirQualityAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alertPrefs.setAirQualityAlertsEnabled(isChecked);
            android.widget.Toast.makeText(this, 
                    isChecked ? "Air quality alerts enabled" : "Air quality alerts disabled", 
                    android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // Weather change alerts
        binding.switchWeatherChangeAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            alertPrefs.setWeatherChangeAlertsEnabled(isChecked);
            android.widget.Toast.makeText(this, 
                    isChecked ? "Weather change alerts enabled" : "Weather change alerts disabled", 
                    android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // Schedule alerts if any alert is enabled
        if (alertPrefs.areAlertsEnabled()) {
            scheduler.scheduleWeatherAlerts(this, alertPrefs.getAlertFrequency());
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    // Helper methods to get settings from MainActivity
    public static String getTemperatureUnit(SharedPreferences prefs) {
        return prefs.getString(KEY_TEMPERATURE_UNIT, "celsius");
    }

    public static String getWindSpeedUnit(SharedPreferences prefs) {
        return prefs.getString(KEY_WIND_SPEED_UNIT, "ms");
    }

    public static String getPressureUnit(SharedPreferences prefs) {
        return prefs.getString(KEY_PRESSURE_UNIT, "hpa");
    }

    @SuppressWarnings("unused")
    public static String getLanguage(SharedPreferences prefs) {
        return prefs.getString(KEY_LANGUAGE, "en");
    }
}

