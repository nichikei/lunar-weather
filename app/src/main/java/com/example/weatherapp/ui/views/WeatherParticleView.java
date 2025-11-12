package com.example.weatherapp.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Particle Effects View
 * Rain, Snow, Clouds animations
 */
public class WeatherParticleView extends View {

    private List<Particle> particles = new ArrayList<>();
    private Paint paint;
    private Random random = new Random();
    private ValueAnimator animator;
    private ParticleType particleType = ParticleType.NONE;
    
    private static final int RAIN_COUNT = 150;
    private static final int SNOW_COUNT = 100;
    private static final int CLOUD_COUNT = 5;

    public enum ParticleType {
        NONE,
        RAIN,
        SNOW,
        CLOUDS
    }

    public WeatherParticleView(Context context) {
        super(context);
        init();
    }

    public WeatherParticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
    }

    public void setParticleType(ParticleType type) {
        if (this.particleType == type) return;
        
        this.particleType = type;
        particles.clear();

        if (animator != null) {
            animator.cancel();
        }

        switch (type) {
            case RAIN:
                initRain();
                break;
            case SNOW:
                initSnow();
                break;
            case CLOUDS:
                initClouds();
                break;
            case NONE:
                stopAnimation();
                return;
        }

        startAnimation();
    }

    private void initRain() {
        for (int i = 0; i < RAIN_COUNT; i++) {
            particles.add(new RainDrop(
                    random.nextInt(getWidth() > 0 ? getWidth() : 1080),
                    random.nextInt(getHeight() > 0 ? getHeight() : 1920) - getHeight(),
                    random.nextFloat() * 3 + 15, // Speed
                    random.nextInt(50) + 30 // Length
            ));
        }
    }

    private void initSnow() {
        for (int i = 0; i < SNOW_COUNT; i++) {
            particles.add(new SnowFlake(
                    random.nextInt(getWidth() > 0 ? getWidth() : 1080),
                    random.nextInt(getHeight() > 0 ? getHeight() : 1920) - getHeight(),
                    random.nextFloat() * 2 + 1, // Speed
                    random.nextInt(5) + 3 // Size
            ));
        }
    }

    private void initClouds() {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            particles.add(new Cloud(
                    random.nextInt(getWidth() > 0 ? getWidth() : 1080),
                    random.nextInt(getHeight() / 2),
                    random.nextFloat() * 0.5f + 0.2f, // Speed
                    random.nextInt(100) + 150 // Size
            ));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (Particle particle : particles) {
            particle.draw(canvas, paint);
        }
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(Long.MAX_VALUE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });
        animator.start();
    }

    private void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        invalidate();
    }

    private void updateParticles() {
        int width = getWidth();
        int height = getHeight();

        for (Particle particle : particles) {
            particle.update(width, height);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (particleType != ParticleType.NONE) {
            setParticleType(particleType); // Reinit with new size
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    // Particle classes
    private abstract static class Particle {
        float x, y;
        float speed;
        int size;

        Particle(float x, float y, float speed, int size) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.size = size;
        }

        abstract void draw(Canvas canvas, Paint paint);
        abstract void update(int width, int height);
    }

    private class RainDrop extends Particle {
        RainDrop(float x, float y, float speed, int size) {
            super(x, y, speed, size);
        }

        @Override
        void draw(Canvas canvas, Paint paint) {
            paint.setColor(0x88FFFFFF); // Semi-transparent white
            paint.setStrokeWidth(2);
            canvas.drawLine(x, y, x - 5, y + size, paint);
        }

        @Override
        void update(int width, int height) {
            y += speed;
            x -= speed * 0.3f; // Diagonal movement

            if (y > height) {
                y = -size;
                x = random.nextInt(width);
            }
        }
    }

    private class SnowFlake extends Particle {
        float drift = 0;
        float driftSpeed;

        SnowFlake(float x, float y, float speed, int size) {
            super(x, y, speed, size);
            driftSpeed = random.nextFloat() * 0.5f - 0.25f;
        }

        @Override
        void draw(Canvas canvas, Paint paint) {
            paint.setColor(0xFFFFFFFF); // White
            canvas.drawCircle(x, y, size, paint);
        }

        @Override
        void update(int width, int height) {
            y += speed;
            drift += driftSpeed;
            x += Math.sin(drift) * 0.5f;

            if (y > height) {
                y = -size;
                x = random.nextInt(width);
                drift = 0;
            }
        }
    }

    private class Cloud extends Particle {
        Cloud(float x, float y, float speed, int size) {
            super(x, y, speed, size);
        }

        @Override
        void draw(Canvas canvas, Paint paint) {
            paint.setColor(0x44FFFFFF); // Very transparent white
            
            // Draw cloud shape (multiple circles)
            canvas.drawCircle(x, y, size * 0.6f, paint);
            canvas.drawCircle(x + size * 0.5f, y, size * 0.5f, paint);
            canvas.drawCircle(x - size * 0.5f, y, size * 0.5f, paint);
            canvas.drawCircle(x, y - size * 0.3f, size * 0.4f, paint);
        }

        @Override
        void update(int width, int height) {
            x += speed;

            if (x > width + size) {
                x = -size;
                y = random.nextInt(height / 2);
            }
        }
    }
}
