package com.example.weatherapp.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Enhanced Weather Effects View
 * Thunder flashes, sun rays, stars, fog, rainbow, sunrise/sunset glow
 */
public class EnhancedWeatherEffectsView extends View {

    private Paint paint;
    private Random random = new Random();
    private ValueAnimator mainAnimator;
    
    // Effect states
    private boolean thunderEnabled = false;
    private boolean sunRaysEnabled = false;
    private boolean starsEnabled = false;
    private boolean fogEnabled = false;
    private boolean rainbowEnabled = false;
    private boolean sunriseGlowEnabled = false;
    
    // Animation values
    private float animationTime = 0f;
    private float thunderFlashAlpha = 0f;
    private List<Star> stars = new ArrayList<>();
    private List<SunRay> sunRays = new ArrayList<>();
    private float rainbowAlpha = 0f;
    private float sunriseGlowAlpha = 0f;
    
    // Constants
    private static final int STAR_COUNT = 80;
    private static final int SUN_RAY_COUNT = 12;

    public EnhancedWeatherEffectsView(Context context) {
        super(context);
        init();
    }

    public EnhancedWeatherEffectsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, null); // For blur effects
        
        // Initialize stars
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star());
        }
        
        // Initialize sun rays
        for (int i = 0; i < SUN_RAY_COUNT; i++) {
            sunRays.add(new SunRay(i));
        }
        
        startAnimation();
    }

    public void setThunderEffect(boolean enabled) {
        this.thunderEnabled = enabled;
        if (enabled) {
            triggerThunderFlash();
        }
    }

    public void setSunRaysEffect(boolean enabled) {
        this.sunRaysEnabled = enabled;
    }

    public void setStarsEffect(boolean enabled) {
        this.starsEnabled = enabled;
    }

    public void setFogEffect(boolean enabled) {
        this.fogEnabled = enabled;
    }

    public void setRainbowEffect(boolean enabled) {
        this.rainbowEnabled = enabled;
        if (enabled) {
            animateRainbow();
        }
    }

    public void setSunriseGlowEffect(boolean enabled) {
        this.sunriseGlowEnabled = enabled;
        if (enabled) {
            animateSunriseGlow();
        }
    }

    private void startAnimation() {
        mainAnimator = ValueAnimator.ofFloat(0f, 1000f);
        mainAnimator.setDuration(Long.MAX_VALUE);
        mainAnimator.setInterpolator(new LinearInterpolator());
        mainAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mainAnimator.addUpdateListener(animation -> {
            animationTime = (float) animation.getAnimatedValue();
            
            // Update star twinkle
            if (starsEnabled) {
                for (Star star : stars) {
                    star.update(animationTime);
                }
            }
            
            // Random thunder flash
            if (thunderEnabled && random.nextFloat() < 0.002f) {
                triggerThunderFlash();
            }
            
            invalidate();
        });
        mainAnimator.start();
    }

    private void triggerThunderFlash() {
        ValueAnimator flashAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        flashAnimator.setDuration(150);
        flashAnimator.addUpdateListener(animation -> {
            thunderFlashAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        flashAnimator.start();
    }

    private void animateRainbow() {
        ValueAnimator rainbowAnimator = ValueAnimator.ofFloat(0f, 0.7f);
        rainbowAnimator.setDuration(2000);
        rainbowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rainbowAnimator.addUpdateListener(animation -> {
            rainbowAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        rainbowAnimator.start();
    }

    private void animateSunriseGlow() {
        ValueAnimator glowAnimator = ValueAnimator.ofFloat(0f, 0.6f, 0.4f);
        glowAnimator.setDuration(3000);
        glowAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowAnimator.addUpdateListener(animation -> {
            sunriseGlowAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        glowAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Reinitialize stars with new positions
        if (w > 0 && h > 0) {
            for (Star star : stars) {
                star.init(w, h);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) return;
        
        // 1. Sunrise/Sunset Glow (bottom of screen)
        if (sunriseGlowEnabled && sunriseGlowAlpha > 0) {
            drawSunriseGlow(canvas, width, height);
        }
        
        // 2. Sun Rays
        if (sunRaysEnabled) {
            drawSunRays(canvas, width, height);
        }
        
        // 3. Stars (for night)
        if (starsEnabled) {
            drawStars(canvas);
        }
        
        // 4. Rainbow
        if (rainbowEnabled && rainbowAlpha > 0) {
            drawRainbow(canvas, width, height);
        }
        
        // 5. Fog Effect
        if (fogEnabled) {
            drawFog(canvas, width, height);
        }
        
        // 6. Thunder Flash (full screen white flash)
        if (thunderEnabled && thunderFlashAlpha > 0) {
            drawThunderFlash(canvas, width, height);
        }
    }

    private void drawSunriseGlow(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        
        // Radial gradient from bottom center
        int centerX = width / 2;
        int centerY = height;
        float radius = height * 0.8f;
        
        int[] colors = {
            Color.argb((int)(sunriseGlowAlpha * 255), 255, 100, 50),
            Color.argb((int)(sunriseGlowAlpha * 180), 255, 150, 100),
            Color.argb(0, 255, 200, 150)
        };
        float[] positions = {0f, 0.5f, 1f};
        
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            colors, positions, Shader.TileMode.CLAMP
        );
        
        paint.setShader(gradient);
        canvas.drawRect(0, height * 0.3f, width, height, paint);
        paint.setShader(null);
    }

    private void drawSunRays(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(30);
        
        int centerX = width / 2;
        int centerY = (int)(height * 0.2f); // Sun position at top
        
        for (SunRay ray : sunRays) {
            Path path = new Path();
            path.moveTo(centerX, centerY);
            
            float angle = ray.getAngle(animationTime);
            float length = height * 1.5f;
            float width1 = 30f;
            float width2 = 100f;
            
            // Left edge of ray
            float x1 = centerX + (float)Math.cos(angle - 0.1f) * length;
            float y1 = centerY + (float)Math.sin(angle - 0.1f) * length;
            
            // Right edge of ray
            float x2 = centerX + (float)Math.cos(angle + 0.1f) * length;
            float y2 = centerY + (float)Math.sin(angle + 0.1f) * length;
            
            path.lineTo(x1, y1);
            path.lineTo(x2, y2);
            path.close();
            
            paint.setColor(Color.argb(40, 255, 255, 200));
            canvas.drawPath(path, paint);
        }
        
        paint.setAlpha(255);
    }

    private void drawStars(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        
        for (Star star : stars) {
            paint.setColor(Color.argb(
                (int)(star.alpha * 255),
                255, 255, 255
            ));
            canvas.drawCircle(star.x, star.y, star.size, paint);
            
            // Twinkle effect - small cross
            if (star.alpha > 0.7f) {
                paint.setStrokeWidth(1f);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(star.x - star.size * 2, star.y, 
                              star.x + star.size * 2, star.y, paint);
                canvas.drawLine(star.x, star.y - star.size * 2, 
                              star.x, star.y + star.size * 2, paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    private void drawRainbow(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(15f);
        
        int centerX = width / 2;
        int centerY = (int)(height * 0.4f);
        float baseRadius = width * 0.4f;
        
        int[] rainbowColors = {
            Color.RED, Color.rgb(255, 165, 0), Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.rgb(75, 0, 130), Color.rgb(238, 130, 238)
        };
        
        for (int i = 0; i < rainbowColors.length; i++) {
            paint.setColor(rainbowColors[i]);
            paint.setAlpha((int)(rainbowAlpha * 255));
            
            float radius = baseRadius + (i * 20);
            RectF oval = new RectF(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius
            );
            canvas.drawArc(oval, 180, 180, false, paint);
        }
    }

    private void drawFog(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        
        // Multiple fog layers with blur
        paint.setMaskFilter(new BlurMaskFilter(50f, BlurMaskFilter.Blur.NORMAL));
        
        for (int i = 0; i < 3; i++) {
            float offset = (animationTime * 0.1f + i * 100) % width;
            int alpha = 40 + i * 10;
            
            paint.setColor(Color.argb(alpha, 220, 220, 220));
            
            // Wavy fog shape
            Path fogPath = new Path();
            fogPath.moveTo(-100 + offset, height * 0.6f + i * 50);
            
            for (int x = -100; x < width + 100; x += 50) {
                float y = height * 0.6f + i * 50 + 
                         (float)Math.sin((x + animationTime * 0.5f) * 0.01f) * 30;
                fogPath.lineTo(x + offset, y);
            }
            
            fogPath.lineTo(width + 100 + offset, height);
            fogPath.lineTo(-100 + offset, height);
            fogPath.close();
            
            canvas.drawPath(fogPath, paint);
        }
        
        paint.setMaskFilter(null);
    }

    private void drawThunderFlash(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(
            (int)(thunderFlashAlpha * 255),
            255, 255, 255
        ));
        canvas.drawRect(0, 0, width, height, paint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mainAnimator != null) {
            mainAnimator.cancel();
        }
    }

    // Star class
    private class Star {
        float x, y, size, alpha;
        float twinkleSpeed;
        float twinkleOffset;
        
        Star() {
            twinkleSpeed = 0.5f + random.nextFloat() * 1.5f;
            twinkleOffset = random.nextFloat() * 1000f;
        }
        
        void init(int width, int height) {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height * 0.6f; // Top 60% of screen
            size = 1f + random.nextFloat() * 2f;
        }
        
        void update(float time) {
            // Twinkle effect
            alpha = 0.3f + 0.7f * (float)Math.abs(
                Math.sin((time * twinkleSpeed + twinkleOffset) * 0.01f)
            );
        }
    }

    // Sun Ray class
    private class SunRay {
        float baseAngle;
        
        SunRay(int index) {
            baseAngle = (float)(index * (Math.PI * 2 / SUN_RAY_COUNT));
        }
        
        float getAngle(float time) {
            return baseAngle + (time * 0.0005f); // Slow rotation
        }
    }
}
