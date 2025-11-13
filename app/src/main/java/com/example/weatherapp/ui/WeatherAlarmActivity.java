package com.example.weatherapp.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.domain.model.WeatherAlarm;
import com.example.weatherapp.domain.services.WeatherAlarmService;
import com.example.weatherapp.utils.AlarmScheduler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class WeatherAlarmActivity extends AppCompatActivity implements WeatherAlarmAdapter.OnAlarmInteractionListener {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabAddAlarm;
    private ImageView btnBack;

    private WeatherAlarmAdapter adapter;
    private WeatherAlarmService alarmService;
    private AlarmScheduler alarmScheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_alarm);

        // Initialize services
        alarmService = new WeatherAlarmService(this);
        alarmScheduler = new AlarmScheduler(this);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Check exact alarm permission
        checkExactAlarmPermission();

        // Load alarms
        loadAlarms();

        // Setup listeners
        setupListeners();
    }

    private void checkExactAlarmPermission() {
        if (!alarmScheduler.canScheduleExactAlarms()) {
            showPermissionDialog();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Weather Alarms needs permission to schedule exact alarms. Please enable 'Alarms & reminders' in Settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewAlarms);
        emptyStateLayout = findViewById(R.id.layoutEmptyState);
        fabAddAlarm = findViewById(R.id.fabAddAlarm);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new WeatherAlarmAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        fabAddAlarm.setOnClickListener(v -> showAddAlarmDialog());
        
        // iOS-style add button in nav bar (same as FAB)
        View btnAddAlarmNav = findViewById(R.id.btnAddAlarmNav);
        if (btnAddAlarmNav != null) {
            btnAddAlarmNav.setOnClickListener(v -> showAddAlarmDialog());
        }
    }

    private void showAddAlarmDialog() {
        // Check permission first
        if (!alarmScheduler.canScheduleExactAlarms()) {
            showPermissionDialog();
            return;
        }

        com.example.weatherapp.ui.dialogs.AddAlarmBottomSheet bottomSheet = 
                com.example.weatherapp.ui.dialogs.AddAlarmBottomSheet.newInstance();
        bottomSheet.setListener(alarm -> {
            alarmService.saveAlarm(alarm);
            alarmScheduler.scheduleAlarm(alarm);
            loadAlarms();
            Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getSupportFragmentManager(), "AddAlarmBottomSheet");
    }

    private void loadAlarms() {
        List<WeatherAlarm> alarms = alarmService.getAllAlarms();
        adapter.setAlarms(alarms);

        // Show/hide empty state
        if (alarms.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAlarmToggled(WeatherAlarm alarm, boolean enabled) {
        // Check permission when enabling alarm
        if (enabled && !alarmScheduler.canScheduleExactAlarms()) {
            showPermissionDialog();
            loadAlarms(); // Reload to reset switch
            return;
        }

        alarm.setEnabled(enabled);
        alarmService.saveAlarm(alarm);

        if (enabled) {
            alarmScheduler.scheduleAlarm(alarm);
            Toast.makeText(this, "Alarm enabled", Toast.LENGTH_SHORT).show();
        } else {
            alarmScheduler.cancelAlarm(alarm);
            Toast.makeText(this, "Alarm disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAlarmDeleted(WeatherAlarm alarm) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Alarm")
                .setMessage("Are you sure you want to delete this alarm?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    alarmService.deleteAlarm(alarm.getId());
                    alarmScheduler.cancelAlarm(alarm);
                    loadAlarms();
                    Toast.makeText(this, "Alarm deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onAlarmClicked(WeatherAlarm alarm) {
        // TODO: Show Edit Alarm Dialog/BottomSheet
        Toast.makeText(this, "Edit Alarm - Coming Soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlarms();
    }
}
