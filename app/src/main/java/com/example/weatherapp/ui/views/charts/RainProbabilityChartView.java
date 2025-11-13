package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
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
 * Rain Probability Chart View - iOS Style
 * Displays hourly rain probability as area chart with gradient
 * Shows percentage labels on each data point
 */
public class RainProbabilityChartView extends View {

    private List<HourlyForecastResponse.HourlyItem> hourlyDataList = new ArrayList<>();
    
    private Paint linePaint;
    private Paint fillPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint gridPaint;
    private Paint barPaint;
    
    private float topPadding = 50f;
    private float bottomPadding = 50f;
    private float sidePadding = 30f;
    
    private Path linePath;
    private Path fillPath;
    
    // Colors - iOS style blue gradient
    private static final int LINE_COLOR = Color.parseColor("#007AFF");
    private static final int FILL_START_COLOR = Color.parseColor("#4D007AFF");
    private static final int FILL_END_COLOR = Color.parseColor("#00007AFF");
    private static final int GRID_COLOR = Color.parseColor("#22FFFFFF");

    public RainProbabilityChartView(Context context) {
        super(context);
        init();
    }

    public RainProbabilityChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RainProbabilityChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(LINE_COLOR);
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(26f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#99FFFFFF"));
        labelPaint.setTextSize(22f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(GRID_COLOR);
        gridPaint.setStrokeWidth(1.5f);
        gridPaint.setStyle(Paint.Style.STROKE);
        
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        
        linePath = new Path();
        fillPath = new Path();
    }

    public void setData(List<HourlyForecastResponse.HourlyItem> data) {
        this.hourlyDataList = data != null ? data : new ArrayList<>();
        android.util.Log.d("RainChartView", "setData called with " + 
            (data != null ? data.size() + " items" : "null"));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (hourlyDataList == null || hourlyDataList.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        int dataCount = Math.min(24, hourlyDataList.size());
        if (dataCount == 0) {
            drawEmptyState(canvas);
            return;
        }
        
        float width = getWidth();
        float height = getHeight();
        float chartHeight = height - topPadding - bottomPadding;
        float chartWidth = width - (sidePadding * 2);
        
        // Draw grid lines
        drawGrid(canvas, width, height, chartHeight);
        
        // Calculate point spacing
        float pointSpacing = chartWidth / (dataCount - 1);
        
        linePath.reset();
        fillPath.reset();
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH", Locale.getDefault());
        
        // Collect points for drawing
        List<Float> xPoints = new ArrayList<>();
        List<Float> yPoints = new ArrayList<>();
        
        for (int i = 0; i < dataCount; i++) {
            HourlyForecastResponse.HourlyItem data = hourlyDataList.get(i);
            double pop = data.getPop() * 100; // Convert to percentage
            
            float x = sidePadding + (i * pointSpacing);
            // Invert Y (0% at bottom, 100% at top)
            float y = topPadding + chartHeight - (chartHeight * (float) pop / 100f);
            
            xPoints.add(x);
            yPoints.add(y);
            
            // Build line path
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, topPadding + chartHeight); // Start from bottom
                fillPath.lineTo(x, y);
            } else {
                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }
        }
        
        // Complete fill path
        fillPath.lineTo(xPoints.get(xPoints.size() - 1), topPadding + chartHeight);
        fillPath.close();
        
        // Setup gradient for fill
        LinearGradient gradient = new LinearGradient(
            0, topPadding,
            0, topPadding + chartHeight,
            FILL_START_COLOR,
            FILL_END_COLOR,
            Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);
        
        // Draw fill area
        canvas.drawPath(fillPath, fillPaint);
        
        // Draw line
        canvas.drawPath(linePath, linePaint);
        
        // Draw data points, labels, and bars
        for (int i = 0; i < dataCount; i++) {
            HourlyForecastResponse.HourlyItem data = hourlyDataList.get(i);
            double pop = data.getPop() * 100;
            
            float x = xPoints.get(i);
            float y = yPoints.get(i);
            
            // Draw vertical bar from bottom to point (subtle)
            barPaint.setColor(Color.parseColor("#11007AFF"));
            canvas.drawRect(x - 1.5f, y, x + 1.5f, topPadding + chartHeight, barPaint);
            
            // Draw data point circle
            Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            pointPaint.setColor(LINE_COLOR);
            pointPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, 7f, pointPaint);
            
            // Draw white border
            pointPaint.setColor(Color.WHITE);
            pointPaint.setStyle(Paint.Style.STROKE);
            pointPaint.setStrokeWidth(3f);
            canvas.drawCircle(x, y, 7f, pointPaint);
            
            // Draw percentage label above point
            if (pop > 0) {
                textPaint.setTextSize(24f);
                canvas.drawText((int) pop + "%", x, y - 15f, textPaint);
            }
            
            // Draw hour label below chart
            Date date = new Date(data.getDt() * 1000L);
            String hourLabel = timeFormat.format(date) + "giờ";
            
            // Rotate labels if too crowded
            if (dataCount > 12 && i % 2 != 0) {
                continue; // Skip every other label
            }
            
            labelPaint.setTextSize(dataCount > 12 ? 20f : 22f);
            canvas.drawText(hourLabel, x, height - bottomPadding + 35f, labelPaint);
        }
    }

    /**
     * Draw horizontal grid lines for reference
     */
    private void drawGrid(Canvas canvas, float width, float height, float chartHeight) {
        int gridLines = 5; // 0%, 25%, 50%, 75%, 100%
        
        for (int i = 0; i <= gridLines; i++) {
            float y = topPadding + (chartHeight * i / gridLines);
            canvas.drawLine(sidePadding, y, width - sidePadding, y, gridPaint);
            
            // Draw percentage label on left
            int percentage = 100 - (i * 100 / gridLines);
            labelPaint.setTextAlign(Paint.Align.RIGHT);
            labelPaint.setTextSize(20f);
            canvas.drawText(percentage + "%", sidePadding - 10f, y + 7f, labelPaint);
            labelPaint.setTextAlign(Paint.Align.CENTER);
        }
    }

    /**
     * Draw empty state when no data
     */
    private void drawEmptyState(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        
        textPaint.setColor(Color.parseColor("#66FFFFFF"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("0%", width / 2, height / 2, textPaint);
        
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Không có dữ liệu mưa", width / 2, height / 2 + 40f, labelPaint);
    }
}
