package com.example.weatherapp.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Custom view for particle effects (rain, snow, stars)
 * Creates realistic falling particle animations
 */
public class ParticleEffectView extends View {
    
    public enum ParticleType {
        RAIN,
        SNOW,
        STARS,
        NONE
    }
    
    private ParticleType particleType = ParticleType.NONE;
    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private boolean isAnimating = false;
    
    private int particleCount = 50;
    private int particleColor = 0xFFFFFFFF;
    
    public ParticleEffectView(Context context) {
        super(context);
        init();
    }
    
    public ParticleEffectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ParticleEffectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(particleColor);
    }
    
    /**
     * Start particle animation
     */
    public void startAnimation(ParticleType type) {
        this.particleType = type;
        this.isAnimating = true;
        
        // Clear existing particles
        particles.clear();
        
        // Configure based on type
        switch (type) {
            case RAIN:
                particleCount = 80;
                particleColor = 0x88AADDFF;
                break;
            case SNOW:
                particleCount = 50;
                particleColor = 0xFFFFFFFF;
                break;
            case STARS:
                particleCount = 30;
                particleColor = 0xFFFFFF99;
                break;
        }
        
        paint.setColor(particleColor);
        
        // Initialize particles
        initializeParticles();
        
        // Start animation loop
        invalidate();
    }
    
    /**
     * Stop particle animation
     */
    public void stopAnimation() {
        isAnimating = false;
        particles.clear();
        invalidate();
    }
    
    /**
     * Set particle count
     */
    public void setParticleCount(int count) {
        this.particleCount = count;
        if (isAnimating) {
            initializeParticles();
        }
    }
    
    private void initializeParticles() {
        particles.clear();
        
        for (int i = 0; i < particleCount; i++) {
            Particle particle = new Particle();
            particle.x = random.nextInt(getWidth() > 0 ? getWidth() : 1080);
            particle.y = random.nextInt(getHeight() > 0 ? getHeight() : 1920) - getHeight();
            
            switch (particleType) {
                case RAIN:
                    particle.speed = 15 + random.nextFloat() * 10;
                    particle.size = 2 + random.nextFloat() * 2;
                    particle.length = 10 + random.nextInt(20);
                    particle.angle = 10 + random.nextInt(10);
                    break;
                    
                case SNOW:
                    particle.speed = 2 + random.nextFloat() * 3;
                    particle.size = 3 + random.nextFloat() * 5;
                    particle.rotation = random.nextFloat() * 360;
                    particle.rotationSpeed = -1 + random.nextFloat() * 2;
                    particle.swingAmplitude = 20 + random.nextFloat() * 30;
                    particle.swingSpeed = 0.02f + random.nextFloat() * 0.03f;
                    break;
                    
                case STARS:
                    particle.speed = 1 + random.nextFloat() * 2;
                    particle.size = 2 + random.nextFloat() * 3;
                    particle.alpha = 0.3f + random.nextFloat() * 0.7f;
                    particle.twinkleSpeed = 0.02f + random.nextFloat() * 0.03f;
                    break;
            }
            
            particles.add(particle);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isAnimating || particles.isEmpty()) {
            return;
        }
        
        // Draw and update particles
        for (Particle particle : particles) {
            drawParticle(canvas, particle);
            updateParticle(particle);
        }
        
        // Continue animation
        invalidate();
    }
    
    private void drawParticle(Canvas canvas, Particle particle) {
        int alpha = (int) (particle.alpha * 255);
        paint.setAlpha(alpha);
        
        switch (particleType) {
            case RAIN:
                drawRainDrop(canvas, particle);
                break;
            case SNOW:
                drawSnowflake(canvas, particle);
                break;
            case STARS:
                drawStar(canvas, particle);
                break;
        }
    }
    
    private void drawRainDrop(Canvas canvas, Particle particle) {
        paint.setStrokeWidth(particle.size);
        
        float endX = particle.x + (float) Math.sin(Math.toRadians(particle.angle)) * particle.length;
        float endY = particle.y + (float) Math.cos(Math.toRadians(particle.angle)) * particle.length;
        
        canvas.drawLine(particle.x, particle.y, endX, endY, paint);
    }
    
    private void drawSnowflake(Canvas canvas, Particle particle) {
        canvas.save();
        canvas.translate(particle.x, particle.y);
        canvas.rotate(particle.rotation);
        
        // Draw simple snowflake
        float radius = particle.size;
        
        // Draw 6 arms
        for (int i = 0; i < 6; i++) {
            float angle = i * 60;
            float endX = (float) Math.cos(Math.toRadians(angle)) * radius;
            float endY = (float) Math.sin(Math.toRadians(angle)) * radius;
            canvas.drawLine(0, 0, endX, endY, paint);
        }
        
        canvas.restore();
    }
    
    private void drawStar(Canvas canvas, Particle particle) {
        canvas.save();
        canvas.translate(particle.x, particle.y);
        
        Path starPath = new Path();
        float radius = particle.size;
        float innerRadius = radius * 0.4f;
        
        for (int i = 0; i < 5; i++) {
            float angle = (float) (Math.PI * 2 * i / 5 - Math.PI / 2);
            float x = (float) Math.cos(angle) * radius;
            float y = (float) Math.sin(angle) * radius;
            
            if (i == 0) {
                starPath.moveTo(x, y);
            } else {
                starPath.lineTo(x, y);
            }
            
            angle = (float) (Math.PI * 2 * (i + 0.5) / 5 - Math.PI / 2);
            x = (float) Math.cos(angle) * innerRadius;
            y = (float) Math.sin(angle) * innerRadius;
            starPath.lineTo(x, y);
        }
        
        starPath.close();
        canvas.drawPath(starPath, paint);
        
        canvas.restore();
    }
    
    private void updateParticle(Particle particle) {
        // Update position
        particle.y += particle.speed;
        
        switch (particleType) {
            case RAIN:
                particle.x += Math.sin(Math.toRadians(particle.angle)) * 2;
                break;
                
            case SNOW:
                // Swing motion for snow
                particle.swingTime += particle.swingSpeed;
                particle.x += Math.sin(particle.swingTime) * particle.swingAmplitude * 0.05f;
                particle.rotation += particle.rotationSpeed;
                break;
                
            case STARS:
                // Twinkle effect
                particle.twinkleTime += particle.twinkleSpeed;
                particle.alpha = 0.3f + (float) (Math.sin(particle.twinkleTime) * 0.5f + 0.5f) * 0.7f;
                break;
        }
        
        // Reset particle when it goes off screen
        if (particle.y > getHeight()) {
            particle.y = -50;
            particle.x = random.nextInt(getWidth() > 0 ? getWidth() : 1080);
        }
        
        if (particle.x < -50 || particle.x > getWidth() + 50) {
            particle.x = random.nextInt(getWidth() > 0 ? getWidth() : 1080);
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        if (isAnimating) {
            initializeParticles();
        }
    }
    
    /**
     * Particle data class
     */
    private static class Particle {
        float x, y;
        float speed;
        float size;
        float alpha = 1.0f;
        
        // Rain specific
        float length;
        float angle;
        
        // Snow specific
        float rotation;
        float rotationSpeed;
        float swingAmplitude;
        float swingSpeed;
        float swingTime;
        
        // Star specific
        float twinkleSpeed;
        float twinkleTime;
    }
}
