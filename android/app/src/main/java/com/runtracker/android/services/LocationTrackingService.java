package com.runtracker.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.runtracker.android.R;
import com.runtracker.android.ui.MainActivity;

/**
 * Service for tracking user's location in the background
 */
public class LocationTrackingService extends Service {

    public static final String ACTION_START_TRACKING_SERVICE = "ACTION_START_TRACKING_SERVICE";
    public static final String ACTION_STOP_TRACKING_SERVICE = "ACTION_STOP_TRACKING_SERVICE";
    public static final String ACTION_LOCATION_BROADCAST = "ACTION_LOCATION_BROADCAST";
    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";
    
    private static final String NOTIFICATION_CHANNEL_ID = "location_tracking_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private static final long UPDATE_INTERVAL = 5000; // 5 seconds
    private static final long FASTEST_INTERVAL = 2000; // 2 seconds
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean isTracking = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create location request
        locationRequest = LocationRequest.create()
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    broadcastLocation(location);
                }
            }
        };
        
        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            
            if (ACTION_START_TRACKING_SERVICE.equals(action)) {
                startLocationTracking();
            } else if (ACTION_STOP_TRACKING_SERVICE.equals(action)) {
                stopLocationTracking();
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    private void startLocationTracking() {
        if (isTracking) {
            return;
        }
        
        // Create notification channel
        createNotificationChannel();
        
        // Create notification
        Notification notification = createNotification();
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, notification);
        
        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper());
            isTracking = true;
        } catch (SecurityException securityException) {
            // Permission has been revoked
            stopSelf();
        }
    }
    
    private void stopLocationTracking() {
        if (!isTracking) {
            return;
        }
        
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isTracking = false;
        
        // Stop service
        stopForeground(true);
        stopSelf();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        // Create an intent to open the app when notification is tapped
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        
        // Build the notification
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Run Tracker")
                .setContentText("Tracking your run...")
                .setSmallIcon(R.drawable.ic_run)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
    
    private void broadcastLocation(Location location) {
        Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    
    /**
     * Helper method to start the service
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction(ACTION_START_TRACKING_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
    
    /**
     * Helper method to stop the service
     */
    public static void stopService(Context context) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        intent.setAction(ACTION_STOP_TRACKING_SERVICE);
        context.startService(intent);
    }
}