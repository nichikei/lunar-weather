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
import com.example.weatherapp.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_TEMPERATURE_UNIT = "temperature_unit";
    private static final String KEY_WIND_SPEED_UNIT = "wind_speed_unit";
    private static final String KEY_PRESSURE_UNIT = "pressure_unit";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications";
    private static final String KEY_LANGUAGE = "language";

    // Flags to prevent infinite loops
    private boolean isUpdatingTemperature = false;
    private boolean isUpdatingWind = false;
    private boolean isUpdatingPressure = false;
    private boolean isUpdatingLanguage = false;

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

        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        // Load Temperature Unit
        String tempUnit = sharedPreferences.getString(KEY_TEMPERATURE_UNIT, "celsius");
        binding.switchCelsius.setChecked(tempUnit.equals("celsius"));
        binding.switchFahrenheit.setChecked(tempUnit.equals("fahrenheit"));

        // Load Wind Speed Unit
        String windUnit = sharedPreferences.getString(KEY_WIND_SPEED_UNIT, "ms");
        binding.switchWindMs.setChecked(windUnit.equals("ms"));
        binding.switchWindKmh.setChecked(windUnit.equals("kmh"));

        // Load Pressure Unit
        String pressureUnit = sharedPreferences.getString(KEY_PRESSURE_UNIT, "hpa");
        binding.switchPressureHpa.setChecked(pressureUnit.equals("hpa"));
        binding.switchPressureMbar.setChecked(pressureUnit.equals("mbar"));

        // Load Dark Mode
        boolean darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, true);
        binding.switchDarkMode.setChecked(darkMode);

        // Load Notifications
        boolean notifications = sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
        binding.switchNotifications.setChecked(notifications);

        // Load Language - Fixed logic - Default to Vietnamese
        String language = sharedPreferences.getString(KEY_LANGUAGE, "vi");
        binding.switchEnglish.setChecked(language.equals("en"));
       
    }

    private void setupListeners() {
        // Temperature Unit - Fixed logic
        binding.switchCelsius.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingTemperature) return;

            if (isChecked) {
                isUpdatingTemperature = true;
                binding.switchFahrenheit.setChecked(false);
                isUpdatingTemperature = false;
                saveTemperatureUnit("celsius");
            } else if (!binding.switchFahrenheit.isChecked()) {
                // Don't allow both to be unchecked
                isUpdatingTemperature = true;
                binding.switchCelsius.setChecked(true);
                isUpdatingTemperature = false;
            }
        });

        binding.switchFahrenheit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingTemperature) return;

            if (isChecked) {
                isUpdatingTemperature = true;
                binding.switchCelsius.setChecked(false);
                isUpdatingTemperature = false;
                saveTemperatureUnit("fahrenheit");
            } else if (!binding.switchCelsius.isChecked()) {
                // Don't allow both to be unchecked
                isUpdatingTemperature = true;
                binding.switchFahrenheit.setChecked(true);
                isUpdatingTemperature = false;
            }
        });

        // Wind Speed Unit - Fixed logic
        binding.switchWindMs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingWind) return;

            if (isChecked) {
                isUpdatingWind = true;
                binding.switchWindKmh.setChecked(false);
                isUpdatingWind = false;
                saveWindSpeedUnit("ms");
            } else if (!binding.switchWindKmh.isChecked()) {
                isUpdatingWind = true;
                binding.switchWindMs.setChecked(true);
                isUpdatingWind = false;
            }
        });

        binding.switchWindKmh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingWind) return;

            if (isChecked) {
                isUpdatingWind = true;
                binding.switchWindMs.setChecked(false);
                isUpdatingWind = false;
                saveWindSpeedUnit("kmh");
            } else if (!binding.switchWindMs.isChecked()) {
                isUpdatingWind = true;
                binding.switchWindKmh.setChecked(true);
                isUpdatingWind = false;
            }
        });

        // Pressure Unit - Fixed logic
        binding.switchPressureHpa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingPressure) return;

            if (isChecked) {
                isUpdatingPressure = true;
                binding.switchPressureMbar.setChecked(false);
                isUpdatingPressure = false;
                savePressureUnit("hpa");
            } else if (!binding.switchPressureMbar.isChecked()) {
                isUpdatingPressure = true;
                binding.switchPressureHpa.setChecked(true);
                isUpdatingPressure = false;
            }
        });

        binding.switchPressureMbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingPressure) return;

            if (isChecked) {
                isUpdatingPressure = true;
                binding.switchPressureHpa.setChecked(false);
                isUpdatingPressure = false;
                savePressureUnit("mbar");
            } else if (!binding.switchPressureHpa.isChecked()) {
                isUpdatingPressure = true;
                binding.switchPressureMbar.setChecked(true);
                isUpdatingPressure = false;
            }
        });

        // Dark Mode
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveDarkMode(isChecked);
        });

        // Notifications - Schedule/Cancel notifications when toggled
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotifications(isChecked);
            scheduleNotifications(isChecked);
        });

        // Language Settings
        binding.switchEnglish.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingLanguage) return;

            if (isChecked) {
                isUpdatingLanguage = true;
                binding.switchVietnamese.setChecked(false);
                isUpdatingLanguage = false;
                saveLanguage("en");
                applyLanguage("en");
            } else if (!binding.switchVietnamese.isChecked()) {
                isUpdatingLanguage = true;
                binding.switchEnglish.setChecked(true);
                isUpdatingLanguage = false;
            }
        });

        binding.switchVietnamese.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingLanguage) return;

            if (isChecked) {
                isUpdatingLanguage = true;
                binding.switchEnglish.setChecked(false);
                isUpdatingLanguage = false;
                saveLanguage("vi");
                applyLanguage("vi");
            } else if (!binding.switchEnglish.isChecked()) {
                isUpdatingLanguage = true;
                binding.switchVietnamese.setChecked(true);
                isUpdatingLanguage = false;
            }
        });

        // About button
        binding.btnAbout.setOnClickListener(v -> {
            // TODO: Open About activity or dialog
        });
    }

    private void saveTemperatureUnit(String unit) {
        sharedPreferences.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply();
        setResult(RESULT_OK); // Notify MainActivity to refresh
    }

    private void saveWindSpeedUnit(String unit) {
        sharedPreferences.edit().putString(KEY_WIND_SPEED_UNIT, unit).apply();
        setResult(RESULT_OK);
    }

    private void savePressureUnit(String unit) {
        sharedPreferences.edit().putString(KEY_PRESSURE_UNIT, unit).apply();
        setResult(RESULT_OK);
    }

    private void saveDarkMode(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    private void saveNotifications(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply();
    }

    private void saveLanguage(String language) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply();

        // Notify MainActivity that language changed
        Intent resultIntent = new Intent();
        resultIntent.putExtra("language_changed", true);
        setResult(RESULT_OK, resultIntent);
    }

    private void applyLanguage(String languageCode) {
        LocaleHelper.setLocale(this, languageCode);
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

