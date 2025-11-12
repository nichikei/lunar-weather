package com.example.weatherapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.Toast;

import com.example.weatherapp.domain.model.ActivitySuggestion;

import java.util.Calendar;

/**
 * Helper class for Calendar Integration
 */
public class CalendarHelper {

    /**
     * Add activity to device calendar
     */
    public static void addActivityToCalendar(Context context, ActivitySuggestion activity) {
        try {
            // Parse best time to get start time
            Calendar startTime = Calendar.getInstance();
            String bestTime = activity.getBestTime();
            
            // Set time based on bestTime string
            if (bestTime.toLowerCase().contains("morning")) {
                startTime.set(Calendar.HOUR_OF_DAY, 8);
                startTime.set(Calendar.MINUTE, 0);
            } else if (bestTime.toLowerCase().contains("afternoon")) {
                startTime.set(Calendar.HOUR_OF_DAY, 14);
                startTime.set(Calendar.MINUTE, 0);
            } else if (bestTime.toLowerCase().contains("evening")) {
                startTime.set(Calendar.HOUR_OF_DAY, 18);
                startTime.set(Calendar.MINUTE, 0);
            } else {
                // Default to current time + 1 hour
                startTime.add(Calendar.HOUR_OF_DAY, 1);
            }
            
            // End time (1 hour later)
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.HOUR_OF_DAY, 1);
            
            // Create event description
            String description = activity.getDescription() + "\n\n" +
                               "Reason: " + activity.getReason() + "\n" +
                               "Suitability Score: " + activity.getSuitabilityScore() + "%\n" +
                               "Category: " + activity.getCategory();
            
            // Use Intent to add to calendar (works on all Android versions)
            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE, activity.getIcon() + " " + activity.getTitle())
                    .putExtra(CalendarContract.Events.DESCRIPTION, description)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTimeInMillis())
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, "")
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
            
            context.startActivity(intent);
            
        } catch (Exception e) {
            Toast.makeText(context, "Error adding to calendar: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Check if calendar permission is granted
     */
    public static boolean hasCalendarPermission(Context context) {
        return context.checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) 
               == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Open calendar app
     */
    public static void openCalendar(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("content://com.android.calendar/time"));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot open calendar app", Toast.LENGTH_SHORT).show();
        }
    }
}
