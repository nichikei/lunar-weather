package com.example.weatherapp.utils;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;

/**
 * Helper class to apply blur effects to views using RenderEffect (API 31+)
 * Creates a glass morphism/frosted glass effect
 *
 * Chuẩn Glass Morphism:
 * - RenderEffect: 25f × 25f (TileMode.CLAMP)
 * - Overlay: #14FFFFFF (8% white)
 * - Stroke: #33FFFFFF, 1dp
 */
public class BlurHelper {

    /**
     * Apply blur effect to a view
     *
     * @param view       The view to blur
     * @param blurRadius Blur radius in pixels (chuẩn: 25px)
     */
    public static void applyBlur(View view, float blurRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffect blurEffect = RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP
            );
            view.setRenderEffect(blurEffect);
        }
    }

    /**
     * Apply glass morphism effect (blur only, không thay đổi alpha)
     * Background overlay sẽ được xử lý bởi drawable
     *
     * @param view       The view to apply effect to
     * @param blurRadius Blur radius (chuẩn: 25px)
     */
    public static void applyGlassMorphism(View view, float blurRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Chỉ apply blur effect, giữ nguyên alpha = 1.0
            RenderEffect blurEffect = RenderEffect.createBlurEffect(
                    blurRadius,
                    blurRadius,
                    Shader.TileMode.CLAMP
            );
            view.setRenderEffect(blurEffect);
        }
    }

    /**
     * Remove blur effect from a view
     *
     * @param view The view to remove effect from
     */
    public static void removeBlur(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null);
        }
    }

    /**
     * Apply blur to a container background - CHUẨN GLASS MORPHISM
     * Perfect for cards, panels, and dialogs
     * Radius: 25px theo thiết kế
     */
    public static void applyCardBlur(View view) {
        applyGlassMorphism(view, 25f);
    }

    /**
     * Apply subtle blur for overlays - CHUẨN GLASS MORPHISM
     * Radius: 25px theo thiết kế
     */
    public static void applyOverlayBlur(View view) {
        applyGlassMorphism(view, 25f);
    }

    /**
     * Apply blur for modals/dialogs - CHUẨN GLASS MORPHISM
     * Radius: 25px theo thiết kế
     */
    public static void applyModalBlur(View view) {
        applyGlassMorphism(view, 25f);
    }
}

