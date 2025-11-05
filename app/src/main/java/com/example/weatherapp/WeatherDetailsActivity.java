package com.example.weatherapp;

import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeatherDetailsActivity extends AppCompatActivity {

    private RecyclerView rvMetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        rvMetrics = findViewById(R.id.rvMetrics);

        // KHÔNG apply blur để UI rõ ràng
        // Glass panel background đã đủ đẹp mà không cần blur
        /* DISABLED - causes blur
        View panel = findViewById(R.id.panel);
        if (panel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            RenderEffect blurEffect = RenderEffect.createBlurEffect(
                    10f, 10f, Shader.TileMode.CLAMP
            );
            panel.setRenderEffect(blurEffect);
        }
        */

        // Setup GridLayoutManager with 2 columns
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Setup code for RecyclerView
        rvMetrics.setLayoutManager(new GridLayoutManager(this, 2));
        // Other setup code...
    }
}
