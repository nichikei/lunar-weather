package com.example.weatherapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.ui.adapters.IOSHourlyForecastAdapter;
import com.example.weatherapp.ui.adapters.IOSDailyForecastAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * iOS Style Weather Activity
 * Mimics Apple's Weather App design from iOS
 */
public class IOSWeatherActivity extends AppCompatActivity {

    private RecyclerView rvHourlyForecast;
    private RecyclerView rvDailyForecast;
    
    private IOSHourlyForecastAdapter hourlyAdapter;
    private IOSDailyForecastAdapter dailyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ios_style);
        
        // Setup edge-to-edge display
        setupEdgeToEdge();
        
        // Initialize views
        initViews();
        
        // Setup adapters
        setupAdapters();
        
        // Load sample data
        loadSampleData();
    }

    private void setupEdgeToEdge() {
        // Make app fullscreen with transparent status bar
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Set status bar and navigation bar to transparent
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        
        // Make status bar icons light colored
        WindowInsetsControllerCompat windowInsetsController =
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(false);
            windowInsetsController.setAppearanceLightNavigationBars(false);
        }
    }

    private void initViews() {
        rvHourlyForecast = findViewById(R.id.rvHourlyForecast);
        rvDailyForecast = findViewById(R.id.rvDailyForecast);
        
        // Setup horizontal scroll for hourly forecast
        LinearLayoutManager hourlyLayoutManager = new LinearLayoutManager(
            this, 
            LinearLayoutManager.HORIZONTAL, 
            false
        );
        rvHourlyForecast.setLayoutManager(hourlyLayoutManager);
        
        // Setup vertical scroll for daily forecast
        LinearLayoutManager dailyLayoutManager = new LinearLayoutManager(this);
        rvDailyForecast.setLayoutManager(dailyLayoutManager);
        rvDailyForecast.setNestedScrollingEnabled(false);
    }

    private void setupAdapters() {
        hourlyAdapter = new IOSHourlyForecastAdapter();
        dailyAdapter = new IOSDailyForecastAdapter();
        
        rvHourlyForecast.setAdapter(hourlyAdapter);
        rvDailyForecast.setAdapter(dailyAdapter);
    }

    private void loadSampleData() {
        // Sample hourly data
        List<HourlyForecast> hourlyData = new ArrayList<>();
        hourlyData.add(new HourlyForecast("Now", "􀇛", 21));
        hourlyData.add(new HourlyForecast("10PM", "􀇅", 21));
        hourlyData.add(new HourlyForecast("11PM", "􀇉", 19));
        hourlyData.add(new HourlyForecast("12AM", "􀇓", 19));
        hourlyData.add(new HourlyForecast("1AM", "􀇗", 19));
        hourlyData.add(new HourlyForecast("2AM", "􀇗", 18));
        hourlyData.add(new HourlyForecast("3AM", "􀇗", 18));
        hourlyData.add(new HourlyForecast("4AM", "􀇗", 17));
        
        hourlyAdapter.setData(hourlyData);
        
        // Sample daily data
        List<DailyForecast> dailyData = new ArrayList<>();
        dailyData.add(new DailyForecast("Today", "􀆮", 15, 29, 0, true));
        dailyData.add(new DailyForecast("Mon", "􀇓", 18, 27, 60, false));
        dailyData.add(new DailyForecast("Tue", "􀇓", 20, 25, 50, false));
        dailyData.add(new DailyForecast("Wed", "􀇓", 17, 25, 70, false));
        dailyData.add(new DailyForecast("Thu", "􀇓", 17, 25, 50, false));
        dailyData.add(new DailyForecast("Fri", "􀇕", 20, 26, 0, false));
        dailyData.add(new DailyForecast("Sat", "􀆮", 18, 25, 0, false));
        dailyData.add(new DailyForecast("Sun", "􀇓", 13, 21, 50, false));
        dailyData.add(new DailyForecast("Mon", "􀇓", 12, 19, 50, false));
        dailyData.add(new DailyForecast("Tue", "􀆮", 18, 25, 0, false));
        
        dailyAdapter.setData(dailyData);
    }

    // Inner classes for data models
    public static class HourlyForecast {
        public String time;
        public String icon;
        public int temperature;

        public HourlyForecast(String time, String icon, int temperature) {
            this.time = time;
            this.icon = icon;
            this.temperature = temperature;
        }
    }

    public static class DailyForecast {
        public String day;
        public String icon;
        public int lowTemp;
        public int highTemp;
        public int rainProbability;
        public boolean isToday;

        public DailyForecast(String day, String icon, int lowTemp, int highTemp, 
                           int rainProbability, boolean isToday) {
            this.day = day;
            this.icon = icon;
            this.lowTemp = lowTemp;
            this.highTemp = highTemp;
            this.rainProbability = rainProbability;
            this.isToday = isToday;
        }
    }
}
