package com.example.weatherapp.ui.views.charts;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive line chart for weather data with gradient fills
 * Supports animations, zoom, and touch interactions
 */
public class WeatherLineChart extends InteractiveChartView {
    
    // Chart data
    private List<ChartDataPoint> dataPoints = new ArrayList<>();
    private float maxValue = 100f;
    private float minValue = 0f;
    
    // Paint objects
    private Paint linePaint;
    private Paint gradientPaint;
    private Paint pointPaint;
    private Paint gridPaint;
    private Paint labelPaint;
    private Paint highlightPaint;
    
    // Chart styling
    private int lineColor = Color.parseColor("#2196F3");
    private int gradientStartColor = Color.parseColor("#662196F3");
    private int gradientEndColor = Color.parseColor("#002196F3");
    private float lineWidth = 6f;
    private float pointRadius = 8f;
    
    // Chart dimensions
    private float chartLeft = 60f;
    private float chartRight;
    private float chartTop = 40f;
    private float chartBottom;
    
    // Animation
    private float animationProgress = 0f;
    private ValueAnimator animator;
    
    // Highlight
    private int highlightedIndex = -1;
    
    // Grid
    private boolean showGrid = true;
    private int gridLines = 5;
    
    public WeatherLineChart(Context context) {
        super(context);
        init();
    }
    
    public WeatherLineChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public WeatherLineChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Line paint
        linePaint = createPaint(lineColor, lineWidth, Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        // Gradient fill paint
        gradientPaint = createPaint(gradientStartColor, 0, Paint.Style.FILL);
        
        // Point paint
        pointPaint = createPaint(lineColor, 0, Paint.Style.FILL);
        
        // Grid paint
        gridPaint = createPaint(Color.parseColor("#30FFFFFF"), 2f, Paint.Style.STROKE);
        
        // Label paint
        labelPaint = createPaint(Color.WHITE, 0, Paint.Style.FILL);
        labelPaint.setTextSize(32f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        // Highlight paint
        highlightPaint = createPaint(Color.parseColor("#80FFFFFF"), 0, Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chartRight = w - 40f;
        chartBottom = h - 60f;
        
        // Update gradient
        updateGradient();
    }
    
    private void updateGradient() {
        if (chartBottom > chartTop) {
            LinearGradient gradient = new LinearGradient(
                0, chartTop,
                0, chartBottom,
                gradientStartColor,
                gradientEndColor,
                Shader.TileMode.CLAMP
            );
            gradientPaint.setShader(gradient);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dataPoints.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }
        
        applyTransformations(canvas);
        
        // Draw grid
        if (showGrid) {
            drawGrid(canvas);
        }
        
        // Draw chart
        drawGradientFill(canvas);
        drawLine(canvas);
        drawPoints(canvas);
        
        // Draw highlighted point
        if (highlightedIndex >= 0 && highlightedIndex < dataPoints.size()) {
            drawHighlight(canvas, highlightedIndex);
        }
        
        restoreCanvas(canvas);
    }
    
    private void drawGrid(Canvas canvas) {
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;
        
        // Horizontal grid lines
        for (int i = 0; i <= gridLines; i++) {
            float y = chartTop + (chartHeight * i / gridLines);
            canvas.drawLine(chartLeft, y, chartRight, y, gridPaint);
        }
        
        // Vertical grid lines (one per data point)
        if (dataPoints.size() > 1) {
            float spacing = chartWidth / (dataPoints.size() - 1);
            for (int i = 0; i < dataPoints.size(); i++) {
                float x = chartLeft + (i * spacing);
                canvas.drawLine(x, chartTop, x, chartBottom, gridPaint);
            }
        }
    }
    
    private void drawGradientFill(Canvas canvas) {
        if (dataPoints.size() < 2) return;
        
        Path path = new Path();
        List<PointF> points = calculatePoints();
        
        // Move to first point
        path.moveTo(chartLeft, chartBottom);
        path.lineTo(points.get(0).x, points.get(0).y);
        
        // Draw through all points with animation
        for (int i = 1; i < points.size() && i <= animationProgress * points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        
        // Close path to bottom
        int lastIndex = Math.min(points.size() - 1, (int)(animationProgress * points.size()));
        path.lineTo(points.get(lastIndex).x, chartBottom);
        path.close();
        
        canvas.drawPath(path, gradientPaint);
    }
    
    private void drawLine(Canvas canvas) {
        if (dataPoints.size() < 2) return;
        
        List<PointF> points = calculatePoints();
        Path path = new Path();
        
        path.moveTo(points.get(0).x, points.get(0).y);
        
        // Draw through all points with animation
        for (int i = 1; i < points.size() && i <= animationProgress * points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        
        canvas.drawPath(path, linePaint);
    }
    
    private void drawPoints(Canvas canvas) {
        List<PointF> points = calculatePoints();
        
        int visiblePoints = (int)(animationProgress * points.size());
        for (int i = 0; i < visiblePoints; i++) {
            PointF point = points.get(i);
            
            // Draw point
            canvas.drawCircle(point.x, point.y, pointRadius, pointPaint);
            
            // Draw white center
            canvas.drawCircle(point.x, point.y, pointRadius / 2, 
                createPaint(Color.WHITE, 0, Paint.Style.FILL));
        }
    }
    
    private void drawHighlight(Canvas canvas, int index) {
        List<PointF> points = calculatePoints();
        if (index >= points.size()) return;
        
        PointF point = points.get(index);
        ChartDataPoint data = dataPoints.get(index);
        
        // Draw highlight circle
        canvas.drawCircle(point.x, point.y, pointRadius * 2, highlightPaint);
        canvas.drawCircle(point.x, point.y, pointRadius * 1.5f, pointPaint);
        
        // Draw value label
        String valueText = String.format("%.1f%s", data.value, data.unit);
        canvas.drawText(valueText, point.x, point.y - pointRadius * 3, labelPaint);
        
        // Draw x label
        if (data.label != null) {
            canvas.drawText(data.label, point.x, chartBottom + 40f, labelPaint);
        }
    }
    
    private void drawEmptyState(Canvas canvas) {
        Paint emptyPaint = createPaint(Color.parseColor("#80FFFFFF"), 0, Paint.Style.FILL);
        emptyPaint.setTextSize(48f);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
        
        canvas.drawText("No Data", getWidth() / 2f, getHeight() / 2f, emptyPaint);
    }
    
    private List<PointF> calculatePoints() {
        List<PointF> points = new ArrayList<>();
        
        if (dataPoints.isEmpty()) return points;
        
        float chartWidth = chartRight - chartLeft;
        float chartHeight = chartBottom - chartTop;
        float valueRange = maxValue - minValue;
        
        if (valueRange == 0) valueRange = 1; // Avoid division by zero
        
        float spacing = dataPoints.size() > 1 ? chartWidth / (dataPoints.size() - 1) : 0;
        
        for (int i = 0; i < dataPoints.size(); i++) {
            ChartDataPoint data = dataPoints.get(i);
            
            float x = chartLeft + (i * spacing);
            float normalizedValue = (data.value - minValue) / valueRange;
            float y = chartBottom - (normalizedValue * chartHeight);
            
            points.add(new PointF(x, y));
        }
        
        return points;
    }
    
    @Override
    protected void onChartTapped(float x, float y) {
        // Find nearest point
        List<PointF> points = calculatePoints();
        float minDistance = Float.MAX_VALUE;
        int nearestIndex = -1;
        
        for (int i = 0; i < points.size(); i++) {
            PointF point = points.get(i);
            float distance = (float) Math.sqrt(
                Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2)
            );
            
            if (distance < minDistance && distance < pointRadius * 3) {
                minDistance = distance;
                nearestIndex = i;
            }
        }
        
        highlightedIndex = nearestIndex;
        invalidate();
    }
    
    /**
     * Set chart data with animation
     */
    public void setData(List<ChartDataPoint> data) {
        this.dataPoints = new ArrayList<>(data);
        calculateMinMax();
        updateGradient();
        animateChart();
    }
    
    /**
     * Add single data point
     */
    public void addDataPoint(ChartDataPoint point) {
        dataPoints.add(point);
        calculateMinMax();
        invalidate();
    }
    
    /**
     * Clear all data
     */
    public void clearData() {
        dataPoints.clear();
        highlightedIndex = -1;
        invalidate();
    }
    
    private void calculateMinMax() {
        if (dataPoints.isEmpty()) {
            minValue = 0;
            maxValue = 100;
            return;
        }
        
        minValue = Float.MAX_VALUE;
        maxValue = Float.MIN_VALUE;
        
        for (ChartDataPoint point : dataPoints) {
            minValue = Math.min(minValue, point.value);
            maxValue = Math.max(maxValue, point.value);
        }
        
        // Add padding
        float padding = (maxValue - minValue) * 0.1f;
        minValue -= padding;
        maxValue += padding;
    }
    
    private void animateChart() {
        if (animator != null) {
            animator.cancel();
        }
        
        animationProgress = 0f;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }
    
    /**
     * Set chart colors
     */
    public void setLineColor(int color) {
        this.lineColor = color;
        linePaint.setColor(color);
        pointPaint.setColor(color);
        invalidate();
    }
    
    public void setGradientColors(int startColor, int endColor) {
        this.gradientStartColor = startColor;
        this.gradientEndColor = endColor;
        updateGradient();
        invalidate();
    }
    
    /**
     * Set grid visibility
     */
    public void setShowGrid(boolean show) {
        this.showGrid = show;
        invalidate();
    }
    
    /**
     * Data point class
     */
    public static class ChartDataPoint {
        public float value;
        public String label;
        public String unit;
        
        public ChartDataPoint(float value, String label, String unit) {
            this.value = value;
            this.label = label;
            this.unit = unit;
        }
    }
}
