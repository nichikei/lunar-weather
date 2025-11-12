package com.example.weatherapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

/**
 * Helper class to get device location
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    private final Context context;
    
    public LocationHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Get last known location
     * @return [latitude, longitude] or null if unavailable
     */
    public double[] getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            return null;
        }
        
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager == null) {
                return null;
            }
            
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            if (location != null) {
                return new double[]{location.getLatitude(), location.getLongitude()};
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
        }
        
        return null;
    }
}
