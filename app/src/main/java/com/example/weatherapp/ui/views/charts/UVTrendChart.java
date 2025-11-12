package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom LineChart for UV Index 24-hour trend
 * Shows UV levels throughout the day with gradient fill
 */
public class UVTrendChart extends LineChart {

    private MicroChartView chartHelper;
    private static final int UV_LOW_COLOR = 0xFF4CAF50;      // Green
    private static final int UV_MODERATE_COLOR = 0xFFFFC107; // Yellow
    private static final int UV_HIGH_COLOR = 0xFFFF9800;     // Orange
    private static final int UV_VERY_HIGH_COLOR = 0xFFFF5722; // Red
    private static final int UV_EXTREME_COLOR = 0xFF9C27B0;  // Purple

    public UVTrendChart(Context context) {
        super(context);
    }

    public UVTrendChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UVTrendChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        
        chartHelper = new MicroChartView() {};
        chartHelper.configureBaseChart(this);

        // Configure axes
        XAxis xAxis = getXAxis();
        int textColor = Color.parseColor("#99FFFFFF"); // Semi-transparent white
        chartHelper.configureXAxis(xAxis, textColor);
        xAxis.setLabelCount(6, false); // Show 6 time labels

        YAxis leftAxis = getAxisLeft();
        chartHelper.configureYAxis(leftAxis);

        YAxis rightAxis = getAxisRight();
        chartHelper.configureYAxis(rightAxis);

        // Set minimum height for visibility
        setMinimumHeight(180);
    }

    /**
     * Set UV data for 24-hour period
     * @param uvValues List of UV index values (0-11+)
     * @param hourLabels List of hour labels (e.g., "6AM", "9AM", "12PM")
     */
    public void setUVData(List<Float> uvValues, List<String> hourLabels) {
        if (uvValues == null || uvValues.isEmpty()) {
            clear();
            return;
        }

        // Create entries
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < uvValues.size(); i++) {
            entries.add(new Entry(i, uvValues.get(i)));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "UV Index");
        
        // Line style
        dataSet.setColor(UV_HIGH_COLOR);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(UV_HIGH_COLOR);
        dataSet.setCircleRadius(3.5f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        
        // Smooth curve
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        
        // Gradient fill
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.uv_gradient);
        dataSet.setFillDrawable(drawable);

        // Highlight
        dataSet.setHighlightEnabled(false);

        // Set data
        LineData lineData = new LineData(dataSet);
        setData(lineData);

        // Configure X-axis labels
        getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < hourLabels.size()) {
                    return hourLabels.get(index);
                }
                return "";
            }
        });

        // Animate
        animateX(800);
        invalidate();
    }

    /**
     * Get color based on UV index level
     */
    private int getUVColor(float uvIndex) {
        if (uvIndex < 3) return UV_LOW_COLOR;
        else if (uvIndex < 6) return UV_MODERATE_COLOR;
        else if (uvIndex < 8) return UV_HIGH_COLOR;
        else if (uvIndex < 11) return UV_VERY_HIGH_COLOR;
        else return UV_EXTREME_COLOR;
    }

    /**
     * Update single UV value at specific hour
     */
    public void updateUVValue(int hourIndex, float uvValue) {
        if (getData() != null && getData().getDataSetCount() > 0) {
            ILineDataSet dataSet = getData().getDataSetByIndex(0);
            Entry entry = dataSet.getEntryForIndex(hourIndex);
            if (entry != null) {
                entry.setY(uvValue);
                getData().notifyDataChanged();
                notifyDataSetChanged();
                invalidate();
            }
        }
    }
}
