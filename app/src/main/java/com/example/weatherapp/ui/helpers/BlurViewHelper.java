package com.example.weatherapp.ui.helpers;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

/**
 * Helper class to setup iOS-style glassmorphism using BlurView
 */
public class BlurViewHelper {

    private Activity activity;
    private ViewGroup rootView;

    public BlurViewHelper(Activity activity, ViewGroup rootView) {
        this.activity = activity;
        this.rootView = rootView;
    }

    /**
     * Setup blur effect for a CardView (iOS glassmorphism)
     * @param blurView The BlurView to configure
     * @param blurRadius Blur radius (15-25 recommended)
     * @param overlayColor Semi-transparent color overlay (e.g., 0x4D000000)
     */
    public void setupBlur(BlurView blurView, float blurRadius, int overlayColor) {
        if (blurView == null || rootView == null) return;

        final float radius = blurRadius;

        final Drawable windowBackground = activity.getWindow().getDecorView().getBackground();

        blurView.setupWith(rootView, new RenderScriptBlur(activity))
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)
                .setOverlayColor(overlayColor)
                .setBlurAutoUpdate(true);
    }

    /**
     * Setup blur for bottom navigation (iOS-style tab bar)
     */
    public void setupBottomNavBlur(BlurView blurView) {
        setupBlur(blurView, 20f, 0x4D000000); // Dark semi-transparent
    }

    /**
     * Setup blur for weather cards
     */
    public void setupCardBlur(BlurView blurView) {
        setupBlur(blurView, 15f, 0x4D000000); // Dark semi-transparent
    }

    /**
     * Wrap an existing CardView with BlurView programmatically
     * This is useful if you don't want to modify XML
     */
    public BlurView wrapCardWithBlur(CardView cardView) {
        // Get parent and position
        ViewGroup parent = (ViewGroup) cardView.getParent();
        int index = parent.indexOfChild(cardView);
        ViewGroup.LayoutParams params = cardView.getLayoutParams();

        // Remove card from parent
        parent.removeView(cardView);

        // Create BlurView
        BlurView blurView = new BlurView(activity);
        blurView.setLayoutParams(params);

        // Add card to BlurView
        cardView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        cardView.setCardBackgroundColor(0x00000000); // Transparent
        blurView.addView(cardView);

        // Setup blur
        setupCardBlur(blurView);

        // Add BlurView back to parent
        parent.addView(blurView, index);

        return blurView;
    }
}
