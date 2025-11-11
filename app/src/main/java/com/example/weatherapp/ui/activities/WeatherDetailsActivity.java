package com.example.weatherapp.ui.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.presentation.viewmodel.WeatherDetailsViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class WeatherDetailsActivity extends AppCompatActivity {

    private WeatherDetailsViewModel viewModel;
    
    private RecyclerView rvMetrics;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private TextView tvCityName, tvMainTemp, tvWeatherCondition, tvTempRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(WeatherDetailsViewModel.class);

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup collapsing toolbar
        setupCollapsingToolbar();

        // Setup GridLayoutManager with 2 columns
        setupRecyclerView();

        // Setup AppBar scroll listener for animations
        setupAppBarScrollListener();
        
        // Setup observers
        setupObservers();

        // Load weather data from intent
        loadWeatherData();
    }

    private void initViews() {
        rvMetrics = findViewById(R.id.rvMetrics);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        tvCityName = findViewById(R.id.tvCityName);
        tvMainTemp = findViewById(R.id.tvMainTemp);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        tvTempRange = findViewById(R.id.tvTempRange);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupCollapsingToolbar() {
        collapsingToolbar.setTitle("");
        collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
    }

    private void setupRecyclerView() {
        rvMetrics.setLayoutManager(new GridLayoutManager(this, 2));
        // Other setup code...
    }

    private void setupAppBarScrollListener() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                // Calculate collapse percentage
                float percentage = (float) Math.abs(verticalOffset) / (float) scrollRange;

                // When fully collapsed, show city name in toolbar
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(tvCityName.getText().toString());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle("");
                    isShow = false;
                }

                // Custom animations based on scroll
                animateViews(percentage);
            }
        });
    }

    private void animateViews(float percentage) {
        // Fade out weather info card as we scroll
        float alpha = 1.0f - percentage;
        tvMainTemp.setAlpha(alpha);
        tvWeatherCondition.setAlpha(alpha);
        tvTempRange.setAlpha(alpha);
    }

    private void setupObservers() {
        viewModel.getWeatherState().observe(this, state -> {
            if (state != null) {
                tvCityName.setText(state.cityName);
                tvMainTemp.setText(state.temperature + "°");
                tvWeatherCondition.setText(state.condition);
                tvTempRange.setText(state.tempRange);
            }
        });
    }
    
    private void loadWeatherData() {
        // Get data from intent and pass to ViewModel
        if (getIntent() != null) {
            String cityName = getIntent().getStringExtra("city_name");
            String temp = getIntent().getStringExtra("temperature");
            String condition = getIntent().getStringExtra("condition");
            String tempRange = getIntent().getStringExtra("temp_range");

            viewModel.loadWeatherDetails(cityName, temp, condition, tempRange);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

