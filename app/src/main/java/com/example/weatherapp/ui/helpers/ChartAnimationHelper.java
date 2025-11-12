package com.example.weatherapp.ui.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;
import com.example.weatherapp.ui.views.charts.AnimatedProgressRing;
import com.example.weatherapp.ui.views.charts.WeatherLineChart;
import com.example.weatherapp.ui.views.charts.WindSpeedGauge;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for coordinating chart animations
 * Provides staggered animations and synchronized updates
 */
public class ChartAnimationHelper {
    
    private final List<ValueAnimator> activeAnimators = new ArrayList<>();
    
    /**
     * Animate multiple progress rings with stagger
     */
    public void animateProgressRings(long staggerDelay, AnimatedProgressRing... rings) {
        for (int i = 0; i < rings.length; i++) {
            final AnimatedProgressRing ring = rings[i];
            final int index = i;
            
            // Delay each ring animation
            View view = (View) ring;
            view.postDelayed(() -> {
                ring.setShowParticles(true);
            }, staggerDelay * index);
        }
    }
    
    /**
     * Fade in chart with scale animation
     */
    public void fadeInChartWithScale(View chartView, long duration) {
        chartView.setAlpha(0f);
        chartView.setScaleX(0.8f);
        chartView.setScaleY(0.8f);
        
        chartView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    /**
     * Slide in chart from bottom
     */
    public void slideInChartFromBottom(View chartView, long duration) {
        chartView.setTranslationY(chartView.getHeight());
        chartView.setAlpha(0f);
        
        chartView.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
    
    /**
     * Slide in chart from left
     */
    public void slideInChartFromLeft(View chartView, long duration) {
        chartView.setTranslationX(-chartView.getWidth());
        chartView.setAlpha(0f);
        
        chartView.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
    
    /**
     * Slide in chart from right
     */
    public void slideInChartFromRight(View chartView, long duration) {
        chartView.setTranslationX(chartView.getWidth());
        chartView.setAlpha(0f);
        
        chartView.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }
    
    /**
     * Bounce chart entrance animation
     */
    public void bounceInChart(View chartView, long duration) {
        chartView.setScaleX(0f);
        chartView.setScaleY(0f);
        chartView.setAlpha(0f);
        
        chartView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .start();
    }
    
    /**
     * Staggered fade in for multiple charts
     */
    public void staggeredFadeIn(long staggerDelay, View... charts) {
        for (int i = 0; i < charts.length; i++) {
            final View chart = charts[i];
            final long delay = staggerDelay * i;
            
            chart.setAlpha(0f);
            chart.setTranslationY(50f);
            
            chart.postDelayed(() -> {
                chart.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }, delay);
        }
    }
    
    /**
     * Update progress ring with smooth animation
     */
    public void updateProgressRing(AnimatedProgressRing ring, float progress, boolean showParticles) {
        ring.setProgress(progress);
        ring.setShowParticles(showParticles);
    }
    
    /**
     * Update wind gauge with smooth animation
     */
    public void updateWindGauge(WindSpeedGauge gauge, float speed) {
        gauge.setSpeed(speed);
    }
    
    /**
     * Update line chart data with animation
     */
    public void updateLineChart(WeatherLineChart chart, List<WeatherLineChart.ChartDataPoint> data) {
        chart.setData(data);
    }
    
    /**
     * Cross-fade between two charts
     */
    public void crossFadeCharts(View outChart, View inChart, long duration) {
        outChart.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> outChart.setVisibility(View.GONE))
                .start();
        
        inChart.setAlpha(0f);
        inChart.setVisibility(View.VISIBLE);
        inChart.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }
    
    /**
     * Pulse animation for highlighting
     */
    public void pulseChart(View chartView, int repeatCount) {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1f, 1.1f, 1f);
        scaleAnimator.setDuration(600);
        scaleAnimator.setRepeatCount(repeatCount);
        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            chartView.setScaleX(scale);
            chartView.setScaleY(scale);
        });
        
        activeAnimators.add(scaleAnimator);
        scaleAnimator.start();
    }
    
    /**
     * Shake animation for error state
     */
    public void shakeChart(View chartView) {
        float[] values = {0f, -25f, 25f, -25f, 25f, -15f, 15f, -6f, 6f, 0f};
        ValueAnimator shakeAnimator = ValueAnimator.ofFloat(values);
        shakeAnimator.setDuration(500);
        shakeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            chartView.setTranslationX(value);
        });
        
        activeAnimators.add(shakeAnimator);
        shakeAnimator.start();
    }
    
    /**
     * Flip chart animation (3D rotation)
     */
    public void flipChart(View chartView, long duration) {
        chartView.animate()
                .rotationY(180f)
                .setDuration(duration / 2)
                .withEndAction(() -> {
                    chartView.setRotationY(0f);
                    chartView.animate()
                            .rotationY(360f)
                            .setDuration(duration / 2)
                            .start();
                })
                .start();
    }
    
    /**
     * Zoom in chart animation
     */
    public void zoomInChart(View chartView, long duration) {
        chartView.setPivotX(chartView.getWidth() / 2f);
        chartView.setPivotY(chartView.getHeight() / 2f);
        chartView.setScaleX(0f);
        chartView.setScaleY(0f);
        
        chartView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(duration)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
    
    /**
     * Zoom out chart animation
     */
    public void zoomOutChart(View chartView, long duration, Runnable onComplete) {
        chartView.setPivotX(chartView.getWidth() / 2f);
        chartView.setPivotY(chartView.getHeight() / 2f);
        
        chartView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    chartView.setVisibility(View.GONE);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }
    
    /**
     * Sequential animation chain
     */
    public void animateSequentially(long delay, Runnable... animations) {
        for (int i = 0; i < animations.length; i++) {
            final Runnable animation = animations[i];
            final long totalDelay = delay * i;
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(animation, totalDelay);
        }
    }
    
    /**
     * Cancel all active animations
     */
    public void cancelAllAnimations() {
        for (ValueAnimator animator : activeAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimators.clear();
    }
    
    /**
     * Cleanup method
     */
    public void cleanup() {
        cancelAllAnimations();
    }
}
