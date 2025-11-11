package com.example.weatherapp.ui.helpers;

import android.animation.ValueAnimator;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

/**
 * Helper for creating shimmer loading effects
 */
public class ShimmerHelper {
    
    /**
     * Apply shimmer effect to a view
     */
    public static void applyShimmer(View view) {
        if (view == null) return;
        
        ValueAnimator shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        shimmerAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(0.3f + (progress * 0.4f)); // Pulse between 0.3 and 0.7
        });
        
        shimmerAnimator.start();
        
        // Store animator in tag to stop it later
        view.setTag(shimmerAnimator);
    }
    
    /**
     * Stop shimmer effect
     */
    public static void stopShimmer(View view) {
        if (view == null) return;
        
        Object tag = view.getTag();
        if (tag instanceof ValueAnimator) {
            ((ValueAnimator) tag).cancel();
            view.setTag(null);
        }
        
        view.setAlpha(1f);
    }
    
    /**
     * Create pulsing glow effect for important elements
     */
    public static void applyGlowPulse(View view) {
        if (view == null) return;
        
        ValueAnimator glowAnimator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        glowAnimator.setDuration(2000);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        glowAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
        });
        
        glowAnimator.start();
    }
}
