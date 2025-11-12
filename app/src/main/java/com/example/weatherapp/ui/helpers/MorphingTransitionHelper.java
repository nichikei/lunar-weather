package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.weatherapp.R;

/**
 * Helper class for smooth morphing transitions between weather states
 * Handles background colors, shapes, and layout transformations
 */
public class MorphingTransitionHelper {
    
    private final Context context;
    private static final long DEFAULT_DURATION = 500;
    
    public MorphingTransitionHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Morph background color based on weather condition
     */
    public void morphBackgroundColor(View targetView, String weatherCondition) {
        morphBackgroundColor(targetView, weatherCondition, DEFAULT_DURATION);
    }
    
    public void morphBackgroundColor(View targetView, String weatherCondition, long duration) {
        if (targetView == null || weatherCondition == null) return;
        
        int startColor = getCurrentBackgroundColor(targetView);
        int endColor = getColorForWeather(weatherCondition);
        
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setDuration(duration);
        colorAnimation.addUpdateListener(animator -> 
            targetView.setBackgroundColor((int) animator.getAnimatedValue())
        );
        colorAnimation.start();
    }
    
    /**
     * Morph gradient background based on weather
     */
    public void morphGradientBackground(View targetView, String weatherCondition, long duration) {
        if (targetView == null || weatherCondition == null) return;
        
        int[] colors = getGradientColorsForWeather(weatherCondition);
        GradientDrawable gradient = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            colors
        );
        gradient.setCornerRadius(24f);
        
        // Fade transition to new gradient
        targetView.animate()
                .alpha(0f)
                .setDuration(duration / 2)
                .withEndAction(() -> {
                    targetView.setBackground(gradient);
                    targetView.animate()
                            .alpha(1f)
                            .setDuration(duration / 2)
                            .start();
                })
                .start();
    }
    
    /**
     * Morph card view with transition
     */
    public void morphCardView(CardView cardView, String weatherCondition, long duration) {
        if (cardView == null) return;
        
        ViewGroup parent = (ViewGroup) cardView.getParent();
        if (parent == null) return;
        
        // Create transition set
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.addTransition(new ChangeBounds());
        transitionSet.addTransition(new Fade());
        transitionSet.setDuration(duration);
        
        TransitionManager.beginDelayedTransition(parent, transitionSet);
        
        // Update card properties
        int color = getColorForWeather(weatherCondition);
        cardView.setCardBackgroundColor(color);
        cardView.setRadius(cardView.getRadius() == 24f ? 32f : 24f); // Toggle radius
    }
    
    /**
     * Morph weather icon with rotation and scale
     */
    public void morphWeatherIcon(ImageView iconView, int newIconRes, long duration) {
        if (iconView == null) return;
        
        // Rotate and scale out
        iconView.animate()
                .rotation(180f)
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(duration / 2)
                .withEndAction(() -> {
                    // Change icon
                    iconView.setImageResource(newIconRes);
                    iconView.setRotation(0f);
                    
                    // Scale in
                    iconView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(duration / 2)
                            .start();
                })
                .start();
    }
    
    /**
     * Morph text with fade and slide
     */
    public void morphText(TextView textView, String newText, long duration) {
        if (textView == null || newText == null) return;
        
        textView.animate()
                .alpha(0f)
                .translationY(-20f)
                .setDuration(duration / 2)
                .withEndAction(() -> {
                    textView.setText(newText);
                    textView.setTranslationY(20f);
                    textView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(duration / 2)
                            .start();
                })
                .start();
    }
    
    /**
     * Create smooth layout transition
     */
    public void beginLayoutTransition(ViewGroup container) {
        beginLayoutTransition(container, DEFAULT_DURATION);
    }
    
    public void beginLayoutTransition(ViewGroup container, long duration) {
        if (container == null) return;
        
        AutoTransition transition = new AutoTransition();
        transition.setDuration(duration);
        TransitionManager.beginDelayedTransition(container, transition);
    }
    
    /**
     * Morph multiple views simultaneously
     */
    public void morphWeatherState(
            ViewGroup container,
            View backgroundView,
            ImageView iconView,
            TextView tempView,
            String weatherCondition,
            int iconRes,
            String temperature) {
        
        if (container == null) return;
        
        // Begin container transition
        beginLayoutTransition(container);
        
        // Morph all components
        if (backgroundView != null) {
            morphBackgroundColor(backgroundView, weatherCondition, DEFAULT_DURATION);
        }
        
        if (iconView != null) {
            morphWeatherIcon(iconView, iconRes, DEFAULT_DURATION);
        }
        
        if (tempView != null && temperature != null) {
            morphText(tempView, temperature, DEFAULT_DURATION);
        }
    }
    
    /**
     * Expand view with morph effect
     */
    public void expandWithMorph(View view, int finalHeight, long duration) {
        if (view == null) return;
        
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int startHeight = view.getHeight();
        
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, finalHeight);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.start();
    }
    
    /**
     * Collapse view with morph effect
     */
    public void collapseWithMorph(View view, long duration) {
        if (view == null) return;
        
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int startHeight = view.getHeight();
        
        ValueAnimator animator = ValueAnimator.ofInt(startHeight, 0);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            params.height = (int) animation.getAnimatedValue();
            view.setLayoutParams(params);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }
    
    // Helper methods
    
    private int getCurrentBackgroundColor(View view) {
        if (view.getBackground() instanceof ColorDrawable) {
            return ((ColorDrawable) view.getBackground()).getColor();
        }
        return context.getResources().getColor(R.color.md_theme_light_surface, null);
    }
    
    private int getColorForWeather(String weatherCondition) {
        String condition = weatherCondition.toLowerCase();
        
        if (condition.contains("clear") || condition.contains("sunny")) {
            return context.getResources().getColor(R.color.gradient_sunny_start, null);
        } else if (condition.contains("rain") || condition.contains("drizzle")) {
            return context.getResources().getColor(R.color.gradient_rainy_start, null);
        } else if (condition.contains("cloud")) {
            return context.getResources().getColor(R.color.gradient_cloudy_start, null);
        } else if (condition.contains("snow")) {
            return context.getResources().getColor(R.color.md_theme_light_surfaceVariant, null);
        }
        
        return context.getResources().getColor(R.color.md_theme_light_surface, null);
    }
    
    private int[] getGradientColorsForWeather(String weatherCondition) {
        String condition = weatherCondition.toLowerCase();
        
        if (condition.contains("clear") || condition.contains("sunny")) {
            return new int[] {
                context.getResources().getColor(R.color.gradient_sunny_start, null),
                context.getResources().getColor(R.color.gradient_sunny_end, null)
            };
        } else if (condition.contains("rain") || condition.contains("drizzle")) {
            return new int[] {
                context.getResources().getColor(R.color.gradient_rainy_start, null),
                context.getResources().getColor(R.color.gradient_rainy_end, null)
            };
        } else if (condition.contains("cloud")) {
            return new int[] {
                context.getResources().getColor(R.color.gradient_cloudy_start, null),
                context.getResources().getColor(R.color.gradient_cloudy_end, null)
            };
        }
        
        return new int[] {
            context.getResources().getColor(R.color.md_theme_light_surface, null),
            context.getResources().getColor(R.color.md_theme_light_surfaceVariant, null)
        };
    }
}
