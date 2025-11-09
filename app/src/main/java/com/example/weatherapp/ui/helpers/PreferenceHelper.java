package com.example.weatherapp.ui.helpers;

import android.content.SharedPreferences;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;

/**
 * Helper class for managing preference switches in SettingsActivity
 * Handles mutual exclusion between paired switches
 */
public class PreferenceHelper {

    private final SharedPreferences sharedPreferences;
    private final PreferenceChangeListener changeListener;

    // Flags to prevent infinite loops
    private boolean isUpdating = false;

    public interface PreferenceChangeListener {
        void onPreferenceChanged();
    }

    public PreferenceHelper(SharedPreferences sharedPreferences, PreferenceChangeListener changeListener) {
        this.sharedPreferences = sharedPreferences;
        this.changeListener = changeListener;
    }

    /**
     * Setup a pair of mutually exclusive switches
     * When one is checked, the other is automatically unchecked
     */
    public void setupSwitchPair(
            SwitchCompat switch1,
            SwitchCompat switch2,
            String preferenceKey,
            String value1,
            String value2
    ) {
        // Load current value
        String currentValue = sharedPreferences.getString(preferenceKey, value1);
        switch1.setChecked(currentValue.equals(value1));
        switch2.setChecked(currentValue.equals(value2));

        // Setup listeners
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdating) return;

            if (isChecked) {
                isUpdating = true;
                switch2.setChecked(false);
                isUpdating = false;
                savePreference(preferenceKey, value1);
            } else if (!switch2.isChecked()) {
                // Don't allow both to be unchecked
                isUpdating = true;
                switch1.setChecked(true);
                isUpdating = false;
            }
        });

        switch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdating) return;

            if (isChecked) {
                isUpdating = true;
                switch1.setChecked(false);
                isUpdating = false;
                savePreference(preferenceKey, value2);
            } else if (!switch1.isChecked()) {
                // Don't allow both to be unchecked
                isUpdating = true;
                switch2.setChecked(true);
                isUpdating = false;
            }
        });
    }

    /**
     * Setup a single boolean switch
     */
    public void setupBooleanSwitch(
            SwitchCompat switchCompat,
            String preferenceKey,
            boolean defaultValue,
            BooleanChangeCallback callback
    ) {
        boolean currentValue = sharedPreferences.getBoolean(preferenceKey, defaultValue);
        switchCompat.setChecked(currentValue);

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean(preferenceKey, isChecked);
            if (callback != null) {
                callback.onChanged(isChecked);
            }
        });
    }

    /**
     * Setup a language switch (special case - triggers recreation)
     */
    public void setupLanguageSwitch(
            SwitchCompat switchCompat,
            String preferenceKey,
            String value,
            LanguageChangeCallback callback
    ) {
        String currentValue = sharedPreferences.getString(preferenceKey, "vi");
        switchCompat.setChecked(currentValue.equals(value));

        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdating) return;

            if (isChecked) {
                isUpdating = true;
                isUpdating = false;
                savePreference(preferenceKey, value);
                if (callback != null) {
                    callback.onLanguageChanged(value);
                }
            }
        });
    }

    private void savePreference(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
        if (changeListener != null) {
            changeListener.onPreferenceChanged();
        }
    }

    private void saveBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public interface BooleanChangeCallback {
        void onChanged(boolean value);
    }

    public interface LanguageChangeCallback {
        void onLanguageChanged(String language);
    }
}
