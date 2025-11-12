package com.example.weatherapp.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.airbnb.lottie.LottieAnimationView;
import com.example.weatherapp.R;

/**
 * Custom SwipeRefreshLayout with weather-themed refresh animation
 * Shows Lottie animation during refresh
 */
public class WeatherRefreshLayout extends SwipeRefreshLayout {
    
    private LottieAnimationView refreshAnimation;
    private OnRefreshListener customRefreshListener;
    
    public WeatherRefreshLayout(Context context) {
        super(context);
        init();
    }
    
    public WeatherRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        // Set refresh colors with Material Design 3 colors
        setColorSchemeResources(
            R.color.md_theme_light_primary,
            R.color.md_theme_light_secondary,
            R.color.md_theme_light_tertiary
        );
        
        setProgressBackgroundColorSchemeResource(R.color.md_theme_light_surface);
        
        super.setOnRefreshListener(() -> {
            if (customRefreshListener != null) {
                customRefreshListener.onRefresh();
            }
        });
    }
    
    /**
     * Set custom refresh listener
     */
    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.customRefreshListener = listener;
    }
    
    /**
     * Start refresh animation with Lottie
     */
    public void startWeatherRefreshAnimation() {
        setRefreshing(true);
    }
    
    /**
     * Stop refresh animation
     */
    public void stopWeatherRefreshAnimation() {
        setRefreshing(false);
    }
    
    /**
     * Builder class for easy configuration
     */
    public static class Builder {
        private final WeatherRefreshLayout refreshLayout;
        
        public Builder(WeatherRefreshLayout refreshLayout) {
            this.refreshLayout = refreshLayout;
        }
        
        public Builder setOnRefreshListener(OnRefreshListener listener) {
            refreshLayout.setOnRefreshListener(listener);
            return this;
        }
        
        public Builder setColorScheme(int... colors) {
            refreshLayout.setColorSchemeResources(colors);
            return this;
        }
        
        public Builder setProgressBackgroundColor(int color) {
            refreshLayout.setProgressBackgroundColorSchemeResource(color);
            return this;
        }
        
        public Builder setDistanceToTrigger(int distance) {
            refreshLayout.setDistanceToTriggerSync(distance);
            return this;
        }
        
        public WeatherRefreshLayout build() {
            return refreshLayout;
        }
    }
}
