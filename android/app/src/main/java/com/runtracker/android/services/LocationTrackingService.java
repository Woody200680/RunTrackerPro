package com.runtracker.android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.runtracker.android.R;
import com.runtracker.android.data.CoachingManager;
import com.runtracker.android.data.models.CoachingWorkout;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for tracking user location during runs
 */
public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    private static final String CHANNEL_ID = "location_tracking_channel";
    private static final int NOTIFICATION_ID = 1;
    
    // Actions
    public static final String ACTION_START = "com.runtracker.android.ACTION_START_TRACKING";
    public static final String ACTION_STOP = "com.runtracker.android.ACTION_STOP_TRACKING";
    public static final String ACTION_PAUSE = "com.runtracker.android.ACTION_PAUSE_TRACKING";
    public static final String ACTION_RESUME = "com.runtracker.android.ACTION_RESUME_TRACKING";
    
    // Location tracking
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    
    // Run state
    private boolean isTracking = false;
    private boolean isPaused = false;
    private long startTimeMillis;
    private long pauseTimeMillis;
    private long totalPausedTime = 0;
    private final List<Location> locations = new ArrayList<>();
    private final List<Run.PauseInterval> pauseIntervals = new ArrayList<>();
    
    // Dependencies
    private RunRepository runRepository;
    private AudioCueManager audioCueManager;
    private VoiceCoach voiceCoach;
    private CoachingManager coachingManager;
    
    // Current run
    private Run currentRun;
    
    // Coaching
    private int coachingType = Constants.COACHING_TYPE_BASIC;
    private CoachingWorkout activeWorkout;
    
    // Binder for activity communication
    private final IBinder binder = new LocalBinder();
    
    // Listeners
    private final List<LocationUpdateListener> locationUpdateListeners = new ArrayList<>();
    
    /**
     * Interface for listening to location updates
     */
    public interface LocationUpdateListener {
        void onLocationUpdate(Location location, Run run);
    }
    
    /**
     * Binder for activity communication
     */
    public class LocalBinder extends Binder {
        public LocationTrackingService getService() {
            return LocationTrackingService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Create notification channel for foreground service
        createNotificationChannel();
        
        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        createLocationCallback();
        
        // Get repositories and services
        if (getApplication() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getApplication();
            runRepository = mainActivity.getRunRepository();
            audioCueManager = mainActivity.getAudioCueManager();
            voiceCoach = mainActivity.getVoiceCoach();
            coachingManager = mainActivity.getCoachingManager();
        } else {
            // Fallback if service is not started from MainActivity
            runRepository = new RunRepository(this);
            audioCueManager = new AudioCueManager(this);
            voiceCoach = new VoiceCoach(this);
            coachingManager = new CoachingManager(this);
        }
        
        // Load coaching settings
        loadCoachingSettings();
    }
    
    /**
     * Load coaching settings from preferences
     */
    private void loadCoachingSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        coachingType = preferences.getInt(Constants.PREF_COACHING_TYPE, Constants.COACHING_TYPE_BASIC);
        
        // If workout coaching is enabled, get the active workout
        if (coachingType == Constants.COACHING_TYPE_WORKOUT) {
            String activePlanId = preferences.getString(Constants.PREF_ACTIVE_PLAN_ID, null);
            if (activePlanId != null) {
                // Get next scheduled workout
                activeWorkout = coachingManager.getNextScheduledWorkout();
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START:
                        startTracking();
                        break;
                    case ACTION_STOP:
                        stopTracking();
                        break;
                    case ACTION_PAUSE:
                        pauseTracking();
                        break;
                    case ACTION_RESUME:
                        resumeTracking();
                        break;
                }
            }
        }
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    /**
     * Create location request for high accuracy tracking
     */
    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_UPDATE_INTERVAL / 2);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    
    /**
     * Create location callback for processing location updates
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    processNewLocation(location);
                }
            }
        };
    }
    
    /**
     * Process a new location update
     * @param location The new location
     */
    private void processNewLocation(Location location) {
        if (!isTracking || isPaused) {
            return;
        }
        
        // Add location to the list
        locations.add(location);
        
        // Update current run with new location
        if (currentRun != null) {
            // Create location point
            Run.LocationPoint locationPoint = new Run.LocationPoint(
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    System.currentTimeMillis()
            );
            
            // Add to run
            currentRun.addLocationPoint(locationPoint);
            
            // Calculate stats (distance, pace, etc.)
            calculateRunStats();
            
            // Check for milestone audio cues
            if (audioCueManager != null) {
                audioCueManager.checkMilestones(
                        currentRun.getTotalDistance(), 
                        currentRun.getActiveDuration());
                
                audioCueManager.checkPeriodicCue(
                        currentRun.getTotalDistance(),
                        currentRun.getActiveDuration(),
                        currentRun.getPace());
            }
            
            // Update voice coaching
            updateVoiceCoaching();
            
            // Notify listeners
            for (LocationUpdateListener listener : locationUpdateListeners) {
                listener.onLocationUpdate(location, currentRun);
            }
            
            // Update notification
            updateNotification();
        }
    }
    
    /**
     * Calculate run statistics
     */
    private void calculateRunStats() {
        if (currentRun == null) {
            return;
        }
        
        // Calculate active duration (total time minus paused time)
        long currentTimeMillis = System.currentTimeMillis();
        long totalDuration = currentTimeMillis - startTimeMillis;
        long activeDuration = totalDuration - totalPausedTime;
        
        // Update run stats
        currentRun.setActiveDuration(activeDuration / 1000); // Convert to seconds
        
        // Total distance and pace are calculated in the Run model when adding location points
    }
    
    /**
     * Update voice coaching based on run progress
     */
    private void updateVoiceCoaching() {
        if (voiceCoach == null || currentRun == null || !isTracking) {
            return;
        }
        
        // Update based on coaching type
        if (coachingType == Constants.COACHING_TYPE_WORKOUT && activeWorkout != null) {
            // If this is a workout-based coaching, sync with workout
            voiceCoach.updateWorkoutCoaching();
        }
        // Basic coaching updates are handled internally by the VoiceCoach class
    }
    
    /**
     * Start location tracking
     */
    private void startTracking() {
        if (isTracking) {
            return;
        }
        
        // Clear previous data
        locations.clear();
        pauseIntervals.clear();
        totalPausedTime = 0;
        
        // Set tracking flags
        isTracking = true;
        isPaused = false;
        
        // Record start time
        startTimeMillis = System.currentTimeMillis();
        
        // Create a new run
        currentRun = new Run();
        currentRun.setStartTime(startTimeMillis);
        currentRun.setStatus(Run.STATUS_ACTIVE);
        
        // Save the initial run to get an ID
        runRepository.saveRun(currentRun);
        
        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest, locationCallback, Looper.getMainLooper());
            
            // Start foreground service with notification
            startForeground(NOTIFICATION_ID, createNotification());
            
            // Start audio cues
            if (audioCueManager != null) {
                audioCueManager.reset();
            }
            
            // Start voice coaching
            startVoiceCoaching();
            
            Log.d(TAG, "Location tracking started");
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }
    }
    
    /**
     * Stop location tracking
     */
    private void stopTracking() {
        if (!isTracking) {
            return;
        }
        
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
        
        // Calculate final stats
        if (currentRun != null) {
            calculateRunStats();
            
            // Mark run as completed
            currentRun.setStatus(Run.STATUS_COMPLETED);
            currentRun.setEndTime(System.currentTimeMillis());
            
            // Save to repository
            runRepository.saveRun(currentRun);
            
            // Check if this run was part of a workout
            if (coachingType == Constants.COACHING_TYPE_WORKOUT && activeWorkout != null) {
                // Mark workout as completed
                coachingManager.markWorkoutCompleted(activeWorkout.getId(), currentRun.getId());
            }
        }
        
        // Reset state
        isTracking = false;
        isPaused = false;
        currentRun = null;
        
        // Stop audio cues
        if (audioCueManager != null) {
            audioCueManager.reset();
        }
        
        // Stop voice coaching
        stopVoiceCoaching();
        
        // Stop foreground service
        stopForeground(true);
        stopSelf();
        
        Log.d(TAG, "Location tracking stopped");
    }
    
    /**
     * Pause location tracking
     */
    private void pauseTracking() {
        if (!isTracking || isPaused) {
            return;
        }
        
        isPaused = true;
        pauseTimeMillis = System.currentTimeMillis();
        
        // Create a new pause interval
        Run.PauseInterval pauseInterval = new Run.PauseInterval(pauseTimeMillis, 0);
        pauseIntervals.add(pauseInterval);
        
        // Update run status
        if (currentRun != null) {
            currentRun.setStatus(Run.STATUS_PAUSED);
            currentRun.addPauseInterval(pauseInterval);
            runRepository.saveRun(currentRun);
        }
        
        // Update notification
        updateNotification();
        
        Log.d(TAG, "Location tracking paused");
    }
    
    /**
     * Resume location tracking
     */
    private void resumeTracking() {
        if (!isTracking || !isPaused) {
            return;
        }
        
        // Calculate paused time
        long resumeTimeMillis = System.currentTimeMillis();
        long pauseDuration = resumeTimeMillis - pauseTimeMillis;
        totalPausedTime += pauseDuration;
        
        // Update the latest pause interval
        if (!pauseIntervals.isEmpty()) {
            Run.PauseInterval latestPause = pauseIntervals.get(pauseIntervals.size() - 1);
            latestPause.setEndTime(resumeTimeMillis);
            latestPause.setDuration(pauseDuration);
            
            // Update in the run
            if (currentRun != null) {
                currentRun.updatePauseInterval(pauseIntervals.size() - 1, latestPause);
            }
        }
        
        // Update state
        isPaused = false;
        
        // Update run status
        if (currentRun != null) {
            currentRun.setStatus(Run.STATUS_ACTIVE);
            runRepository.saveRun(currentRun);
        }
        
        // Update notification
        updateNotification();
        
        Log.d(TAG, "Location tracking resumed");
    }
    
    /**
     * Start voice coaching
     */
    private void startVoiceCoaching() {
        if (voiceCoach != null && currentRun != null) {
            // Determine coaching type
            if (coachingType == Constants.COACHING_TYPE_WORKOUT && activeWorkout != null) {
                // Start workout coaching
                voiceCoach.startCoaching(currentRun, VoiceCoach.COACHING_WORKOUT, activeWorkout);
            } else {
                // Start basic coaching
                voiceCoach.startCoaching(currentRun, VoiceCoach.COACHING_BASIC, null);
            }
        }
    }
    
    /**
     * Stop voice coaching
     */
    private void stopVoiceCoaching() {
        if (voiceCoach != null) {
            voiceCoach.stopCoaching();
        }
    }
    
    /**
     * Add a location update listener
     * @param listener Listener to add
     */
    public void addLocationUpdateListener(LocationUpdateListener listener) {
        if (listener != null && !locationUpdateListeners.contains(listener)) {
            locationUpdateListeners.add(listener);
        }
    }
    
    /**
     * Remove a location update listener
     * @param listener Listener to remove
     */
    public void removeLocationUpdateListener(LocationUpdateListener listener) {
        locationUpdateListeners.remove(listener);
    }
    
    /**
     * Get the current run
     * @return Current run or null if not tracking
     */
    public Run getCurrentRun() {
        return currentRun;
    }
    
    /**
     * Check if tracking is active
     * @return true if tracking
     */
    public boolean isTracking() {
        return isTracking;
    }
    
    /**
     * Check if tracking is paused
     * @return true if paused
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Create a notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Used for tracking your run location");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Create a notification for the foreground service
     * @return Notification
     */
    private Notification createNotification() {
        // Create an intent to open the app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_active))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        return builder.build();
    }
    
    /**
     * Update the notification with current run stats
     */
    private void updateNotification() {
        if (!isTracking || currentRun == null) {
            return;
        }
        
        // Get stats
        String statusText;
        if (isPaused) {
            statusText = getString(R.string.tracking_paused);
        } else {
            String distance = String.format("%.2f km", currentRun.getTotalDistance());
            String duration = formatDuration(currentRun.getActiveDuration());
            statusText = getString(R.string.tracking_stats, distance, duration);
        }
        
        // Create intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Build updated notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(statusText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        // Get notification manager and update
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
    
    /**
     * Format duration in seconds to hours, minutes, seconds
     * @param seconds Duration in seconds
     * @return Formatted duration string
     */
    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
    
    @Override
    public void onDestroy() {
        // Clean up
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        
        // Stop coaching
        if (voiceCoach != null) {
            voiceCoach.stopCoaching();
        }
        
        super.onDestroy();
    }
}