package com.example.weatherapp.ui.helpers;

import android.view.View;
import androidx.core.widget.NestedScrollView;

/**
 * Custom scroll listener for parallax effects
 * Creates smooth background movement based on scroll position
 */
public class ParallaxScrollListener implements NestedScrollView.OnScrollChangeListener {
    
    private final View parallaxView;
    private final float parallaxSpeed;
    private final boolean reverseDirection;
    
    /**
     * Create parallax effect with default speed
     * @param parallaxView View to apply parallax effect to (usually background)
     */
    public ParallaxScrollListener(View parallaxView) {
        this(parallaxView, 0.5f, false);
    }
    
    /**
     * Create parallax effect with custom speed
     * @param parallaxView View to apply parallax effect
     * @param parallaxSpeed Speed multiplier (0.5 = half speed, 2.0 = double speed)
     * @param reverseDirection True to move opposite to scroll direction
     */
    public ParallaxScrollListener(View parallaxView, float parallaxSpeed, boolean reverseDirection) {
        this.parallaxView = parallaxView;
        this.parallaxSpeed = parallaxSpeed;
        this.reverseDirection = reverseDirection;
    }
    
    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        if (parallaxView != null) {
            float translationY = scrollY * parallaxSpeed;
            if (reverseDirection) {
                translationY = -translationY;
            }
            parallaxView.setTranslationY(translationY);
        }
    }
    
    /**
     * Create multi-layer parallax effect
     */
    public static class MultiLayerParallaxListener implements NestedScrollView.OnScrollChangeListener {
        
        private final ParallaxLayer[] layers;
        
        public MultiLayerParallaxListener(ParallaxLayer... layers) {
            this.layers = layers;
        }
        
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            for (ParallaxLayer layer : layers) {
                if (layer.view != null) {
                    float translationY = scrollY * layer.speed;
                    if (layer.reverseDirection) {
                        translationY = -translationY;
                    }
                    
                    layer.view.setTranslationY(translationY);
                    
                    // Optional alpha fade effect
                    if (layer.enableFade) {
                        float alpha = 1f - (Math.abs(scrollY) / 1000f);
                        layer.view.setAlpha(Math.max(0f, Math.min(1f, alpha)));
                    }
                }
            }
        }
    }
    
    /**
     * Represents a single parallax layer with its own speed and effects
     */
    public static class ParallaxLayer {
        public final View view;
        public final float speed;
        public final boolean reverseDirection;
        public final boolean enableFade;
        
        public ParallaxLayer(View view, float speed) {
            this(view, speed, false, false);
        }
        
        public ParallaxLayer(View view, float speed, boolean reverseDirection) {
            this(view, speed, reverseDirection, false);
        }
        
        public ParallaxLayer(View view, float speed, boolean reverseDirection, boolean enableFade) {
            this.view = view;
            this.speed = speed;
            this.reverseDirection = reverseDirection;
            this.enableFade = enableFade;
        }
    }
    
    /**
     * Builder pattern for creating complex parallax effects
     */
    public static class Builder {
        private View view;
        private float speed = 0.5f;
        private boolean reverseDirection = false;
        
        public Builder setView(View view) {
            this.view = view;
            return this;
        }
        
        public Builder setSpeed(float speed) {
            this.speed = speed;
            return this;
        }
        
        public Builder setReverseDirection(boolean reverse) {
            this.reverseDirection = reverse;
            return this;
        }
        
        public ParallaxScrollListener build() {
            if (view == null) {
                throw new IllegalStateException("View must be set");
            }
            return new ParallaxScrollListener(view, speed, reverseDirection);
        }
    }
}
