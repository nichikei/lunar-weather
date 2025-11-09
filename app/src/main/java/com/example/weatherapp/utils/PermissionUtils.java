package com.example.weatherapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class để xử lý quyền (permissions)
 */
public class PermissionUtils {

    // Permission request codes
    public static final int REQUEST_LOCATION_PERMISSION = 100;
    public static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    public static final int REQUEST_CAMERA_PERMISSION = 102;
    public static final int REQUEST_STORAGE_PERMISSION = 103;

    /**
     * Kiểm tra quyền truy cập vị trí
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Yêu cầu quyền truy cập vị trí
     */
    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            REQUEST_LOCATION_PERMISSION);
    }

    /**
     * Kiểm tra quyền thông báo (Android 13+)
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Không cần permission cho Android < 13
    }

    /**
     * Yêu cầu quyền thông báo (Android 13+)
     */
    public static void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATION_PERMISSION);
        }
    }

    /**
     * Kiểm tra quyền camera
     */
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Yêu cầu quyền camera
     */
    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.CAMERA},
            REQUEST_CAMERA_PERMISSION);
    }

    /**
     * Kiểm tra user đã từ chối vĩnh viễn quyền chưa
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Kiểm tra một permission bất kỳ
     */
    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Kiểm tra nhiều permissions
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Yêu cầu nhiều permissions
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    // Prevent instantiation
    private PermissionUtils() {
        throw new AssertionError("Cannot instantiate PermissionUtils class");
    }
}

