package com.example.weatherapp.ui.views.charts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.Nullable;

/**
 * Animated circular progress ring for weather metrics
 * Supports gradient colors and particle trail effects
 */
public class AnimatedProgressRing extends View {
    
    // Progress value (0-100)
    private float progress = 0f;
    private float targetProgress = 0f;
    private float animatedProgress = 0f;
    
    // Ring dimensions
    private float ringWidth = 30f;
    private float startAngle = -90f; // Start from top
    
    // Colors
    private int[] gradientColors = {
        Color.parseColor("#4CAF50"),
        Color.parseColor("#8BC34A"),
        Color.parseColor("#CDDC39")
    };
    private int backgroundColor = Color.parseColor("#20FFFFFF");
    private int textColor = Color.WHITE;
    
    // Paint objects
    private Paint ringPaint;
    private Paint backgroundPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint particlePaint;
    
    // Text
    private String valueText = "0";
    private String label = "";
    private String unit = "%";
    
    // Animation
    private ValueAnimator animator;
    
    // Particle trail
    private boolean showParticles = false;
    private float particleAngle = 0f;
    
    public AnimatedProgressRing(Context context) {
        super(context);
        init();
    }
    
    public AnimatedProgressRing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AnimatedProgressRing(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Ring paint with gradient
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(ringWidth);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Background ring paint
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(ringWidth);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(80f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(textColor);
        labelPaint.setTextSize(36f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setAlpha(200);
        
        // Particle paint
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        particlePaint.setColor(Color.WHITE);
        particlePaint.setStyle(Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Create gradient shader
        float centerX = w / 2f;
        float centerY = h / 2f;
        
        SweepGradient gradient = new SweepGradient(centerX, centerY, gradientColors, null);
        ringPaint.setShader(gradient);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - ringWidth / 2 - 20f;
        
        RectF rect = new RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
        
        // Rotate canvas so gradient starts from top
        canvas.save();
        canvas.rotate(-90, centerX, centerY);
        
        // Draw background ring
        canvas.drawArc(rect, 0, 360, false, backgroundPaint);
        
        // Draw progress ring with animation
        float sweepAngle = (animatedProgress / 100f) * 360f;
        canvas.drawArc(rect, 0, sweepAngle, false, ringPaint);
        
        canvas.restore();
        
        // Draw particles if enabled
        if (showParticles && animatedProgress > 0) {
            drawParticles(canvas, centerX, centerY, radius, sweepAngle);
        }
        
        // Draw value text
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        canvas.drawText(valueText + unit, centerX, centerY + textHeight / 4, textPaint);
        
        // Draw label below value
        if (!label.isEmpty()) {
            canvas.drawText(label, centerX, centerY + textHeight / 4 + 50f, labelPaint);
        }
    }
    
    private void drawParticles(Canvas canvas, float centerX, float centerY, float radius, float sweepAngle) {
        // Draw particles along the arc
        int particleCount = 5;
        float angleStep = sweepAngle / particleCount;
        
        for (int i = 0; i < particleCount; i++) {
            float angle = startAngle + (i * angleStep) + particleAngle;
            float radians = (float) Math.toRadians(angle);
            
            float x = centerX + (float) Math.cos(radians) * radius;
            float y = centerY + (float) Math.sin(radians) * radius;
            
            // Draw particle with fade effect
            float alpha = 1f - (i / (float) particleCount);
            particlePaint.setAlpha((int) (alpha * 255));
            
            float particleRadius = 8f - (i * 1.5f);
            canvas.drawCircle(x, y, particleRadius, particlePaint);
        }
        
        // Animate particle movement
        particleAngle += 5f;
        if (particleAngle >= 360f) {
            particleAngle = 0f;
        }
        
        postInvalidateOnAnimation();
    }
    
    /**
     * Set progress value with animation
     */
    public void setProgress(float progress) {
        this.targetProgress = Math.max(0, Math.min(100, progress));
        animateProgress();
    }
    
    /**
     * Set progress immediately without animation
     */
    public void setProgressImmediate(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.animatedProgress = this.progress;
        this.targetProgress = this.progress;
        this.valueText = String.format("%.0f", progress);
        invalidate();
    }
    
    private void animateProgress() {
        if (animator != null) {
            animator.cancel();
        }
        
        animator = ValueAnimator.ofFloat(animatedProgress, targetProgress);
        animator.setDuration(1000);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            valueText = String.format("%.0f", animatedProgress);
            invalidate();
        });
        animator.start();
    }
    
    /**
     * Set ring width
     */
    public void setRingWidth(float width) {
        this.ringWidth = width;
        ringPaint.setStrokeWidth(width);
        backgroundPaint.setStrokeWidth(width);
        invalidate();
    }
    
    /**
     * Set gradient colors
     */
    public void setGradientColors(int... colors) {
        this.gradientColors = colors;
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        SweepGradient gradient = new SweepGradient(centerX, centerY, colors, null);
        ringPaint.setShader(gradient);
        invalidate();
    }
    
    /**
     * Set label text
     */
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    /**
     * Set unit text
     */
    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }
    
    /**
     * Enable/disable particle trail effect
     */
    public void setShowParticles(boolean show) {
        this.showParticles = show;
        if (!show) {
            particleAngle = 0f;
        }
        invalidate();
    }
    
    /**
     * Set text color
     */
    public void setTextColor(int color) {
        this.textColor = color;
        textPaint.setColor(color);
        labelPaint.setColor(color);
        invalidate();
    }
    
    /**
     * Set background ring color
     */
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        backgroundPaint.setColor(color);
        invalidate();
    }
    
    /**
     * Get current progress
     */
    public float getProgress() {
        return progress;
    }
    
    /**
     * Clean up animations
     */
    public void cleanup() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
