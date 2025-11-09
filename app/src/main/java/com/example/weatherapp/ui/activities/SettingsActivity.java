package com.example.weatherapp.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import com.example.weatherapp.databinding.ActivitySettingsBinding;
import com.example.weatherapp.notification.WeatherNotificationManager;
import com.example.weatherapp.notification.WeatherNotificationWorker;
import com.example.weatherapp.ui.helpers.PreferenceHelper;
import com.example.weatherapp.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
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

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferenceHelper = new PreferenceHelper(sharedPreferences, this::onPreferenceChanged);

        setupToolbar();
        setupListeners();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void onPreferenceChanged() {
        setResult(RESULT_OK); // Notify MainActivity to refresh
    }

    private void setupListeners() {
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
            // For testing: Use OneTimeWorkRequest with 2 minute delay
            // Note: PeriodicWorkRequest minimum interval is 15 minutes on Android
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

            android.widget.Toast.makeText(this, "Weather notification will show in 2 minutes (test mode)",
                    android.widget.Toast.LENGTH_LONG).show();
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

