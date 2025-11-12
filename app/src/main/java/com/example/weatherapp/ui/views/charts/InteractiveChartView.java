package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * Base class for interactive charts with touch gestures
 * Supports zoom, pan, and tap interactions
 */
public abstract class InteractiveChartView extends View {
    
    // Touch gesture detectors
    protected ScaleGestureDetector scaleGestureDetector;
    protected GestureDetector gestureDetector;
    
    // Transformation matrix for zoom/pan
    protected Matrix transformMatrix = new Matrix();
    protected float scaleFactor = 1.0f;
    protected float focusX = 0f;
    protected float focusY = 0f;
    
    // Zoom constraints
    protected float minZoom = 1.0f;
    protected float maxZoom = 5.0f;
    
    // Chart state
    protected boolean isInteractive = true;
    protected boolean isAnimating = false;
    
    // Listeners
    protected OnChartTapListener tapListener;
    protected OnChartDataChangeListener dataChangeListener;
    
    public InteractiveChartView(Context context) {
        super(context);
        init(context);
    }
    
    public InteractiveChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public InteractiveChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
    
    private void init(Context context) {
        // Initialize scale gesture detector for pinch-to-zoom
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        
        // Initialize gesture detector for tap and scroll
        gestureDetector = new GestureDetector(context, new GestureListener());
        
        // Allow drawing
        setWillNotDraw(false);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isInteractive) {
            return super.onTouchEvent(event);
        }
        
        boolean handled = false;
        
        // Handle scale gestures
        handled = scaleGestureDetector.onTouchEvent(event);
        
        // Handle other gestures
        handled = gestureDetector.onTouchEvent(event) || handled;
        
        return handled || super.onTouchEvent(event);
    }
    
    /**
     * Scale gesture listener for pinch-to-zoom
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            
            // Constrain zoom level
            scaleFactor = Math.max(minZoom, Math.min(scaleFactor, maxZoom));
            
            // Update focus point
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            
            onZoomChanged(scaleFactor, focusX, focusY);
            invalidate();
            return true;
        }
    }
    
    /**
     * Gesture listener for tap and scroll
     */
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            
            onChartTapped(x, y);
            
            if (tapListener != null) {
                tapListener.onChartTap(x, y);
            }
            
            return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (scaleFactor > 1.0f) {
                focusX -= distanceX;
                focusY -= distanceY;
                
                onPanChanged(focusX, focusY);
                invalidate();
                return true;
            }
            return false;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            // Reset zoom on double tap
            resetZoom();
            return true;
        }
    }
    
    /**
     * Reset zoom to default
     */
    public void resetZoom() {
        scaleFactor = 1.0f;
        focusX = 0f;
        focusY = 0f;
        onZoomChanged(scaleFactor, focusX, focusY);
        invalidate();
    }
    
    /**
     * Set zoom level programmatically
     */
    public void setZoom(float zoom, float centerX, float centerY) {
        scaleFactor = Math.max(minZoom, Math.min(zoom, maxZoom));
        focusX = centerX;
        focusY = centerY;
        onZoomChanged(scaleFactor, focusX, focusY);
        invalidate();
    }
    
    /**
     * Enable or disable interactions
     */
    public void setInteractive(boolean interactive) {
        this.isInteractive = interactive;
    }
    
    /**
     * Set zoom constraints
     */
    public void setZoomLimits(float min, float max) {
        this.minZoom = min;
        this.maxZoom = max;
    }
    
    /**
     * Set tap listener
     */
    public void setOnChartTapListener(OnChartTapListener listener) {
        this.tapListener = listener;
    }
    
    /**
     * Set data change listener
     */
    public void setOnChartDataChangeListener(OnChartDataChangeListener listener) {
        this.dataChangeListener = listener;
    }
    
    /**
     * Called when zoom changes
     */
    protected void onZoomChanged(float zoom, float centerX, float centerY) {
        // Override in subclass
    }
    
    /**
     * Called when pan changes
     */
    protected void onPanChanged(float x, float y) {
        // Override in subclass
    }
    
    /**
     * Called when chart is tapped
     */
    protected void onChartTapped(float x, float y) {
        // Override in subclass
    }
    
    /**
     * Apply transformation matrix to canvas
     */
    protected void applyTransformations(Canvas canvas) {
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2f, getHeight() / 2f);
        canvas.translate(focusX / scaleFactor, focusY / scaleFactor);
    }
    
    /**
     * Restore canvas after transformations
     */
    protected void restoreCanvas(Canvas canvas) {
        canvas.restore();
    }
    
    /**
     * Interface for chart tap events
     */
    public interface OnChartTapListener {
        void onChartTap(float x, float y);
    }
    
    /**
     * Interface for chart data changes
     */
    public interface OnChartDataChangeListener {
        void onDataChanged();
    }
    
    /**
     * Create standard paint for charts
     */
    protected Paint createPaint(int color, float strokeWidth, Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }
}
