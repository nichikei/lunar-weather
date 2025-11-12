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

import java.util.ArrayList;
import java.util.List;

/**
 * Custom AreaChart for Humidity 24-hour trend
 * Shows humidity percentage with gradient area fill
 */
public class HumidityAreaChart extends LineChart {

    private MicroChartView chartHelper;
    private static final int HUMIDITY_COLOR = 0xFF2196F3; // Blue

    public HumidityAreaChart(Context context) {
        super(context);
    }

    public HumidityAreaChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HumidityAreaChart(Context context, AttributeSet attrs, int defStyle) {
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
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        YAxis rightAxis = getAxisRight();
        chartHelper.configureYAxis(rightAxis);

        setMinimumHeight(140);
    }

    /**
     * Set humidity data for 24-hour period
     * @param humidityValues List of humidity percentages (0-100)
     * @param hourLabels List of hour labels
     */
    public void setHumidityData(List<Float> humidityValues, List<String> hourLabels) {
        if (humidityValues == null || humidityValues.isEmpty()) {
            clear();
            return;
        }

        // Create entries
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < humidityValues.size(); i++) {
            entries.add(new Entry(i, humidityValues.get(i)));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Humidity");
        
        // Line style
        dataSet.setColor(HUMIDITY_COLOR);
        dataSet.setLineWidth(2.5f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        
        // Smooth curve
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        
        // Gradient fill
        dataSet.setDrawFilled(true);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.humidity_gradient);
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
     * Update humidity value at specific hour
     */
    public void updateHumidityValue(int hourIndex, float humidityValue) {
        if (getData() != null && getData().getDataSetCount() > 0) {
            LineDataSet dataSet = (LineDataSet) getData().getDataSetByIndex(0);
            Entry entry = dataSet.getEntryForIndex(hourIndex);
            if (entry != null) {
                entry.setY(humidityValue);
                getData().notifyDataChanged();
                notifyDataSetChanged();
                invalidate();
            }
        }
    }
}
