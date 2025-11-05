package com.example.weatherapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if ("com.example.weatherapp.ACTION_REFRESH_WEATHER".equals(action)) {
            // Trigger immediate weather update
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(WeatherNotificationWorker.class).build();
            WorkManager.getInstance(context).enqueue(workRequest);

            Toast.makeText(context, "Refreshing weather...", Toast.LENGTH_SHORT).show();
        }
    }
}


