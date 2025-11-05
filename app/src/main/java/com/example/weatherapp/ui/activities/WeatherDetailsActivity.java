package com.example.weatherapp.ui.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;

public class WeatherDetailsActivity extends AppCompatActivity {

    private RecyclerView rvMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        rvMetrics = findViewById(R.id.rvMetrics);

        // Setup GridLayoutManager with 2 columns
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Setup code for RecyclerView
        rvMetrics.setLayoutManager(new GridLayoutManager(this, 2));
        // Other setup code...
    }
}

