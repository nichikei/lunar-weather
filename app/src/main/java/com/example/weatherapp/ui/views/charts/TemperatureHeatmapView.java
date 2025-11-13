package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.weatherapp.data.responses.HourlyForecastResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Temperature Heatmap View - iOS Style
 * Displays hourly temperature data as colored bars in a heatmap
 */
public class TemperatureHeatmapView extends View {

    private List<HourlyForecastResponse.HourlyItem> hourlyDataList = new ArrayList<>();
    
    private Paint heatmapPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint iconPaint;
    
    private float barSpacing = 8f;
    private float topPadding = 40f;
    private float bottomPadding = 50f;
    private float sidePadding = 16f;
    
    // Temperature color gradient (cold -> hot)
    private static final int COLOR_COLD = Color.parseColor("#4A90E2");      // Blue
    private static final int COLOR_COOL = Color.parseColor("#7ED321");      // Green
    private static final int COLOR_WARM = Color.parseColor("#F5A623");      // Orange
    private static final int COLOR_HOT = Color.parseColor("#D0021B");       // Red

    public TemperatureHeatmapView(Context context) {
        super(context);
        init();
    }

    public TemperatureHeatmapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemperatureHeatmapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        heatmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        heatmapPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#99FFFFFF"));
        labelPaint.setTextSize(24f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        iconPaint.setColor(Color.parseColor("#CCFFFFFF"));
        iconPaint.setStyle(Paint.Style.FILL);
    }

    public void setData(List<HourlyForecastResponse.HourlyItem> data) {
        this.hourlyDataList = data != null ? data : new ArrayList<>();
        android.util.Log.d("TempHeatmapView", "setData called with " + 
            (data != null ? data.size() + " items" : "null"));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (hourlyDataList == null || hourlyDataList.isEmpty()) {
            android.util.Log.w("TempHeatmapView", "onDraw - No data to display");
            return;
        }

        int dataCount = Math.min(24, hourlyDataList.size()); // Show max 24 hours
        android.util.Log.d("TempHeatmapView", "onDraw - Drawing " + dataCount + " bars");
        if (dataCount == 0) return;
        
        float width = getWidth();
        float height = getHeight();
        float chartHeight = height - topPadding - bottomPadding;
        
        // Calculate bar width based on available space
        float availableWidth = width - (sidePadding * 2);
        float barWidth = (availableWidth - (barSpacing * (dataCount - 1))) / dataCount;
        
        // Find min and max temperature for normalization
        double minTemp = hourlyDataList.stream()
            .limit(dataCount)
            .mapToDouble(d -> d.getMain().getTemp())
            .min()
            .orElse(0);
        double maxTemp = hourlyDataList.stream()
            .limit(dataCount)
            .mapToDouble(d -> d.getMain().getTemp())
            .max()
            .orElse(30);
        
        double tempRange = maxTemp - minTemp;
        if (tempRange == 0) tempRange = 1; // Avoid division by zero
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH", Locale.getDefault());
        
        // Draw bars
        for (int i = 0; i < dataCount; i++) {
            HourlyForecastResponse.HourlyItem data = hourlyDataList.get(i);
            double temp = data.getMain().getTemp();
            
            // Calculate bar position and height
            float x = sidePadding + (i * (barWidth + barSpacing));
            
            // Normalize temperature to 0-1 range
            float normalizedTemp = (float) ((temp - minTemp) / tempRange);
            
            // Bar height based on temperature (taller = hotter)
            float barHeight = chartHeight * 0.4f + (chartHeight * 0.6f * normalizedTemp);
            float y = topPadding + (chartHeight - barHeight);
            
            // Get color based on temperature
            int barColor = getTemperatureColor(normalizedTemp);
            heatmapPaint.setColor(barColor);
            
            // Draw rounded bar
            RectF rect = new RectF(x, y, x + barWidth, topPadding + chartHeight);
            canvas.drawRoundRect(rect, 8f, 8f, heatmapPaint);
            
            // Draw temperature text on bar
            int tempValue = (int) Math.round(temp);
            textPaint.setTextSize(barWidth > 30 ? 28f : 22f);
            canvas.drawText(tempValue + "°", 
                x + barWidth / 2, 
                y - 8f, 
                textPaint);
            
            // Draw hour label below
            Date date = new Date(data.getDt() * 1000L);
            String hourLabel = timeFormat.format(date) + "h";
            labelPaint.setTextSize(barWidth > 30 ? 24f : 20f);
            canvas.drawText(hourLabel, 
                x + barWidth / 2, 
                height - bottomPadding + 30f, 
                labelPaint);
            
            // Draw weather icon below hour (small cloud/sun symbol)
            if (data.getWeather() != null && !data.getWeather().isEmpty()) {
                String weather = data.getWeather().get(0).getMain().toLowerCase();
                drawWeatherSymbol(canvas, x + barWidth / 2, height - 20f, weather);
            }
        }
    }

    /**
     * Get color based on normalized temperature (0-1)
     */
    private int getTemperatureColor(float normalizedTemp) {
        if (normalizedTemp < 0.33f) {
            // Cold (Blue)
            return interpolateColor(COLOR_COLD, COLOR_COOL, normalizedTemp / 0.33f);
        } else if (normalizedTemp < 0.66f) {
            // Cool to Warm (Green to Orange)
            return interpolateColor(COLOR_COOL, COLOR_WARM, (normalizedTemp - 0.33f) / 0.33f);
        } else {
            // Hot (Orange to Red)
            return interpolateColor(COLOR_WARM, COLOR_HOT, (normalizedTemp - 0.66f) / 0.34f);
        }
    }

    /**
     * Interpolate between two colors
     */
    private int interpolateColor(int colorStart, int colorEnd, float ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        
        int startA = Color.alpha(colorStart);
        int startR = Color.red(colorStart);
        int startG = Color.green(colorStart);
        int startB = Color.blue(colorStart);
        
        int endA = Color.alpha(colorEnd);
        int endR = Color.red(colorEnd);
        int endG = Color.green(colorEnd);
        int endB = Color.blue(colorEnd);
        
        return Color.argb(
            (int) (startA + ratio * (endA - startA)),
            (int) (startR + ratio * (endR - startR)),
            (int) (startG + ratio * (endG - startG)),
            (int) (startB + ratio * (endB - startB))
        );
    }

    /**
     * Draw simple weather symbol
     */
    private void drawWeatherSymbol(Canvas canvas, float x, float y, String weather) {
        iconPaint.setAlpha(100);
        float radius = 6f;
        
        switch (weather) {
            case "clear":
                // Draw sun (circle)
                canvas.drawCircle(x, y, radius, iconPaint);
                break;
            case "clouds":
                // Draw cloud (two circles)
                canvas.drawCircle(x - 3f, y, radius * 0.7f, iconPaint);
                canvas.drawCircle(x + 3f, y, radius * 0.7f, iconPaint);
                break;
            case "rain":
            case "drizzle":
                // Draw rain drops (small lines)
                iconPaint.setStrokeWidth(2f);
                canvas.drawLine(x, y, x, y + 8f, iconPaint);
                break;
            default:
                canvas.drawCircle(x, y, radius * 0.5f, iconPaint);
                break;
        }
    }
}
