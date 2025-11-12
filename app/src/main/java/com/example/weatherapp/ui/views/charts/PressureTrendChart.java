package com.example.weatherapp.ui.views.charts;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom LineChart for Barometric Pressure trend
 * Shows pressure changes with color zones (low/normal/high)
 */
public class PressureTrendChart extends LineChart {

    private MicroChartView chartHelper;
    private static final int PRESSURE_COLOR = 0xFF9C27B0; // Purple
    private static final float NORMAL_PRESSURE_LOW = 1013f;  // hPa
    private static final float NORMAL_PRESSURE_HIGH = 1020f; // hPa

    public PressureTrendChart(Context context) {
        super(context);
    }

    public PressureTrendChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PressureTrendChart(Context context, AttributeSet attrs, int defStyle) {
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
        leftAxis.setAxisMinimum(980f);
        leftAxis.setAxisMaximum(1050f);
        
        // Add limit lines for normal pressure zone
        LimitLine lowLine = new LimitLine(NORMAL_PRESSURE_LOW, "");
        lowLine.setLineWidth(1f);
        lowLine.setLineColor(Color.parseColor("#40FFFFFF"));
        lowLine.enableDashedLine(10f, 5f, 0f);
        leftAxis.addLimitLine(lowLine);
        
        LimitLine highLine = new LimitLine(NORMAL_PRESSURE_HIGH, "");
        highLine.setLineWidth(1f);
        highLine.setLineColor(Color.parseColor("#40FFFFFF"));
        highLine.enableDashedLine(10f, 5f, 0f);
        leftAxis.addLimitLine(highLine);

        YAxis rightAxis = getAxisRight();
        chartHelper.configureYAxis(rightAxis);

        setMinimumHeight(140);
    }

    /**
     * Set pressure data
     * @param pressureValues List of pressure values in hPa
     * @param hourLabels List of hour labels
     */
    public void setPressureData(List<Float> pressureValues, List<String> hourLabels) {
        if (pressureValues == null || pressureValues.isEmpty()) {
            clear();
            return;
        }

        // Create entries
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < pressureValues.size(); i++) {
            entries.add(new Entry(i, pressureValues.get(i)));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Pressure");
        
        // Line style
        dataSet.setColor(PRESSURE_COLOR);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(PRESSURE_COLOR);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setDrawValues(false);
        
        // Smooth curve
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        
        // No fill for pressure
        dataSet.setDrawFilled(false);

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
     * Get pressure status based on value
     */
    public String getPressureStatus(float pressure) {
        if (pressure < NORMAL_PRESSURE_LOW) {
            return "Low Pressure";
        } else if (pressure > NORMAL_PRESSURE_HIGH) {
            return "High Pressure";
        } else {
            return "Normal";
        }
    }
}
