package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom BarChart for Wind Speed hourly visualization
 * Shows wind speed variations with color-coded bars
 */
public class WindSpeedMicroChart extends BarChart {

    private MicroChartView chartHelper;
    private static final int WIND_LIGHT_COLOR = 0xFF4CAF50;   // Green: 0-10 km/h
    private static final int WIND_MODERATE_COLOR = 0xFFFFC107; // Yellow: 10-20 km/h
    private static final int WIND_STRONG_COLOR = 0xFFFF9800;   // Orange: 20-30 km/h
    private static final int WIND_VERY_STRONG_COLOR = 0xFFFF5722; // Red: 30+ km/h

    public WindSpeedMicroChart(Context context) {
        super(context);
    }

    public WindSpeedMicroChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WindSpeedMicroChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        
        chartHelper = new MicroChartView() {};
        chartHelper.configureBaseChart(this);

        // Configure axes
        XAxis xAxis = getXAxis();
        int textColor = Color.parseColor("#99FFFFFF");
        chartHelper.configureXAxis(xAxis, textColor);
        xAxis.setLabelCount(6, false);

        YAxis leftAxis = getAxisLeft();
        chartHelper.configureYAxis(leftAxis);

        YAxis rightAxis = getAxisRight();
        chartHelper.configureYAxis(rightAxis);

        setMinimumHeight(140);
        setFitBars(true);
    }

    /**
     * Set wind speed data
     * @param windSpeeds List of wind speeds in km/h
     * @param hourLabels List of hour labels
     */
    public void setWindData(List<Float> windSpeeds, List<String> hourLabels) {
        if (windSpeeds == null || windSpeeds.isEmpty()) {
            clear();
            return;
        }

        // Create entries
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        
        for (int i = 0; i < windSpeeds.size(); i++) {
            float speed = windSpeeds.get(i);
            entries.add(new BarEntry(i, speed));
            colors.add(getWindColor(speed));
        }

        // Create dataset
        BarDataSet dataSet = new BarDataSet(entries, "Wind Speed");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);
        dataSet.setHighlightEnabled(false);

        // Set data
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.8f);
        setData(barData);

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
        animateY(800);
        invalidate();
    }

    /**
     * Get color based on wind speed
     */
    private int getWindColor(float windSpeed) {
        if (windSpeed < 10) return WIND_LIGHT_COLOR;
        else if (windSpeed < 20) return WIND_MODERATE_COLOR;
        else if (windSpeed < 30) return WIND_STRONG_COLOR;
        else return WIND_VERY_STRONG_COLOR;
    }
}
