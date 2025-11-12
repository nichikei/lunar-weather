package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/**
 * Material Design 3 Animation Helper
 * Provides smooth, modern animations for UI elements
 */
public class MaterialAnimationHelper {
    
    private static final int DEFAULT_DURATION = 400;
    private static final int FAST_DURATION = 250;
    private static final int SLOW_DURATION = 600;
    
    /**
     * Fade in animation with scale
     */
    public static void fadeInScale(View view) {
        fadeInScale(view, DEFAULT_DURATION, 0);
    }
    
    public static void fadeInScale(View view, int duration, int delay) {
        view.setAlpha(0f);
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setStartDelay(delay)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }
    
    /**
     * Fade out animation with scale
     */
    public static void fadeOutScale(View view) {
        fadeOutScale(view, DEFAULT_DURATION, null);
    }
    
    public static void fadeOutScale(View view, int duration, Runnable onComplete) {
        view.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(duration)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }
    
    /**
     * Slide in from bottom
     */
    public static void slideInFromBottom(View view) {
        slideInFromBottom(view, DEFAULT_DURATION, 0);
    }
    
    public static void slideInFromBottom(View view, int duration, int delay) {
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(duration)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
    
    /**
     * Slide out to bottom
     */
    public static void slideOutToBottom(View view) {
        slideOutToBottom(view, DEFAULT_DURATION, null);
    }
    
    public static void slideOutToBottom(View view, int duration, Runnable onComplete) {
        view.animate()
                .translationY(view.getHeight())
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }
    
    /**
     * Slide in from right
     */
    public static void slideInFromRight(View view) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
    
    /**
     * Slide in from left
     */
    public static void slideInFromLeft(View view) {
        view.setTranslationX(-view.getWidth());
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
    
    /**
     * Reveal animation (circular reveal effect simulation)
     */
    public static void reveal(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(SLOW_DURATION)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    /**
     * Bounce animation
     */
    public static void bounce(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(FAST_DURATION);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
    }
    
    /**
     * Pulse animation (for attention)
     */
    public static void pulse(View view) {
        pulse(view, 1);
    }
    
    public static void pulse(View view, int repeatCount) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);
        
        // Set repeat on individual animators
        scaleX.setRepeatCount(repeatCount);
        scaleY.setRepeatCount(repeatCount);
        alpha.setRepeatCount(repeatCount);
        
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(600);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }
    
    /**
     * Rotate animation
     */
    public static void rotate(View view, float fromDegrees, float toDegrees) {
        view.animate()
                .rotation(toDegrees)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
    
    /**
     * Flip animation (card flip effect)
     */
    public static void flip(View view) {
        view.animate()
                .rotationY(180f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setRotationY(0f);
                })
                .start();
    }
    
    /**
     * Shimmer effect (loading placeholder)
     */
    public static ValueAnimator startShimmer(View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(0.3f + (progress * 0.4f));
        });
        animator.start();
        return animator;
    }
    
    /**
     * Stagger animation for list items
     */
    public static void staggeredFadeIn(ViewGroup container) {
        int childCount = container.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = container.getChildAt(i);
            fadeInScale(child, DEFAULT_DURATION, i * 50);
        }
    }
    
    /**
     * Expand/Collapse animation
     */
    public static void expand(View view) {
        view.setVisibility(View.VISIBLE);
        
        final int targetHeight = measureViewHeight(view);
        view.getLayoutParams().height = 0;
        
        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });
        animator.setDuration(DEFAULT_DURATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }
    
    public static void collapse(View view) {
        final int initialHeight = view.getHeight();
        
        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.addUpdateListener(animation -> {
            view.getLayoutParams().height = (int) animation.getAnimatedValue();
            view.requestLayout();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.setDuration(DEFAULT_DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
    
    /**
     * Cross-fade between two views
     */
    public static void crossFade(View viewOut, View viewIn) {
        viewIn.setAlpha(0f);
        viewIn.setVisibility(View.VISIBLE);
        
        viewIn.animate()
                .alpha(1f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(null);
        
        viewOut.animate()
                .alpha(0f)
                .setDuration(DEFAULT_DURATION)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(() -> {
                    viewOut.setVisibility(View.GONE);
                    viewOut.setAlpha(1f);
                });
    }
    
    /**
     * Measure view height for expand animation
     */
    private static int measureViewHeight(View view) {
        view.measure(
                View.MeasureSpec.makeMeasureSpec(((View) view.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        return view.getMeasuredHeight();
    }
}
