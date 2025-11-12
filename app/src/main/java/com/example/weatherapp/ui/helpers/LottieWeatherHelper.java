package com.example.weatherapp.ui.helpers;

import android.content.Context;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapp.R;

/**
 * Helper class to manage Lottie weather animations
 * Maps weather conditions to appropriate Lottie animation files
 */
public class LottieWeatherHelper {
    
    private static final String TAG = "LottieWeatherHelper";
    
    // Weather animation mapping
    public static final String ANIM_CLEAR_DAY = "https://lottie.host/4f0d0f8f-4c5e-4f6e-9e5d-0c0a9e6f5f5f/FXzQfZzQfZ.json";
    public static final String ANIM_CLEAR_NIGHT = "https://lottie.host/embed/b0b0b0b0-b0b0-b0b0-b0b0-b0b0b0b0b0b0/night.json";
    public static final String ANIM_RAIN = "https://lottie.host/embed/rain.json";
    public static final String ANIM_HEAVY_RAIN = "https://lottie.host/embed/heavy_rain.json";
    public static final String ANIM_THUNDERSTORM = "https://lottie.host/embed/thunderstorm.json";
    public static final String ANIM_SNOW = "https://lottie.host/embed/snow.json";
    public static final String ANIM_CLOUDS = "https://lottie.host/embed/clouds.json";
    public static final String ANIM_FOG = "https://lottie.host/embed/fog.json";
    public static final String ANIM_WIND = "https://lottie.host/embed/wind.json";
    
    /**
     * Set weather animation based on condition and time of day
     */
    public static void setWeatherAnimation(LottieAnimationView lottieView, String weatherCondition, boolean isNight) {
        if (lottieView == null) return;
        
        String condition = weatherCondition.toLowerCase();
        String animationUrl = null;
        
        // Map weather conditions to Lottie animations
        if (condition.contains("thunderstorm") || condition.contains("storm")) {
            // Use raw resource for thunderstorm (we'll create a simple one)
            lottieView.setAnimation(R.raw.weather_thunderstorm);
            lottieView.setSpeed(1.2f);
            
        } else if (condition.contains("drizzle") || (condition.contains("rain") && condition.contains("light"))) {
            // Light rain
            lottieView.setAnimation(R.raw.weather_light_rain);
            lottieView.setSpeed(1.0f);
            
        } else if (condition.contains("rain")) {
            // Heavy rain
            lottieView.setAnimation(R.raw.weather_heavy_rain);
            lottieView.setSpeed(1.3f);
            
        } else if (condition.contains("snow")) {
            // Snow
            lottieView.setAnimation(R.raw.weather_snow);
            lottieView.setSpeed(0.8f);
            
        } else if (condition.contains("mist") || condition.contains("fog") || condition.contains("haze")) {
            // Fog/Mist
            lottieView.setAnimation(R.raw.weather_fog);
            lottieView.setSpeed(0.6f);
            
        } else if (condition.contains("cloud")) {
            // Cloudy
            lottieView.setAnimation(R.raw.weather_cloudy);
            lottieView.setSpeed(0.7f);
            
        } else if (condition.contains("clear") || condition.contains("sun")) {
            if (isNight) {
                // Clear night with stars
                lottieView.setAnimation(R.raw.weather_clear_night);
                lottieView.setSpeed(0.5f);
            } else {
                // Sunny day
                lottieView.setAnimation(R.raw.weather_clear_day);
                lottieView.setSpeed(1.0f);
            }
            
        } else if (condition.contains("wind")) {
            // Windy
            lottieView.setAnimation(R.raw.weather_wind);
            lottieView.setSpeed(1.5f);
            
        } else {
            // Default - clear day
            lottieView.setAnimation(R.raw.weather_clear_day);
            lottieView.setSpeed(1.0f);
        }
        
        // Always loop and play
        lottieView.setRepeatCount(Integer.MAX_VALUE);
        lottieView.playAnimation();
        
        Log.d(TAG, "Lottie animation set: " + condition + " | Night: " + isNight);
    }
    
    /**
     * Set animation alpha for blending with background
     */
    public static void setAnimationAlpha(LottieAnimationView lottieView, float alpha) {
        if (lottieView != null) {
            lottieView.setAlpha(alpha);
        }
    }
    
    /**
     * Pause animation to save battery
     */
    public static void pauseAnimation(LottieAnimationView lottieView) {
        if (lottieView != null && lottieView.isAnimating()) {
            lottieView.pauseAnimation();
        }
    }
    
    /**
     * Resume animation
     */
    public static void resumeAnimation(LottieAnimationView lottieView) {
        if (lottieView != null && !lottieView.isAnimating()) {
            lottieView.resumeAnimation();
        }
    }
}
