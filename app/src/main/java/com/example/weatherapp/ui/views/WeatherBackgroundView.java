package com.example.weatherapp.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * Animated Weather Background View
 * Creates smooth gradient transitions based on weather conditions
 */
public class WeatherBackgroundView extends View {

    private Paint paint;
    private int[] currentColors;
    private int[] targetColors;
    private float animationProgress = 1f;
    private ValueAnimator colorAnimator;

    // Weather gradient colors
    private static final int[] SUNNY_DAY = {0xFFFFD700, 0xFFFF8C00, 0xFF87CEEB};
    private static final int[] CLEAR_NIGHT = {0xFF1A237E, 0xFF283593, 0xFF000000};
    private static final int[] CLOUDY = {0xFF90A4AE, 0xFF607D8B, 0xFF546E7A};
    private static final int[] RAINY = {0xFF455A64, 0xFF37474F, 0xFF263238};
    private static final int[] THUNDERSTORM = {0xFF311B92, 0xFF1A237E, 0xFF000000};
    private static final int[] SNOWY = {0xFFE3F2FD, 0xFFBBDEFB, 0xFF90CAF9};
    private static final int[] FOGGY = {0xFFCFD8DC, 0xFFB0BEC5, 0xFF90A4AE};
    private static final int[] HOT = {0xFFFF5722, 0xFFFF7043, 0xFFFFAB91};
    private static final int[] COLD = {0xFF0277BD, 0xFF01579B, 0xFF003366};

    public WeatherBackgroundView(Context context) {
        super(context);
        init();
    }

    public WeatherBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        currentColors = SUNNY_DAY.clone();
        targetColors = SUNNY_DAY.clone();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Interpolate colors
        int[] colors = new int[3];
        for (int i = 0; i < 3; i++) {
            colors[i] = interpolateColor(currentColors[i], targetColors[i], animationProgress);
        }

        // Create gradient shader
        LinearGradient gradient = new LinearGradient(
                0, 0,
                0, getHeight(),
                colors,
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );

        paint.setShader(gradient);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    /**
     * Update background based on weather condition
     */
    public void setWeatherCondition(String condition, boolean isNight) {
        int[] newColors;

        if (isNight && !condition.toLowerCase().contains("thunder")) {
            newColors = CLEAR_NIGHT;
        } else {
            String conditionLower = condition.toLowerCase();
            if (conditionLower.contains("clear") || conditionLower.contains("sunny")) {
                newColors = SUNNY_DAY;
            } else if (conditionLower.contains("cloud")) {
                newColors = CLOUDY;
            } else if (conditionLower.contains("rain") || conditionLower.contains("drizzle")) {
                newColors = RAINY;
            } else if (conditionLower.contains("thunder") || conditionLower.contains("storm")) {
                newColors = THUNDERSTORM;
            } else if (conditionLower.contains("snow") || conditionLower.contains("sleet")) {
                newColors = SNOWY;
            } else if (conditionLower.contains("fog") || conditionLower.contains("mist") || conditionLower.contains("haze")) {
                newColors = FOGGY;
            } else {
                newColors = SUNNY_DAY;
            }
        }

        animateToColors(newColors);
    }

    /**
     * Update based on temperature
     */
    public void setTemperature(double temperature) {
        if (temperature > 35) {
            animateToColors(HOT);
        } else if (temperature < 5) {
            animateToColors(COLD);
        }
    }

    private void animateToColors(int[] newColors) {
        if (colorAnimator != null && colorAnimator.isRunning()) {
            colorAnimator.cancel();
        }

        currentColors = getCurrentInterpolatedColors();
        targetColors = newColors.clone();

        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(2000); // 2 seconds smooth transition
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.addUpdateListener(animation -> {
            animationProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        colorAnimator.start();
    }

    private int[] getCurrentInterpolatedColors() {
        int[] colors = new int[3];
        for (int i = 0; i < 3; i++) {
            colors[i] = interpolateColor(currentColors[i], targetColors[i], animationProgress);
        }
        return colors;
    }

    private int interpolateColor(int startColor, int endColor, float fraction) {
        int startA = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endA = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        return ((int) (startA + (endA - startA) * fraction) << 24) |
                ((int) (startR + (endR - startR) * fraction) << 16) |
                ((int) (startG + (endG - startG) * fraction) << 8) |
                (int) (startB + (endB - startB) * fraction);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
    }
}
