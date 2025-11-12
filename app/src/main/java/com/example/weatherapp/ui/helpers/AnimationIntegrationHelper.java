package com.example.weatherapp.ui.helpers;

import android.content.Context;
import android.view.View;
import androidx.core.widget.NestedScrollView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapp.ui.views.ParticleEffectView;
import com.example.weatherapp.ui.views.WeatherRefreshLayout;

/**
 * Integration helper for all advanced animations
 * Manages Lottie, Parallax, Morphing, Particles, and Pull-to-Refresh
 */
public class AnimationIntegrationHelper {
    
    private final Context context;
    private final LottieAnimationHelper lottieHelper;
    private final MorphingTransitionHelper morphingHelper;
    
    // Views
    private LottieAnimationView lottieWeatherView;
    private ParticleEffectView particleEffectView;
    private WeatherRefreshLayout refreshLayout;
    private View parallaxBackground;
    
    // Current weather state
    private String currentWeatherCondition = "clear";
    
    public AnimationIntegrationHelper(Context context) {
        this.context = context;
        this.lottieHelper = new LottieAnimationHelper();
        this.morphingHelper = new MorphingTransitionHelper(context);
    }
    
    /**
     * Initialize all animation views
     */
    public void initializeViews(
            LottieAnimationView lottieView,
            ParticleEffectView particleView,
            WeatherRefreshLayout refreshLayout,
            View parallaxBackground) {
        
        this.lottieWeatherView = lottieView;
        this.particleEffectView = particleView;
        this.refreshLayout = refreshLayout;
        this.parallaxBackground = parallaxBackground;
    }
    
    /**
     * Setup parallax scroll effect
     */
    public void setupParallaxEffect(android.widget.ScrollView scrollView) {
        if (scrollView == null || parallaxBackground == null) return;
        
        scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            // Parallax effect: move background slower than content
            float parallaxSpeed = 0.5f;
            parallaxBackground.setTranslationY(scrollY * parallaxSpeed);
            
            // Optional fade effect
            float alpha = 1f - (Math.abs(scrollY) / 1000f);
            parallaxBackground.setAlpha(Math.max(0.3f, Math.min(1f, alpha)));
        });
    }
    
    /**
     * Setup pull-to-refresh
     */
    public void setupPullToRefresh(WeatherRefreshLayout.OnRefreshListener listener) {
        if (refreshLayout == null) return;
        
        refreshLayout.setOnRefreshListener(listener);
    }
    
    /**
     * Update all animations based on weather condition
     */
    public void updateWeatherAnimations(String weatherCondition, boolean animate) {
        if (weatherCondition == null || weatherCondition.equals(currentWeatherCondition)) {
            return;
        }
        
        String oldCondition = currentWeatherCondition;
        currentWeatherCondition = weatherCondition.toLowerCase();
        
        // Update Lottie animation
        updateLottieAnimation(animate);
        
        // Update particle effects
        updateParticleEffect();
        
        // Store old condition for transitions
        String finalOldCondition = oldCondition;
    }
    
    /**
     * Update Lottie weather animation
     */
    private void updateLottieAnimation(boolean animate) {
        if (lottieWeatherView == null) return;
        
        if (animate) {
            LottieAnimationHelper.fadeInWithScale(
                lottieWeatherView,
                LottieAnimationHelper.getAnimationResourceForWeather(currentWeatherCondition),
                500
            );
        } else {
            LottieAnimationHelper.setWeatherAnimation(lottieWeatherView, currentWeatherCondition);
        }
    }
    
    /**
     * Update particle effect based on weather
     */
    private void updateParticleEffect() {
        if (particleEffectView == null) return;
        
        String condition = currentWeatherCondition.toLowerCase();
        
        if (condition.contains("rain") || condition.contains("drizzle")) {
            particleEffectView.startAnimation(ParticleEffectView.ParticleType.RAIN);
        } else if (condition.contains("snow")) {
            particleEffectView.startAnimation(ParticleEffectView.ParticleType.SNOW);
        } else if (condition.contains("clear") && isNightTime()) {
            particleEffectView.startAnimation(ParticleEffectView.ParticleType.STARS);
        } else {
            particleEffectView.stopAnimation();
        }
    }
    
    /**
     * Morph background with weather transition
     */
    public void morphBackground(View backgroundView) {
        if (backgroundView == null) return;
        
        morphingHelper.morphBackgroundColor(backgroundView, currentWeatherCondition, 800);
    }
    
    /**
     * Morph card views with weather transition
     */
    public void morphCardViews(androidx.cardview.widget.CardView... cardViews) {
        for (androidx.cardview.widget.CardView card : cardViews) {
            if (card != null) {
                morphingHelper.morphCardView(card, currentWeatherCondition, 600);
            }
        }
    }
    
    /**
     * Show loading animation
     */
    public void showLoadingAnimation() {
        if (lottieWeatherView != null) {
            LottieAnimationHelper.showLoadingAnimation(lottieWeatherView);
        }
    }
    
    /**
     * Hide loading animation
     */
    public void hideLoadingAnimation() {
        if (lottieWeatherView != null) {
            LottieAnimationHelper.hideLoadingAnimation(lottieWeatherView);
        }
    }
    
    /**
     * Start refresh animation
     */
    public void startRefreshAnimation() {
        if (refreshLayout != null) {
            refreshLayout.startWeatherRefreshAnimation();
        }
    }
    
    /**
     * Stop refresh animation
     */
    public void stopRefreshAnimation() {
        if (refreshLayout != null) {
            refreshLayout.stopWeatherRefreshAnimation();
        }
    }
    
    /**
     * Pause all animations
     */
    public void pauseAllAnimations() {
        if (lottieWeatherView != null) {
            lottieWeatherView.pauseAnimation();
        }
        
        if (particleEffectView != null) {
            particleEffectView.stopAnimation();
        }
    }
    
    /**
     * Resume all animations
     */
    public void resumeAllAnimations() {
        if (lottieWeatherView != null && lottieWeatherView.getVisibility() == View.VISIBLE) {
            lottieWeatherView.resumeAnimation();
        }
        
        updateParticleEffect();
    }
    
    /**
     * Cleanup animations
     */
    public void cleanup() {
        if (lottieWeatherView != null) {
            lottieWeatherView.cancelAnimation();
        }
        
        if (particleEffectView != null) {
            particleEffectView.stopAnimation();
        }
    }
    
    /**
     * Helper method to determine if it's night time
     */
    private boolean isNightTime() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        return hour >= 20 || hour <= 6;
    }
    
    /**
     * Get current weather condition
     */
    public String getCurrentWeatherCondition() {
        return currentWeatherCondition;
    }
    
    /**
     * Set particle count for performance tuning
     */
    public void setParticleCount(int count) {
        if (particleEffectView != null) {
            particleEffectView.setParticleCount(count);
        }
    }
    
    /**
     * Get Lottie helper for advanced usage
     */
    public LottieAnimationHelper getLottieHelper() {
        return lottieHelper;
    }
    
    /**
     * Get Morphing helper for advanced usage
     */
    public MorphingTransitionHelper getMorphingHelper() {
        return morphingHelper;
    }
}
