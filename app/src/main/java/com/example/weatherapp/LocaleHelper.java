package com.example.weatherapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

public class LocaleHelper {

    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_LANGUAGE = "language";

    /**
     * Set the app locale and update configuration
     */
    public static Context setLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());

        // Since minSdk is 24+, we can directly use LocaleList
        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);
        configuration.setLocale(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context = context.createConfigurationContext(configuration);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Save the language preference
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();

        return context;
    }

    /**
     * Get the saved language preference
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "vi"); // Changed default from "en" to "vi"
    }

    /**
     * Apply saved locale when app starts
     */
    public static Context onAttach(Context context) {
        String language = getLanguage(context);
        return setLocale(context, language);
    }
}
