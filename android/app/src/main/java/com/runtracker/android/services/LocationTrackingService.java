package com.runtracker.android.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Service for tracking user location during runs
 */
public class LocationTrackingService {

    private static final String TAG = "LocationTrackingService";
    private static final long UPDATE_INTERVAL = 5000; // 5 seconds
    private static final long FASTEST_INTERVAL = 2000; // 2 seconds
    
    private final Context context;
    private final LocationCallback locationCallback;
    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationUpdateListener locationUpdateListener;
    private boolean isTracking = false;
    
    /**
     * Interface for location update callbacks
     */
    public interface LocationUpdateListener {
        void onLocationUpdate(double latitude, double longitude);
    }
    
    /**
     * Constructor
     * @param context Application context
     * @param locationUpdateListener Listener for location updates
     */
    public LocationTrackingService(Context context, LocationUpdateListener locationUpdateListener) {
        this.context = context;
        this.locationUpdateListener = locationUpdateListener;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        
                        // Notify listener
                        locationUpdateListener.onLocationUpdate(latitude, longitude);
                    }
                }
            }
        };
    }
    
    /**
     * Start tracking location
     */
    @SuppressLint("MissingPermission")
    public void startTracking() {
        if (isTracking) {
            return;
        }
        
        // Check if we have location permission
        if (ContextCompat.checkSelfPermission(context, 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted");
            return;
        }
        
        // Create location request
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .build();
        
        // Start location updates
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper());
        
        isTracking = true;
        Log.d(TAG, "Started location tracking");
    }
    
    /**
     * Stop tracking location
     */
    public void stopTracking() {
        if (!isTracking) {
            return;
        }
        
        // Remove location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
        Log.d(TAG, "Stopped location tracking");
    }
    
    /**
     * Check if tracking is active
     * @return True if tracking is active, false otherwise
     */
    public boolean isTracking() {
        return isTracking;
    }
}