package com.example.weatherapp.ui.views.charts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.Nullable;

/**
 * Animated gauge for wind speed visualization
 * Features animated needle, colored zones, and smooth transitions
 */
public class WindSpeedGauge extends View {
    
    // Speed value
    private float speed = 0f;
    private float targetSpeed = 0f;
    private float animatedSpeed = 0f;
    
    // Gauge configuration
    private float maxSpeed = 100f;
    private float startAngle = 135f;  // Start from bottom-left
    private float sweepAngle = 270f;  // 3/4 circle
    
    // Dimensions
    private float gaugeWidth = 25f;
    private float needleLength = 0f;
    private float centerDotRadius = 15f;
    
    // Colors for speed zones
    private static final int[] ZONE_COLORS = {
        Color.parseColor("#4CAF50"),  // Green: 0-20 (Light breeze)
        Color.parseColor("#8BC34A"),  // Light green: 20-40 (Gentle breeze)
        Color.parseColor("#FFC107"),  // Yellow: 40-60 (Moderate wind)
        Color.parseColor("#FF9800"),  // Orange: 60-80 (Fresh wind)
        Color.parseColor("#F44336")   // Red: 80-100 (Strong wind)
    };
    
    // Paint objects
    private Paint gaugePaint;
    private Paint needlePaint;
    private Paint centerPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint tickPaint;
    
    // Text
    private String unit = "km/h";
    private String label = "Wind Speed";
    
    // Animation
    private ValueAnimator animator;
    
    public WindSpeedGauge(Context context) {
        super(context);
        init();
    }
    
    public WindSpeedGauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WindSpeedGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Gauge arc paint
        gaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gaugePaint.setStyle(Paint.Style.STROKE);
        gaugePaint.setStrokeWidth(gaugeWidth);
        gaugePaint.setStrokeCap(Paint.Cap.ROUND);
        
        // Needle paint
        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(Color.WHITE);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setShadowLayer(8f, 0f, 4f, Color.parseColor("#40000000"));
        
        // Center dot paint
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setColor(Color.WHITE);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setShadowLayer(4f, 0f, 2f, Color.parseColor("#40000000"));
        
        // Text paint
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        // Label paint
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(32f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setAlpha(200);
        
        // Tick marks paint
        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeWidth(3f);
        tickPaint.setAlpha(150);
        
        setLayerType(LAYER_TYPE_SOFTWARE, needlePaint);
        setLayerType(LAYER_TYPE_SOFTWARE, centerPaint);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float radius = Math.min(w, h) / 2f - gaugeWidth / 2 - 40f;
        needleLength = radius * 0.7f;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - gaugeWidth / 2 - 40f;
        
        RectF rect = new RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
        
        // Draw gauge zones with different colors
        drawGaugeZones(canvas, rect);
        
        // Draw tick marks
        drawTickMarks(canvas, centerX, centerY, radius);
        
        // Draw needle
        drawNeedle(canvas, centerX, centerY);
        
        // Draw center dot
        canvas.drawCircle(centerX, centerY, centerDotRadius, centerPaint);
        
        // Draw speed value
        String valueText = String.format("%.1f", animatedSpeed);
        canvas.drawText(valueText, centerX, centerY + radius + 60f, textPaint);
        
        // Draw unit
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        canvas.drawText(unit, centerX + textPaint.measureText(valueText) / 2 + 30f, 
            centerY + radius + 60f, labelPaint);
        
        // Draw label
        canvas.drawText(label, centerX, centerY + radius + 110f, labelPaint);
    }
    
    private void drawGaugeZones(Canvas canvas, RectF rect) {
        int zoneCount = ZONE_COLORS.length;
        float anglePerZone = sweepAngle / zoneCount;
        
        for (int i = 0; i < zoneCount; i++) {
            gaugePaint.setColor(ZONE_COLORS[i]);
            canvas.drawArc(rect, startAngle + (i * anglePerZone), anglePerZone, false, gaugePaint);
        }
    }
    
    private void drawTickMarks(Canvas canvas, float centerX, float centerY, float radius) {
        int tickCount = 11; // 0, 10, 20, ... 100
        
        for (int i = 0; i < tickCount; i++) {
            float angle = startAngle + (sweepAngle * i / (tickCount - 1));
            float radians = (float) Math.toRadians(angle);
            
            // Major tick
            float startRadius = radius - gaugeWidth / 2 - 5f;
            float endRadius = radius - gaugeWidth / 2 - 20f;
            
            if (i % 2 == 0) {
                // Draw tick
                float x1 = centerX + (float) Math.cos(radians) * startRadius;
                float y1 = centerY + (float) Math.sin(radians) * startRadius;
                float x2 = centerX + (float) Math.cos(radians) * endRadius;
                float y2 = centerY + (float) Math.sin(radians) * endRadius;
                
                canvas.drawLine(x1, y1, x2, y2, tickPaint);
                
                // Draw value label
                float labelRadius = radius - gaugeWidth / 2 - 45f;
                float labelX = centerX + (float) Math.cos(radians) * labelRadius;
                float labelY = centerY + (float) Math.sin(radians) * labelRadius;
                
                Paint valuePaint = new Paint(labelPaint);
                valuePaint.setTextSize(24f);
                valuePaint.setTextAlign(Paint.Align.CENTER);
                
                int value = (int) (maxSpeed * i / (tickCount - 1));
                canvas.drawText(String.valueOf(value), labelX, labelY + 8f, valuePaint);
            }
        }
    }
    
    private void drawNeedle(Canvas canvas, float centerX, float centerY) {
        // Calculate needle angle based on speed
        float speedPercent = animatedSpeed / maxSpeed;
        float needleAngle = startAngle + (sweepAngle * speedPercent);
        float radians = (float) Math.toRadians(needleAngle);
        
        // Needle tip position
        float tipX = centerX + (float) Math.cos(radians) * needleLength;
        float tipY = centerY + (float) Math.sin(radians) * needleLength;
        
        // Create needle path (triangle shape)
        Path needlePath = new Path();
        
        // Calculate perpendicular points for needle base
        float baseWidth = 12f;
        float perpAngle = needleAngle + 90f;
        float perpRadians = (float) Math.toRadians(perpAngle);
        
        float base1X = centerX + (float) Math.cos(perpRadians) * baseWidth;
        float base1Y = centerY + (float) Math.sin(perpRadians) * baseWidth;
        float base2X = centerX - (float) Math.cos(perpRadians) * baseWidth;
        float base2Y = centerY - (float) Math.sin(perpRadians) * baseWidth;
        
        // Draw needle
        needlePath.moveTo(tipX, tipY);
        needlePath.lineTo(base1X, base1Y);
        needlePath.lineTo(base2X, base2Y);
        needlePath.close();
        
        canvas.drawPath(needlePath, needlePaint);
        
        // Draw small circle at needle tip
        canvas.drawCircle(tipX, tipY, 6f, needlePaint);
    }
    
    /**
     * Set wind speed with animation
     */
    public void setSpeed(float speed) {
        this.targetSpeed = Math.max(0, Math.min(maxSpeed, speed));
        animateSpeed();
    }
    
    /**
     * Set speed immediately without animation
     */
    public void setSpeedImmediate(float speed) {
        this.speed = Math.max(0, Math.min(maxSpeed, speed));
        this.animatedSpeed = this.speed;
        this.targetSpeed = this.speed;
        invalidate();
    }
    
    private void animateSpeed() {
        if (animator != null) {
            animator.cancel();
        }
        
        animator = ValueAnimator.ofFloat(animatedSpeed, targetSpeed);
        animator.setDuration(1500);
        animator.setInterpolator(new OvershootInterpolator(0.5f));
        animator.addUpdateListener(animation -> {
            animatedSpeed = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
    
    /**
     * Set maximum speed
     */
    public void setMaxSpeed(float max) {
        this.maxSpeed = max;
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
     * Set label text
     */
    public void setLabel(String label) {
        this.label = label;
        invalidate();
    }
    
    /**
     * Get current speed
     */
    public float getSpeed() {
        return speed;
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
