package com.example.weatherapp.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
 * Ultra Realistic 3D Weather Effects View
 * Advanced particle system, depth layers, bokeh, light leaks
 */
public class Ultra3DWeatherView extends View {

    private Paint paint;
    private Random random = new Random();
    private ValueAnimator mainAnimator;
    
    // Layer system for depth
    private List<ParticleLayer> particleLayers = new ArrayList<>();
    private List<Bokeh> bokehList = new ArrayList<>();
    private List<LightLeak> lightLeaks = new ArrayList<>();
    private List<Sparkle> sparkles = new ArrayList<>();
    private List<ShootingStar> shootingStars = new ArrayList<>();
    private List<Firefly> fireflies = new ArrayList<>();
    private List<FallingLeaf> fallingLeaves = new ArrayList<>();
    private List<WindTrail> windTrails = new ArrayList<>();
    private List<RainRipple> rainRipples = new ArrayList<>();
    private Lightning lightning = new Lightning();
    private Aurora aurora = new Aurora();
    
    // Animation states
    private float animationTime = 0f;
    private String currentWeather = "clear";
    private boolean isNight = false;
    
    // Enhanced effects
    private float depthBlurAmount = 0f;
    private float colorGradingIntensity = 1f;
    private float vignetteStrength = 0.3f;
    
    // Constants - increased for better visibility
    private static final int LAYER_COUNT = 5;
    private static final int BOKEH_COUNT = 30;
    private static final int LIGHT_LEAK_COUNT = 5;
    private static final int SPARKLE_COUNT = 50;
    private static final int SHOOTING_STAR_MAX = 5;
    private static final int FIREFLY_COUNT = 25;
    private static final int LEAF_COUNT = 15;
    private static final int WIND_TRAIL_COUNT = 8;
    private static final int RIPPLE_MAX = 30;

    public Ultra3DWeatherView(Context context) {
        super(context);
        init();
    }

    public Ultra3DWeatherView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        
        // Initialize depth layers
        for (int i = 0; i < LAYER_COUNT; i++) {
            particleLayers.add(new ParticleLayer(i));
        }
        
        // Initialize bokeh effects
        for (int i = 0; i < BOKEH_COUNT; i++) {
            bokehList.add(new Bokeh());
        }
        
        // Initialize light leaks
        for (int i = 0; i < LIGHT_LEAK_COUNT; i++) {
            lightLeaks.add(new LightLeak(i));
        }
        
        // Initialize sparkles
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            sparkles.add(new Sparkle());
        }
        
        // Initialize shooting stars
        for (int i = 0; i < SHOOTING_STAR_MAX; i++) {
            shootingStars.add(new ShootingStar());
        }
        
        // Initialize fireflies
        for (int i = 0; i < FIREFLY_COUNT; i++) {
            fireflies.add(new Firefly());
        }
        
        // Initialize falling leaves
        for (int i = 0; i < LEAF_COUNT; i++) {
            fallingLeaves.add(new FallingLeaf());
        }
        
        // Initialize wind trails
        for (int i = 0; i < WIND_TRAIL_COUNT; i++) {
            windTrails.add(new WindTrail());
        }
        
        // Initialize rain ripples
        for (int i = 0; i < RIPPLE_MAX; i++) {
            rainRipples.add(new RainRipple());
        }
        
        startAnimation();
    }

    public void setWeather(String weather, boolean night) {
        this.currentWeather = weather.toLowerCase();
        this.isNight = night;
        updateParticlesForWeather();
        
        android.util.Log.d("Ultra3DWeatherView", "🎬 3D Effects configured: '" + currentWeather + 
            "' | Night: " + night + " | Layers: " + LAYER_COUNT);
        android.util.Log.d("Ultra3DWeatherView", "✅ Checks: clear=" + currentWeather.contains("clear") + 
            " | cloud=" + currentWeather.contains("cloud") + 
            " | broken=" + currentWeather.contains("broken") + 
            " | scattered=" + currentWeather.contains("scattered"));
    }

    private void updateParticlesForWeather() {
        // Determine particle type based on weather
        String particleType = "clear"; // default - no particles
        
        if (currentWeather.contains("rain") || currentWeather.contains("drizzle")) {
            particleType = "rain";
        } else if (currentWeather.contains("snow") || currentWeather.contains("sleet")) {
            particleType = "snow";
        } else if (currentWeather.contains("cloud") && !isNight) {
            particleType = "cloud";
        }
        
        // Update all layers with the correct type
        for (ParticleLayer layer : particleLayers) {
            layer.setWeatherType(particleType);
        }
        
        android.util.Log.d("Ultra3DWeatherView", "🌊 Particle layers updated: " + currentWeather + " → Type: " + particleType);
    }

    private void startAnimation() {
        mainAnimator = ValueAnimator.ofFloat(0f, 10000f);
        mainAnimator.setDuration(Long.MAX_VALUE);
        mainAnimator.setInterpolator(new LinearInterpolator());
        mainAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mainAnimator.addUpdateListener(animation -> {
            animationTime = (float) animation.getAnimatedValue();
            
            // Update all layers
            for (ParticleLayer layer : particleLayers) {
                layer.update(animationTime);
            }
            
            // Update bokeh
            for (Bokeh bokeh : bokehList) {
                bokeh.update(animationTime);
            }
            
            // Update light leaks
            for (LightLeak leak : lightLeaks) {
                leak.update(animationTime);
            }
            
            // Update sparkles
            for (Sparkle sparkle : sparkles) {
                sparkle.update(animationTime);
            }
            
            // Update shooting stars
            for (ShootingStar star : shootingStars) {
                star.update(animationTime);
            }
            
            // Update fireflies
            for (Firefly firefly : fireflies) {
                firefly.update(animationTime);
            }
            
            // Update falling leaves
            for (FallingLeaf leaf : fallingLeaves) {
                leaf.update(animationTime);
            }
            
            // Update wind trails
            for (WindTrail trail : windTrails) {
                trail.update(animationTime);
            }
            
            // Update rain ripples
            for (RainRipple ripple : rainRipples) {
                ripple.update(animationTime);
            }
            
            // Update lightning
            lightning.update(animationTime);
            
            // Update aurora
            aurora.update(animationTime);
            
            invalidate();
        });
        mainAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if (w > 0 && h > 0) {
            // Reinitialize with new dimensions
            for (ParticleLayer layer : particleLayers) {
                layer.init(w, h);
            }
            for (Bokeh bokeh : bokehList) {
                bokeh.init(w, h);
            }
            for (LightLeak leak : lightLeaks) {
                leak.init(w, h);
            }
            for (Sparkle sparkle : sparkles) {
                sparkle.init(w, h);
            }
            for (ShootingStar star : shootingStars) {
                star.init(w, h);
            }
            for (Firefly firefly : fireflies) {
                firefly.init(w, h);
            }
            for (FallingLeaf leaf : fallingLeaves) {
                leaf.init(w, h);
            }
            for (WindTrail trail : windTrails) {
                trail.init(w, h);
            }
            for (RainRipple ripple : rainRipples) {
                ripple.init(w, h);
            }
            lightning.init(w, h);
            aurora.init(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;
        
        // DEBUG: Draw semi-transparent overlay to verify view is rendering
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(30, 255, 0, 0)); // Red tint
        canvas.drawRect(0, 0, width, height, paint);
        
        // DEBUG: Draw text to show current weather
        paint.setColor(Color.WHITE);
        paint.setTextSize(40f);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("Weather: " + currentWeather, 50, 200, paint);
        canvas.drawText("Night: " + isNight, 50, 260, paint);
        canvas.drawText("Layers: " + LAYER_COUNT, 50, 320, paint);
        
        // 1. Draw vignette effect
        drawVignette(canvas, width, height);
        
        // 2. Draw depth layers (back to front)
        for (int i = LAYER_COUNT - 1; i >= 0; i--) {
            ParticleLayer layer = particleLayers.get(i);
            layer.draw(canvas, paint, width, height);
        }
        
        // 3. Draw aurora (rare, clear night only)
        if (isNight && currentWeather.contains("clear") && random.nextFloat() > 0.7f) {
            aurora.draw(canvas, paint, width, height);
        }
        
        // 4. Draw sparkles (day clear/cloudy weather)
        if (!isNight && (currentWeather.contains("clear") || currentWeather.contains("sun") || currentWeather.contains("cloud"))) {
            drawSparkles(canvas);
        }
        
        // 5. Draw bokeh effects (clear or light clouds)
        if (!isNight && (currentWeather.contains("clear") || currentWeather.contains("broken") || currentWeather.contains("scattered"))) {
            drawBokeh(canvas);
        }
        
        // 6. Draw shooting stars (clear night)
        if (isNight && currentWeather.contains("clear")) {
            drawShootingStars(canvas);
        }
        
        // 7. Draw fireflies (clear warm night)
        if (isNight && currentWeather.contains("clear") && !currentWeather.contains("cold")) {
            drawFireflies(canvas);
        }
        
        // 8. Draw falling leaves (disabled by default - too distracting)
        // Uncomment if you want autumn leaf effects:
        // if (!currentWeather.contains("rain") && !currentWeather.contains("snow") && !isNight) {
        //     drawFallingLeaves(canvas);
        // }
        
        // 9. Draw wind trails
        if (currentWeather.contains("wind") || currentWeather.contains("storm")) {
            drawWindTrails(canvas);
        }
        
        // 10. Draw rain ripples
        if (currentWeather.contains("rain")) {
            drawRainRipples(canvas);
        }
        
        // 11. Draw lightning
        if (currentWeather.contains("storm") || currentWeather.contains("thunder")) {
            lightning.draw(canvas, paint, width, height);
        }
        
        // 12. Draw light leaks (sun/clear/light clouds)
        if (currentWeather.contains("sun") || currentWeather.contains("clear") || 
            currentWeather.contains("broken") || currentWeather.contains("scattered")) {
            drawLightLeaks(canvas, width, height);
        }
        
        // 13. Draw atmospheric glow (always for ambience)
        drawAtmosphericGlow(canvas, width, height);
    }

    private void drawVignette(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        
        // Radial gradient from center
        int centerX = width / 2;
        int centerY = height / 2;
        float radius = (float) Math.sqrt(width * width + height * height) / 2;
        
        int[] colors = {
            Color.argb(0, 0, 0, 0),
            Color.argb((int)(vignetteStrength * 255), 0, 0, 0)
        };
        float[] positions = {0.4f, 1f};
        
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            colors, positions, Shader.TileMode.CLAMP
        );
        
        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);
    }

    private void drawBokeh(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        
        for (Bokeh bokeh : bokehList) {
            // Outer glow
            paint.setColor(Color.argb(
                (int)(bokeh.alpha * 30),
                255, 255, 200
            ));
            paint.setMaskFilter(new BlurMaskFilter(bokeh.size * 0.8f, BlurMaskFilter.Blur.NORMAL));
            canvas.drawCircle(bokeh.x, bokeh.y, bokeh.size * 1.5f, paint);
            
            // Inner bright circle
            paint.setColor(Color.argb(
                (int)(bokeh.alpha * 100),
                255, 255, 220
            ));
            paint.setMaskFilter(null);
            canvas.drawCircle(bokeh.x, bokeh.y, bokeh.size * 0.3f, paint);
            
            // Hexagonal shape (bokeh shape)
            paint.setColor(Color.argb(
                (int)(bokeh.alpha * 50),
                255, 250, 200
            ));
            drawHexagon(canvas, bokeh.x, bokeh.y, bokeh.size, paint);
        }
        
        paint.setMaskFilter(null);
    }

    private void drawHexagon(Canvas canvas, float cx, float cy, float radius, Paint paint) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI / 3 * i);
            float x = cx + radius * (float) Math.cos(angle);
            float y = cy + radius * (float) Math.sin(angle);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawLightLeaks(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        
        for (LightLeak leak : lightLeaks) {
            // Create light leak gradient
            int[] colors = {
                Color.argb((int)(leak.alpha * 60), 255, 200, 100),
                Color.argb((int)(leak.alpha * 30), 255, 220, 150),
                Color.argb(0, 255, 240, 200)
            };
            float[] positions = {0f, 0.5f, 1f};
            
            LinearGradient gradient = new LinearGradient(
                leak.x, leak.y,
                leak.x + leak.width, leak.y + leak.height,
                colors, positions, Shader.TileMode.CLAMP
            );
            
            paint.setShader(gradient);
            paint.setAlpha((int)(leak.alpha * 255));
            
            // Draw light leak shape
            RectF rect = new RectF(
                leak.x, leak.y,
                leak.x + leak.width, leak.y + leak.height
            );
            canvas.drawRect(rect, paint);
            
            paint.setShader(null);
            paint.setAlpha(255);
        }
    }

    private void drawAtmosphericGlow(Canvas canvas, int width, int height) {
        // Draw atmospheric glow for most weather (except heavy rain/storm)
        if (currentWeather.contains("storm") || currentWeather.contains("heavy")) return;
        
        paint.setStyle(Paint.Style.FILL);
        
        // Sun glow from top center
        int centerX = width / 2;
        int centerY = height / 4;
        float radius = width * 0.8f;
        
        int[] colors;
        if (isNight) {
            colors = new int[]{
                Color.argb(20, 100, 120, 200),
                Color.argb(10, 80, 100, 180),
                Color.argb(0, 60, 80, 160)
            };
        } else {
            colors = new int[]{
                Color.argb(40, 255, 230, 150),
                Color.argb(20, 255, 200, 100),
                Color.argb(0, 255, 180, 80)
            };
        }
        float[] positions = {0f, 0.5f, 1f};
        
        RadialGradient gradient = new RadialGradient(
            centerX, centerY, radius,
            colors, positions, Shader.TileMode.CLAMP
        );
        
        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height / 2, paint);
        paint.setShader(null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mainAnimator != null) {
            mainAnimator.cancel();
        }
    }

    // ========== PARTICLE LAYER CLASS ==========
    private class ParticleLayer {
        int layerIndex;
        float depth; // 0.0 (far) to 1.0 (near)
        float speed;
        float scale;
        int alpha;
        List<Particle3D> particles = new ArrayList<>();
        String weatherType = "clear";
        
        ParticleLayer(int index) {
            this.layerIndex = index;
            this.depth = (float) index / LAYER_COUNT;
            this.speed = 0.5f + depth * 1.5f; // Nearer layers move faster
            this.scale = 0.3f + depth * 0.7f; // Nearer layers bigger
            this.alpha = (int) (100 + depth * 155); // Nearer layers more opaque
        }
        
        void init(int width, int height) {
            particles.clear();
            // Increase particle count significantly for better visibility
            int count = 50 + layerIndex * 20; // Was 30 + 10, now 50 + 20
            for (int i = 0; i < count; i++) {
                particles.add(new Particle3D(width, height, depth));
            }
        }
        
        void setWeatherType(String weather) {
            this.weatherType = weather;
            for (Particle3D p : particles) {
                p.setType(weather);
            }
        }
        
        void update(float time) {
            for (Particle3D p : particles) {
                p.update(time, speed);
            }
        }
        
        void draw(Canvas canvas, Paint paint, int width, int height) {
            // Only draw particles if weather matches
            if (!shouldDrawParticles()) {
                return;
            }
            
            // Apply depth blur for far layers
            if (depth < 0.5f) {
                float blurRadius = (1f - depth * 2) * 10f;
                paint.setMaskFilter(new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL));
            }
            
            for (Particle3D p : particles) {
                p.draw(canvas, paint, scale, alpha);
            }
            
            paint.setMaskFilter(null);
        }
        
        boolean shouldDrawParticles() {
            // Check if current weather matches particle type
            if (weatherType.contains("rain")) {
                return currentWeather.contains("rain") || currentWeather.contains("drizzle");
            } else if (weatherType.contains("snow")) {
                return currentWeather.contains("snow") || currentWeather.contains("sleet");
            } else if (weatherType.contains("cloud")) {
                return currentWeather.contains("cloud") && !isNight;
            }
            return false; // Don't draw for clear weather
        }
    }

    // ========== 3D PARTICLE CLASS ==========
    private class Particle3D {
        float x, y, z;
        float vx, vy, vz;
        float size;
        float rotation;
        float rotationSpeed;
        int color;
        String type = "rain";
        
        Particle3D(int width, int height, float depth) {
            reset(width, height, depth);
        }
        
        void reset(int width, int height, float depth) {
            x = random.nextFloat() * width;
            y = -random.nextFloat() * 200;
            z = depth;
            vx = (random.nextFloat() - 0.5f) * 2f;
            vy = 5f + random.nextFloat() * 10f;
            vz = (random.nextFloat() - 0.5f) * 0.1f;
            size = 3f + random.nextFloat() * 8f;
            rotation = random.nextFloat() * 360f;
            rotationSpeed = (random.nextFloat() - 0.5f) * 5f;
            updateColor();
        }
        
        void setType(String weather) {
            this.type = weather;
            updateColor();
        }
        
        void updateColor() {
            if (type.contains("rain")) {
                color = Color.argb(200, 180, 200, 255);
            } else if (type.contains("snow")) {
                color = Color.argb(230, 255, 255, 255);
            } else if (type.contains("cloud")) {
                // More visible clouds with bright white
                color = Color.argb(220, 240, 245, 255);
            } else {
                color = Color.argb(100, 255, 255, 255);
            }
        }
        
        void update(float time, float speedMultiplier) {
            y += vy * speedMultiplier;
            x += vx;
            rotation += rotationSpeed;
            
            // Wind effect
            x += (float) Math.sin(time * 0.001f + y * 0.01f) * 0.5f;
            
            // Reset if out of bounds
            if (y > getHeight() + 100) {
                y = -100;
                x = random.nextFloat() * getWidth();
            }
        }
        
        void draw(Canvas canvas, Paint paint, float scale, int alpha) {
            paint.setColor(color);
            paint.setAlpha((alpha * Color.alpha(color)) / 255);
            
            float drawSize = size * scale;
            
            if (type.contains("snow")) {
                // Snowflake with rotation
                canvas.save();
                canvas.rotate(rotation, x, y);
                drawSnowflake(canvas, paint, x, y, drawSize);
                canvas.restore();
            } else if (type.contains("rain")) {
                // Rain streak with motion blur
                paint.setStrokeWidth(2f * scale);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(x, y, x - vx * 3, y - vy * 2, paint);
                paint.setStyle(Paint.Style.FILL);
            } else if (type.contains("cloud")) {
                // Cloud particles - soft and fluffy with stronger blur
                paint.setMaskFilter(new BlurMaskFilter(drawSize * 1.2f, BlurMaskFilter.Blur.NORMAL));
                canvas.drawCircle(x, y, drawSize * 2.5f, paint);
                paint.setMaskFilter(null);
            } else {
                // Generic particle
                canvas.drawCircle(x, y, drawSize, paint);
            }
        }
        
        void drawSnowflake(Canvas canvas, Paint paint, float cx, float cy, float radius) {
            paint.setStrokeWidth(1.5f);
            paint.setStyle(Paint.Style.STROKE);
            
            // Draw 6 arms
            for (int i = 0; i < 6; i++) {
                float angle = (float) (Math.PI / 3 * i);
                float x1 = cx;
                float y1 = cy;
                float x2 = cx + radius * (float) Math.cos(angle);
                float y2 = cy + radius * (float) Math.sin(angle);
                canvas.drawLine(x1, y1, x2, y2, paint);
                
                // Small branches
                float branchLen = radius * 0.3f;
                float bx = cx + radius * 0.6f * (float) Math.cos(angle);
                float by = cy + radius * 0.6f * (float) Math.sin(angle);
                canvas.drawLine(bx, by,
                    bx + branchLen * (float) Math.cos(angle + Math.PI / 4),
                    by + branchLen * (float) Math.sin(angle + Math.PI / 4), paint);
                canvas.drawLine(bx, by,
                    bx + branchLen * (float) Math.cos(angle - Math.PI / 4),
                    by + branchLen * (float) Math.sin(angle - Math.PI / 4), paint);
            }
            
            paint.setStyle(Paint.Style.FILL);
        }
    }

    // ========== BOKEH CLASS ==========
    private class Bokeh {
        float x, y, size, alpha;
        float pulseSpeed;
        float pulseOffset;
        
        void init(int width, int height) {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height * 0.7f; // Top 70% of screen
            size = 20f + random.nextFloat() * 60f;
            pulseSpeed = 0.5f + random.nextFloat();
            pulseOffset = random.nextFloat() * 1000f;
        }
        
        void update(float time) {
            alpha = 0.3f + 0.4f * (float) Math.abs(Math.sin((time + pulseOffset) * pulseSpeed * 0.001f));
        }
    }

    // ========== LIGHT LEAK CLASS ==========
    private class LightLeak {
        float x, y, width, height, alpha;
        float speed;
        int index;
        
        LightLeak(int idx) {
            this.index = idx;
        }
        
        void init(int w, int h) {
            width = w * 0.3f + random.nextFloat() * w * 0.2f;
            height = h * 0.5f + random.nextFloat() * h * 0.3f;
            x = random.nextFloat() * (w - width);
            y = -height;
            speed = 0.1f + random.nextFloat() * 0.3f;
            alpha = 0.3f + random.nextFloat() * 0.4f;
        }
        
        void update(float time) {
            y += speed;
            if (y > getHeight()) {
                y = -height;
                x = random.nextFloat() * (getWidth() - width);
            }
        }
    }

    // ========== NEW EFFECTS DRAWING METHODS ==========
    
    private void drawSparkles(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (Sparkle sparkle : sparkles) {
            if (sparkle.active) {
                // Brighter sparkles
                paint.setColor(Color.argb((int)(sparkle.alpha * 200), 255, 255, 240));
                paint.setMaskFilter(new BlurMaskFilter(sparkle.size * 0.8f, BlurMaskFilter.Blur.NORMAL));
                
                // Larger outer glow
                canvas.drawCircle(sparkle.x, sparkle.y, sparkle.size * 2.5f, paint);
                
                // Draw 4-pointed star
                canvas.save();
                canvas.rotate(sparkle.rotation, sparkle.x, sparkle.y);
                
                paint.setMaskFilter(null);
                paint.setColor(Color.argb((int)(sparkle.alpha * 255), 255, 255, 255));
                canvas.drawCircle(sparkle.x, sparkle.y, sparkle.size, paint);
                
                paint.setStrokeWidth(3f);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawLine(sparkle.x - sparkle.size * 2.5f, sparkle.y, sparkle.x + sparkle.size * 2.5f, sparkle.y, paint);
                canvas.drawLine(sparkle.x, sparkle.y - sparkle.size * 2.5f, sparkle.x, sparkle.y + sparkle.size * 2.5f, paint);
                canvas.restore();
                paint.setStyle(Paint.Style.FILL);
            }
        }
        paint.setMaskFilter(null);
    }
    
    private void drawShootingStars(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        for (ShootingStar star : shootingStars) {
            if (star.active) {
                paint.setStrokeWidth(2f);
                paint.setColor(Color.argb((int)(star.alpha * 255), 255, 255, 255));
                paint.setMaskFilter(new BlurMaskFilter(3f, BlurMaskFilter.Blur.NORMAL));
                
                // Draw trail
                canvas.drawLine(star.x, star.y, star.x - star.trailLength, star.y + star.trailLength * 0.5f, paint);
                
                // Draw head
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(star.x, star.y, 4f, paint);
                paint.setStyle(Paint.Style.STROKE);
            }
        }
        paint.setMaskFilter(null);
        paint.setStyle(Paint.Style.FILL);
    }
    
    private void drawFireflies(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (Firefly firefly : fireflies) {
            if (firefly.active) {
                int glowColor = Color.argb((int)(firefly.alpha * 255), 255, 255, 100);
                paint.setColor(glowColor);
                paint.setMaskFilter(new BlurMaskFilter(firefly.size * 2, BlurMaskFilter.Blur.NORMAL));
                canvas.drawCircle(firefly.x, firefly.y, firefly.size * 2, paint);
                
                paint.setMaskFilter(null);
                paint.setColor(Color.argb((int)(firefly.alpha * 255), 255, 255, 150));
                canvas.drawCircle(firefly.x, firefly.y, firefly.size, paint);
            }
        }
        paint.setMaskFilter(null);
    }
    
    private void drawFallingLeaves(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        for (FallingLeaf leaf : fallingLeaves) {
            canvas.save();
            canvas.rotate(leaf.rotation, leaf.x, leaf.y);
            
            // Leaf shape (autumn colors)
            int[] colors = {Color.rgb(255, 140, 0), Color.rgb(200, 100, 0), Color.rgb(150, 80, 0)};
            paint.setColor(colors[leaf.colorIndex % colors.length]);
            paint.setAlpha((int)(leaf.alpha * 255));
            
            Path leafPath = new Path();
            leafPath.moveTo(leaf.x, leaf.y - leaf.size);
            leafPath.quadTo(leaf.x + leaf.size, leaf.y, leaf.x, leaf.y + leaf.size);
            leafPath.quadTo(leaf.x - leaf.size, leaf.y, leaf.x, leaf.y - leaf.size);
            canvas.drawPath(leafPath, paint);
            
            canvas.restore();
        }
        paint.setAlpha(255);
    }
    
    private void drawWindTrails(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        
        for (WindTrail trail : windTrails) {
            if (trail.active) {
                paint.setColor(Color.argb((int)(trail.alpha * 100), 200, 200, 255));
                
                Path path = new Path();
                path.moveTo(trail.x, trail.y);
                path.quadTo(
                    trail.x + trail.length * 0.5f, trail.y + trail.wave,
                    trail.x + trail.length, trail.y
                );
                canvas.drawPath(path, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }
    
    private void drawRainRipples(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        
        for (RainRipple ripple : rainRipples) {
            if (ripple.active) {
                paint.setStrokeWidth(2f);
                paint.setColor(Color.argb((int)((1 - ripple.progress) * 150), 180, 200, 255));
                canvas.drawCircle(ripple.x, ripple.y, ripple.radius * ripple.progress, paint);
            }
        }
        paint.setStyle(Paint.Style.FILL);
    }

    // ========== NEW EFFECT CLASSES ==========
    
    private class Sparkle {
        float x, y, size, alpha, rotation;
        float twinkleSpeed, twinkleOffset;
        boolean active = true;
        
        void init(int width, int height) {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height * 0.7f; // Cover more area
            size = 4f + random.nextFloat() * 6f; // Bigger sparkles: 4-10 instead of 2-6
            twinkleSpeed = 0.5f + random.nextFloat() * 1.5f;
            twinkleOffset = random.nextFloat() * 1000f;
            rotation = random.nextFloat() * 360f;
        }
        
        void update(float time) {
            alpha = 0.2f + 0.6f * (float) Math.abs(Math.sin((time + twinkleOffset) * twinkleSpeed * 0.001f));
            rotation += 0.5f;
        }
    }
    
    private class ShootingStar {
        float x, y, speed, trailLength, alpha;
        boolean active = false;
        float lifeTime = 0f;
        float maxLife = 2000f;
        
        void init(int width, int height) {
            reset(width, height);
        }
        
        void reset(int width, int height) {
            x = width + random.nextFloat() * 200;
            y = random.nextFloat() * height * 0.3f;
            speed = 5f + random.nextFloat() * 10f;
            trailLength = 50f + random.nextFloat() * 100f;
            alpha = 1f;
            active = false;
            lifeTime = 0f;
        }
        
        void update(float time) {
            if (!active && random.nextFloat() > 0.999f) {
                active = true;
                lifeTime = 0f;
            }
            
            if (active) {
                x -= speed;
                y += speed * 0.5f;
                lifeTime += 16f; // Approximate frame time
                
                if (lifeTime > maxLife || x < -200) {
                    reset(getWidth(), getHeight());
                }
                
                alpha = Math.max(0, 1f - lifeTime / maxLife);
            }
        }
    }
    
    private class Firefly {
        float x, y, size, alpha;
        float vx, vy, targetX, targetY;
        float pulseSpeed, pulseOffset;
        boolean active = true;
        
        void init(int width, int height) {
            x = random.nextFloat() * width;
            y = height * 0.3f + random.nextFloat() * height * 0.5f;
            size = 3f + random.nextFloat() * 3f;
            pulseSpeed = 1f + random.nextFloat() * 2f;
            pulseOffset = random.nextFloat() * 1000f;
            pickNewTarget(width, height);
        }
        
        void pickNewTarget(int width, int height) {
            targetX = random.nextFloat() * width;
            targetY = height * 0.3f + random.nextFloat() * height * 0.5f;
        }
        
        void update(float time) {
            // Pulsing glow
            alpha = 0.3f + 0.6f * (float) Math.abs(Math.sin((time + pulseOffset) * pulseSpeed * 0.001f));
            
            // Move toward target
            vx = (targetX - x) * 0.01f;
            vy = (targetY - y) * 0.01f;
            x += vx;
            y += vy;
            
            // Pick new target when close
            if (Math.abs(x - targetX) < 20 && Math.abs(y - targetY) < 20) {
                pickNewTarget(getWidth(), getHeight());
            }
        }
    }
    
    private class FallingLeaf {
        float x, y, size, rotation, rotationSpeed, alpha;
        float vx, vy, swingSpeed, swingOffset;
        int colorIndex;
        
        void init(int width, int height) {
            reset(width, height);
        }
        
        void reset(int width, int height) {
            x = random.nextFloat() * width;
            y = -50f;
            size = 10f + random.nextFloat() * 15f;
            vy = 1f + random.nextFloat() * 2f;
            swingSpeed = 0.5f + random.nextFloat();
            swingOffset = random.nextFloat() * 1000f;
            rotationSpeed = (random.nextFloat() - 0.5f) * 10f;
            rotation = random.nextFloat() * 360f;
            alpha = 0.6f + random.nextFloat() * 0.4f;
            colorIndex = random.nextInt(3);
        }
        
        void update(float time) {
            y += vy;
            vx = (float) Math.sin((time + swingOffset) * swingSpeed * 0.001f) * 2f;
            x += vx;
            rotation += rotationSpeed;
            
            if (y > getHeight() + 100) {
                reset(getWidth(), getHeight());
            }
        }
    }
    
    private class WindTrail {
        float x, y, length, wave, alpha;
        float speed;
        boolean active = false;
        
        void init(int width, int height) {
            reset(width, height);
        }
        
        void reset(int width, int height) {
            x = -200f;
            y = random.nextFloat() * height;
            length = 100f + random.nextFloat() * 200f;
            wave = (random.nextFloat() - 0.5f) * 50f;
            speed = 5f + random.nextFloat() * 10f;
            alpha = 0.3f + random.nextFloat() * 0.5f;
            active = false;
        }
        
        void update(float time) {
            if (!active && random.nextFloat() > 0.99f) {
                active = true;
            }
            
            if (active) {
                x += speed;
                if (x > getWidth() + 200) {
                    reset(getWidth(), getHeight());
                }
            }
        }
    }
    
    private class RainRipple {
        float x, y, radius, progress;
        boolean active = false;
        
        void init(int width, int height) {
            reset(width, height);
        }
        
        void reset(int width, int height) {
            x = random.nextFloat() * width;
            y = height * 0.7f + random.nextFloat() * height * 0.3f;
            radius = 20f + random.nextFloat() * 40f;
            progress = 0f;
            active = false;
        }
        
        void update(float time) {
            if (!active && random.nextFloat() > 0.95f) {
                active = true;
                progress = 0f;
            }
            
            if (active) {
                progress += 0.02f;
                if (progress >= 1f) {
                    reset(getWidth(), getHeight());
                }
            }
        }
    }
    
    private class Lightning {
        float x, y, width, height;
        boolean active = false;
        float flashTime = 0f;
        float maxFlash = 200f;
        List<float[]> segments = new ArrayList<>();
        
        void init(int w, int h) {
            width = w;
            height = h;
        }
        
        void update(float time) {
            if (!active && random.nextFloat() > 0.995f) {
                active = true;
                flashTime = 0f;
                generateLightning();
            }
            
            if (active) {
                flashTime += 16f;
                if (flashTime > maxFlash) {
                    active = false;
                    segments.clear();
                }
            }
        }
        
        void generateLightning() {
            segments.clear();
            float startX = width * 0.3f + random.nextFloat() * width * 0.4f;
            float startY = 0f;
            float endX = startX + (random.nextFloat() - 0.5f) * 200f;
            float endY = height * 0.6f;
            
            // Main bolt
            float x1 = startX, y1 = startY;
            int steps = 10;
            for (int i = 0; i < steps; i++) {
                float t = (float) i / steps;
                float x2 = startX + (endX - startX) * t + (random.nextFloat() - 0.5f) * 50f;
                float y2 = startY + (endY - startY) * t;
                segments.add(new float[]{x1, y1, x2, y2});
                x1 = x2;
                y1 = y2;
            }
            
            // Branches
            for (int i = 0; i < 3; i++) {
                int branchFrom = random.nextInt(segments.size());
                float[] parent = segments.get(branchFrom);
                float bx1 = parent[2], by1 = parent[3];
                float bx2 = bx1 + (random.nextFloat() - 0.5f) * 100f;
                float by2 = by1 + 50f + random.nextFloat() * 100f;
                segments.add(new float[]{bx1, by1, bx2, by2});
            }
        }
        
        void draw(Canvas canvas, Paint paint, int w, int h) {
            if (!active) return;
            
            float alpha = 1f - flashTime / maxFlash;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            paint.setColor(Color.argb((int)(alpha * 255), 200, 220, 255));
            paint.setMaskFilter(new BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL));
            
            for (float[] seg : segments) {
                canvas.drawLine(seg[0], seg[1], seg[2], seg[3], paint);
            }
            
            // Flash overlay
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb((int)(alpha * 50), 255, 255, 255));
            canvas.drawRect(0, 0, w, h, paint);
            
            paint.setMaskFilter(null);
        }
    }
    
    private class Aurora {
        List<float[]> waves = new ArrayList<>();
        float[] colors;
        float animOffset = 0f;
        
        void init(int w, int h) {
            // Generate aurora waves
            waves.clear();
            for (int i = 0; i < 3; i++) {
                float y = h * 0.2f + i * 50f;
                waves.add(new float[]{y, random.nextFloat() * 1000f});
            }
            
            // Aurora colors (green, blue, purple)
            colors = new float[]{120f, 220f, 280f};
        }
        
        void update(float time) {
            animOffset = time * 0.0005f;
        }
        
        void draw(Canvas canvas, Paint paint, int w, int h) {
            paint.setStyle(Paint.Style.FILL);
            
            for (int i = 0; i < waves.size(); i++) {
                float[] wave = waves.get(i);
                float y = wave[0];
                float offset = wave[1];
                
                // Create wave path
                Path path = new Path();
                path.moveTo(0, h);
                
                for (int x = 0; x <= w; x += 20) {
                    float waveY = y + (float) Math.sin((x * 0.01f + animOffset + offset)) * 30f;
                    if (x == 0) {
                        path.lineTo(x, waveY);
                    } else {
                        path.lineTo(x, waveY);
                    }
                }
                
                path.lineTo(w, h);
                path.close();
                
                // Aurora gradient
                float hue = colors[i % colors.length];
                int color1 = Color.HSVToColor(50, new float[]{hue, 0.7f, 0.8f});
                int color2 = Color.HSVToColor(0, new float[]{hue, 0.5f, 0.6f});
                
                LinearGradient gradient = new LinearGradient(
                    0, y - 100, 0, y + 100,
                    color1, color2, Shader.TileMode.CLAMP
                );
                
                paint.setShader(gradient);
                canvas.drawPath(path, paint);
            }
            
            paint.setShader(null);
        }
    }
}
