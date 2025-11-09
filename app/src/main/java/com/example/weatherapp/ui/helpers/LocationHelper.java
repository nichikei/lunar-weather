package com.example.weatherapp.ui.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.weatherapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;

import java.util.Locale;

/**
 * Helper class for location/GPS operations
 * Handles permission checking and location fetching
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    
    private final Activity activity;
    private final FusedLocationProviderClient fusedLocationClient;
    private LocationCallback currentLocationCallback;

    public interface OnLocationResultListener {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String message);
    }

    public LocationHelper(Activity activity, FusedLocationProviderClient fusedLocationClient) {
        this.activity = activity;
        this.fusedLocationClient = fusedLocationClient;
    }

    /**
     * Check if location permission is granted
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Get current location
     */
    public void getCurrentLocation(OnLocationResultListener callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        Toast.makeText(activity, R.string.getting_location, Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, location -> {
                    if (location != null) {
                        float accuracy = location.getAccuracy();

                        // If location is too inaccurate, request fresh location
                        if (accuracy > 1000) {
                            Toast.makeText(activity, "Location not accurate, requesting fresh location...", 
                                    Toast.LENGTH_SHORT).show();
                            requestFreshLocation(callback);
                            return;
                        }

                        // Debug: Show coordinates
                        String coordsMsg = String.format(Locale.getDefault(),
                                "Location: %.4f, %.4f (±%.0fm)",
                                location.getLatitude(), location.getLongitude(), accuracy);
                        Toast.makeText(activity, coordsMsg, Toast.LENGTH_LONG).show();

                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        // Last location not available, request fresh location
                        requestFreshLocation(callback);
                    }
                })
                .addOnFailureListener(activity, e -> {
                    Log.e(TAG, "Failed to get location", e);
                    callback.onLocationError(activity.getString(R.string.location_not_available));
                });
    }

    /**
     * Request fresh location when last location is not available
     */
    private void requestFreshLocation(OnLocationResultListener callback) {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        Toast.makeText(activity, "Requesting fresh location...", Toast.LENGTH_SHORT).show();

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdates(1)
                .build();

        currentLocationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    float accuracy = location.getAccuracy();

                    // Debug: Show coordinates
                    String coordsMsg = String.format(Locale.getDefault(),
                            "Fresh location: %.4f, %.4f (±%.0fm)",
                            location.getLatitude(), location.getLongitude(), accuracy);
                    Toast.makeText(activity, coordsMsg, Toast.LENGTH_LONG).show();

                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    
                    // Stop location updates
                    fusedLocationClient.removeLocationUpdates(this);
                } else {
                    callback.onLocationError("Unable to get your location. Please enable location services.");
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, currentLocationCallback, 
                activity.getMainLooper());
    }

    /**
     * Cleanup location updates
     */
    public void cleanup() {
        if (currentLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(currentLocationCallback);
        }
    }
}
