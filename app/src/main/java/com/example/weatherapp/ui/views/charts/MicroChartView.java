package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

/**
 * Base class for micro-charts in weather detail cards
 * Provides common configuration for minimal, clean charts
 */
public abstract class MicroChartView {

    /**
     * Configure common chart properties for micro-charts
     * - No legend
     * - No description
     * - Minimal padding
     * - Disabled interactions (zoom, scroll) by default
     * - Transparent background
     */
    protected void configureBaseChart(Chart<?> chart) {
        // Disable description and legend
        chart.getDescription().setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setEnabled(false);

        // Minimal padding
        chart.setExtraOffsets(0, 0, 0, 0);

        // Disable interactions by default (can be overridden)
        chart.setTouchEnabled(false);

        // Transparent background
        chart.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Configure X-axis for micro-charts
     * - Position at bottom
     * - No grid lines
     * - Custom text color
     */
    protected void configureXAxis(XAxis xAxis, int textColor) {
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(9f);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
    }

    /**
     * Configure Y-axis for micro-charts
     * - No grid lines
     * - No labels (minimal design)
     */
    protected void configureYAxis(YAxis yAxis) {
        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setDrawLabels(false);
        yAxis.setEnabled(false);
    }

    /**
     * Get color with alpha transparency
     */
    protected int getColorWithAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Create gradient colors array for charts
     */
    protected int[] createGradient(int startColor, int endColor) {
        return new int[]{startColor, endColor};
    }
}
