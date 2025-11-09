package com.example.weatherapp.ui.helpers;

import com.example.weatherapp.data.responses.HourlyForecastResponse;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Helper class for chart configuration and data preparation
 * Reduces code duplication in chart setup
 */
public class ChartHelper {
    
    /**
     * Chart color scheme configuration
     */
    public static class ChartColors {
        public int lineColor;
        public int circleColor;
        public int circleHoleColor;
        public int fillColor;
        
        public ChartColors(int lineColor, int circleColor, int circleHoleColor, int fillColor) {
            this.lineColor = lineColor;
            this.circleColor = circleColor;
            this.circleHoleColor = circleHoleColor;
            this.fillColor = fillColor;
        }
        
        // Predefined color schemes
        public static ChartColors TEMPERATURE = new ChartColors(0xFF9B6FFF, 0xFFE2DDFD, 0xFF5B3E9E, 0xFF7B5EC6);
        public static ChartColors RAIN = new ChartColors(0xFF4FC3F7, 0xFF81D4FA, 0xFF29B6F6, 0xFF4FC3F7);
        public static ChartColors WIND = new ChartColors(0xFF66BB6A, 0xFF81C784, 0xFF4CAF50, 0xFF66BB6A);
        public static ChartColors HUMIDITY = new ChartColors(0xFF26C6DA, 0xFF4DD0E1, 0xFF00BCD4, 0xFF26C6DA);
    }
    
    /**
     * Interface for extracting values from hourly forecast items
     */
    public interface ValueExtractor {
        float getValue(HourlyForecastResponse.HourlyItem item);
    }
    
    /**
     * Prepare chart entries from hourly forecast data
     */
    public static List<Entry> prepareChartEntries(HourlyForecastResponse forecastData, 
                                                   int maxCount,
                                                   ValueExtractor extractor) {
        List<Entry> entries = new ArrayList<>();
        if (forecastData == null || forecastData.getList() == null) {
            return entries;
        }
        
        List<HourlyForecastResponse.HourlyItem> list = forecastData.getList();
        int count = Math.min(maxCount, list.size());
        
        for (int i = 0; i < count; i++) {
            float value = extractor.getValue(list.get(i));
            entries.add(new Entry(i, value));
        }
        
        return entries;
    }
    
    /**
     * Configure LineDataSet with standard styling
     */
    public static void styleLineDataSet(LineDataSet dataSet, ChartColors colors) {
        dataSet.setColor(colors.lineColor);
        dataSet.setCircleColor(colors.circleColor);
        dataSet.setLineWidth(3.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(colors.circleHoleColor);
        dataSet.setCircleHoleRadius(2.5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(colors.fillColor);
        dataSet.setFillAlpha(100);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(0xFFFFFFFF);
    }
    
    /**
     * Configure temperature chart with larger circles
     */
    public static void styleTemperatureDataSet(LineDataSet dataSet, ChartColors colors) {
        styleLineDataSet(dataSet, colors);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(3f);
    }
    
    /**
     * Setup basic line chart configuration
     */
    public static void setupLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        
        // X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(0xCCFFFFFF);
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(0x40FFFFFF);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setGranularity(1f);
        xAxis.setAvoidFirstLastClipping(true);
        
        // Y Axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextColor(0xCCFFFFFF);
        yAxis.setTextSize(11f);
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(0x30FFFFFF);
        yAxis.setGridLineWidth(1f);
        yAxis.setDrawAxisLine(false);
        
        // Interaction
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        
        chart.setExtraOffsets(8, 16, 8, 8);
    }
    
    /**
     * Setup bar chart configuration
     */
    public static void setupBarChart(BarChart chart, String[] labels) {
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getAxisRight().setEnabled(false);
        
        // X Axis with labels
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(0xCCFFFFFF);
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(0x40FFFFFF);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setGranularity(1f);
        
        if (labels != null) {
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    return index >= 0 && index < labels.length ? labels[index] : "";
                }
            });
        }
        
        // Y Axis
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setTextColor(0xCCFFFFFF);
        yAxis.setTextSize(11f);
        yAxis.setDrawGridLines(true);
        yAxis.setGridColor(0x30FFFFFF);
        yAxis.setGridLineWidth(1f);
        yAxis.setDrawAxisLine(false);
        
        chart.setTouchEnabled(false);
        chart.setFitBars(true);
        chart.setExtraOffsets(8, 16, 8, 8);
    }
    
    /**
     * Create time formatter for X axis
     */
    public static ValueFormatter createTimeFormatter(HourlyForecastResponse forecastData, int count) {
        final List<HourlyForecastResponse.HourlyItem> list = forecastData.getList();
        final int tzSec = (forecastData.getCity() != null) 
                ? forecastData.getCity().getTimezone() : 0;
        
        return new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index < 0 || index >= count || index >= list.size()) {
                    return "";
                }
                
                long tsMs = list.get(index).getDt() * 1000L;
                long localMs = tsMs + tzSec * 1000L;
                
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                cal.setTimeInMillis(localMs);
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                
                return (hour < 10 ? "0" + hour : String.valueOf(hour)) + "h";
            }
        };
    }
    
    /**
     * Apply time formatter to chart
     */
    public static void applyTimeFormatter(LineChart chart, HourlyForecastResponse forecastData, int count) {
        chart.getXAxis().setLabelCount(count, true);
        chart.getXAxis().setValueFormatter(createTimeFormatter(forecastData, count));
    }
    
    /**
     * Set Y axis range
     */
    public static void setYAxisRange(LineChart chart, float min, float max) {
        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(min);
        yAxis.setAxisMaximum(max);
    }
}
