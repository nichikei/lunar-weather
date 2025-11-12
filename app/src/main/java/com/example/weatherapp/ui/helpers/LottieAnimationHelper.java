package com.example.weatherapp.ui.helpers;

import android.content.Context;
import android.view.View;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.weatherapp.R;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for managing Lottie weather animations
 * Provides weather condition mapping and animation controls
 */
public class LottieAnimationHelper {
    
    private static final Map<String, Integer> WEATHER_ANIMATIONS = new HashMap<>();
    
    static {
        // Map weather conditions to Lottie animation resources
        WEATHER_ANIMATIONS.put("clear", R.raw.weather_sunny);
        WEATHER_ANIMATIONS.put("sunny", R.raw.weather_sunny);
        WEATHER_ANIMATIONS.put("rain", R.raw.weather_rainy);
        WEATHER_ANIMATIONS.put("rainy", R.raw.weather_rainy);
        WEATHER_ANIMATIONS.put("drizzle", R.raw.weather_rainy);
        WEATHER_ANIMATIONS.put("clouds", R.raw.weather_cloudy);
        WEATHER_ANIMATIONS.put("cloudy", R.raw.weather_cloudy);
        WEATHER_ANIMATIONS.put("snow", R.raw.weather_snowy);
        WEATHER_ANIMATIONS.put("snowy", R.raw.weather_snowy);
    }
    
    /**
     * Set weather animation based on condition
     */
    public static void setWeatherAnimation(LottieAnimationView animationView, String weatherCondition) {
        if (animationView == null || weatherCondition == null) return;
        
        String condition = weatherCondition.toLowerCase();
        Integer animationRes = WEATHER_ANIMATIONS.get(condition);
        
        if (animationRes != null) {
            animationView.setAnimation(animationRes);
            animationView.setRepeatCount(LottieDrawable.INFINITE);
            animationView.playAnimation();
        } else {
            // Default to sunny animation
            animationView.setAnimation(R.raw.weather_sunny);
            animationView.setRepeatCount(LottieDrawable.INFINITE);
            animationView.playAnimation();
        }
    }
    
    /**
     * Show loading animation
     */
    public static void showLoadingAnimation(LottieAnimationView animationView) {
        if (animationView == null) return;
        
        animationView.setAnimation(R.raw.weather_loading);
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setVisibility(View.VISIBLE);
        animationView.playAnimation();
    }
    
    /**
     * Hide loading animation
     */
    public static void hideLoadingAnimation(LottieAnimationView animationView) {
        if (animationView == null) return;
        
        animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }
    
    /**
     * Play animation once with callback
     */
    public static void playOnce(LottieAnimationView animationView, int animationRes, Runnable onComplete) {
        if (animationView == null) return;
        
        animationView.setAnimation(animationRes);
        animationView.setRepeatCount(0);
        
        if (onComplete != null) {
            animationView.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(android.animation.Animator animation) {}
                
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    onComplete.run();
                    animationView.removeAllAnimatorListeners();
                }
                
                @Override
                public void onAnimationCancel(android.animation.Animator animation) {
                    animationView.removeAllAnimatorListeners();
                }
                
                @Override
                public void onAnimationRepeat(android.animation.Animator animation) {}
            });
        }
        
        animationView.playAnimation();
    }
    
    /**
     * Fade in Lottie animation with scale
     */
    public static void fadeInWithScale(LottieAnimationView animationView, int animationRes, long duration) {
        if (animationView == null) return;
        
        animationView.setAnimation(animationRes);
        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setAlpha(0f);
        animationView.setScaleX(0.3f);
        animationView.setScaleY(0.3f);
        animationView.setVisibility(View.VISIBLE);
        
        animationView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .withEndAction(() -> animationView.playAnimation())
                .start();
    }
    
    /**
     * Crossfade between two weather animations
     */
    public static void crossfadeWeatherAnimations(
            LottieAnimationView currentView,
            LottieAnimationView nextView,
            String newWeatherCondition,
            long duration) {
        
        if (currentView == null || nextView == null) return;
        
        // Setup next animation
        String condition = newWeatherCondition.toLowerCase();
        Integer animationRes = WEATHER_ANIMATIONS.getOrDefault(condition, R.raw.weather_sunny);
        
        nextView.setAnimation(animationRes);
        nextView.setRepeatCount(LottieDrawable.INFINITE);
        nextView.setAlpha(0f);
        nextView.setVisibility(View.VISIBLE);
        
        // Crossfade
        currentView.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> {
                    currentView.cancelAnimation();
                    currentView.setVisibility(View.GONE);
                })
                .start();
        
        nextView.animate()
                .alpha(1f)
                .setDuration(duration)
                .withStartAction(() -> nextView.playAnimation())
                .start();
    }
    
    /**
     * Pause animation with fade out
     */
    public static void pauseWithFade(LottieAnimationView animationView, long duration) {
        if (animationView == null) return;
        
        animationView.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> {
                    animationView.pauseAnimation();
                    animationView.setVisibility(View.GONE);
                })
                .start();
    }
    
    /**
     * Resume animation with fade in
     */
    public static void resumeWithFade(LottieAnimationView animationView, long duration) {
        if (animationView == null) return;
        
        animationView.setAlpha(0f);
        animationView.setVisibility(View.VISIBLE);
        animationView.resumeAnimation();
        
        animationView.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }
    
    /**
     * Get animation resource for weather condition
     */
    public static int getAnimationResourceForWeather(String weatherCondition) {
        if (weatherCondition == null) return R.raw.weather_sunny;
        
        String condition = weatherCondition.toLowerCase();
        return WEATHER_ANIMATIONS.getOrDefault(condition, R.raw.weather_sunny);
    }
}
