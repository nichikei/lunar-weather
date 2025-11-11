package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Helper class for creating smooth parallax animations
 */
public class ParallaxAnimationHelper {

    /**
     * Create a smooth parallax effect on a background view
     * with professional easing and timing
     */
    public static void animateBackgroundParallax(View backgroundView, Runnable onComplete) {
        if (backgroundView == null) return;

        // Create zoom and lift animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(backgroundView, "scaleX", 1f, 1.08f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(backgroundView, "scaleY", 1f, 1.08f, 1f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(backgroundView, "translationY", 0f, -50f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0.9f, 1f);

        // Configure timing and interpolation
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        translateY.setDuration(1000);
        alpha.setDuration(1000);

        // Use smooth interpolator for natural feel
        DecelerateInterpolator smoothInterpolator = new DecelerateInterpolator(1.5f);
        scaleX.setInterpolator(smoothInterpolator);
        scaleY.setInterpolator(smoothInterpolator);
        translateY.setInterpolator(smoothInterpolator);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());

        // Combine animations
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, translateY, alpha);
        
        if (onComplete != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onComplete.run();
                }
            });
        }

        animatorSet.start();
    }

    /**
     * Create a cascading fade-in effect for multiple views
     */
    public static void animateCascadingFade(View[] views, long delayIncrement) {
        if (views == null || views.length == 0) return;

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;

            long delay = i * delayIncrement;
            
            // Fade out first
            view.animate()
                    .alpha(0.2f)
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(250)
                    .setStartDelay(delay)
                    .withEndAction(() -> {
                        // Then fade in with slight overshoot
                        view.animate()
                                .alpha(1f)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(400)
                                .setInterpolator(new OvershootInterpolator(0.5f))
                                .start();
                    })
                    .start();
        }
    }

    /**
     * Create a smooth slide and fade effect
     */
    public static void animateSlideAndFade(View view, float fromY, float toY, long duration, long delay) {
        if (view == null) return;

        view.setAlpha(0f);
        view.setTranslationY(fromY);

        view.animate()
                .alpha(1f)
                .translationY(toY)
                .setDuration(duration)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    /**
     * Create a pulsing effect for emphasis
     */
    public static void animatePulse(View view, float minScale, float maxScale, long duration) {
        if (view == null) return;

        ValueAnimator pulseAnimator = ValueAnimator.ofFloat(minScale, maxScale);
        pulseAnimator.setDuration(duration);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(1);

        pulseAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
        });

        pulseAnimator.start();
    }

    /**
     * Create a wave effect - views moving in sequence
     */
    public static void animateWave(View[] views, float amplitude, long duration, long delayBetween) {
        if (views == null || views.length == 0) return;

        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            if (view == null) continue;

            long delay = i * delayBetween;
            
            view.animate()
                    .translationY(-amplitude)
                    .setDuration(duration / 2)
                    .setStartDelay(delay)
                    .setInterpolator(new DecelerateInterpolator())
                    .withEndAction(() -> {
                        view.animate()
                                .translationY(0f)
                                .setDuration(duration / 2)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .start();
                    })
                    .start();
        }
    }

    /**
     * Create a smooth rotation effect
     */
    public static void animateRotation(View view, float fromDegrees, float toDegrees, long duration) {
        if (view == null) return;

        view.animate()
                .rotation(toDegrees)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Cancel all animations on a view
     */
    public static void cancelAnimations(View view) {
        if (view != null) {
            view.animate().cancel();
        }
    }

    /**
     * Cancel animations on multiple views
     */
    public static void cancelAnimations(View[] views) {
        if (views == null) return;
        
        for (View view : views) {
            if (view != null) {
                view.animate().cancel();
            }
        }
    }
}
