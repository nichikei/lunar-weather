package com.example.weatherapp.ui.helpers;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Helper for adding flagship-level visual effects
 */
public class FlagshipEffectsHelper {
    
    /**
     * Add subtle glow effect to a view
     */
    public static void addGlow(View view, int glowColor, float radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setOutlineAmbientShadowColor(glowColor);
            view.setOutlineSpotShadowColor(glowColor);
        }
        view.setElevation(radius);
    }
    
    /**
     * Apply gradient background to view
     */
    public static void applyGradientBackground(View view, int startColor, int endColor, int angle) {
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.values()[angle / 45],
            new int[]{startColor, endColor}
        );
        gradient.setCornerRadius(28f);
        view.setBackground(gradient);
    }
    
    /**
     * Add depth with layered shadows
     */
    public static void addDepthShadow(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setOutlineAmbientShadowColor(Color.parseColor("#40000000"));
            view.setOutlineSpotShadowColor(Color.parseColor("#60000000"));
            view.setElevation(12f);
            view.setTranslationZ(2f);
        }
    }
    
    /**
     * Create frosted glass effect (premium blur)
     */
    public static void applyFrostedGlass(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // RenderEffect blur for Android 12+
            try {
                android.graphics.RenderEffect blurEffect = 
                    android.graphics.RenderEffect.createBlurEffect(
                        25f, 25f, 
                        android.graphics.Shader.TileMode.CLAMP
                    );
                view.setRenderEffect(blurEffect);
            } catch (Exception e) {
                // Fallback for devices that don't support RenderEffect
                view.setAlpha(0.95f);
            }
        }
    }
    
    /**
     * Add premium card elevation with custom shadow
     */
    public static void enhanceCard(View view) {
        view.setElevation(8f);
        view.setTranslationZ(1f);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.setOutlineAmbientShadowColor(Color.parseColor("#30000000"));
            view.setOutlineSpotShadowColor(Color.parseColor("#50000000"));
        }
    }
}
