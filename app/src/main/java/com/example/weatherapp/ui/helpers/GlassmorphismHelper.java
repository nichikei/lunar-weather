package com.example.weatherapp.ui.helpers;

import android.graphics.BlurMaskFilter;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;

/**
 * Glassmorphism UI Helper
 * Creates modern frosted glass effects for UI elements
 */
public class GlassmorphismHelper {

    /**
     * Apply glassmorphism effect to a view
     * @param view Target view
     * @param blurRadius Blur amount (0-25)
     * @param backgroundAlpha Background transparency (0-255)
     * @param borderColor Border color
     * @param borderWidth Border width in pixels
     */
    public static void applyGlassEffect(View view, float blurRadius, int backgroundAlpha, int borderColor, float borderWidth) {
        if (view == null) return;
        
        // Create gradient drawable for glass effect
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        
        // Semi-transparent white background
        drawable.setColor((backgroundAlpha << 24) | 0x00FFFFFF);
        
        // Rounded corners
        drawable.setCornerRadius(20f);
        
        // Border
        if (borderWidth > 0) {
            drawable.setStroke((int)borderWidth, borderColor);
        }
        
        view.setBackground(drawable);
        view.setElevation(8f);
        
        // Set layer type for blur (note: actual blur requires RenderScript or custom implementation)
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * Apply card glass effect with default settings
     */
    public static void applyCardGlass(View view) {
        applyGlassEffect(view, 15f, 40, 0x30FFFFFF, 1.5f);
    }

    /**
     * Apply button glass effect
     */
    public static void applyButtonGlass(View view) {
        applyGlassEffect(view, 10f, 60, 0x50FFFFFF, 2f);
        view.setElevation(4f);
    }

    /**
     * Apply header glass effect
     */
    public static void applyHeaderGlass(View view) {
        applyGlassEffect(view, 20f, 30, 0x20FFFFFF, 0f);
        view.setElevation(12f);
    }

    /**
     * Apply glassmorphism to all child views in a container
     */
    public static void applyGlassToChildren(ViewGroup container) {
        if (container == null) return;
        
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof ViewGroup) {
                applyCardGlass(child);
            }
        }
    }

    /**
     * Create animated shimmer effect on glass surface
     */
    public static void addShimmerEffect(View view) {
        if (view == null) return;
        
        // Use ValueAnimator instead for repeating animation
        android.animation.ValueAnimator shimmer = android.animation.ValueAnimator.ofFloat(0.95f, 1f);
        shimmer.setDuration(1500);
        shimmer.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        shimmer.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        shimmer.addUpdateListener(animation -> {
            float alpha = (float) animation.getAnimatedValue();
            view.setAlpha(alpha);
        });
        shimmer.start();
    }

    /**
     * Remove glass effects
     */
    public static void removeGlassEffect(View view) {
        if (view == null) return;
        
        view.setBackground(null);
        view.setElevation(0f);
        view.setLayerType(View.LAYER_TYPE_NONE, null);
    }
}
