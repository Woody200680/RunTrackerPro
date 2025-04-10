package com.runtracker.android.ui.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.runtracker.android.R;
import com.runtracker.android.data.CoachingManager;
import com.runtracker.android.data.models.CoachingWorkout;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.services.AudioCueManager;
import com.runtracker.android.services.LocationTrackingService;
import com.runtracker.android.services.VoiceCoach;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.Constants;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for tracking runs and displaying real-time statistics
 */
public class TrackFragment extends Fragment implements OnMapReadyCallback, 
        LocationTrackingService.LocationUpdateListener {

    private TextView tvDistance;
    private TextView tvDuration;
    private TextView tvPace;
    private TextView tvCalories;
    private FloatingActionButton fabStartPause;
    private Button btnStop;
    private TextView tvWorkoutInfo;
    private TextView tvCoachingStatus;
    
    private GoogleMap map;
    private List<LatLng> routePoints = new ArrayList<>();
    
    // Services and managers
    private LocationTrackingService trackingService;
    private boolean isServiceBound = false;
    private AudioCueManager audioCueManager;
    private VoiceCoach voiceCoach;
    private CoachingManager coachingManager;
    
    // Current run state
    private boolean isTracking = false;
    private boolean isPaused = false;
    private Run currentRun;
    
    // Coaching state
    private boolean isCoachingEnabled = true;
    private int coachingType = Constants.COACHING_TYPE_BASIC;
    private CoachingWorkout activeWorkout;
    
    // Service connection
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationTrackingService.LocalBinder binder = 
                    (LocationTrackingService.LocalBinder) service;
            trackingService = binder.getService();
            isServiceBound = true;
            
            // Register as a listener for location updates
            trackingService.addLocationUpdateListener(TrackFragment.this);
            
            // Check if service is already tracking
            if (trackingService.isTracking()) {
                isTracking = true;
                isPaused = trackingService.isPaused();
                currentRun = trackingService.getCurrentRun();
                updateUI();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            trackingService = null;
            isServiceBound = false;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize UI elements
        initUI(view);
        
        // Get dependencies
        MainActivity activity = (MainActivity) requireActivity();
        audioCueManager = activity.getAudioCueManager();
        voiceCoach = activity.getVoiceCoach();
        coachingManager = activity.getCoachingManager();
        
        // Load coaching settings
        loadCoachingSettings();
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set up button listeners
        setupButtonListeners();
        
        // Set initial UI state
        updateUI();
    }
    
    /**
     * Initialize UI elements
     */
    private void initUI(View view) {
        tvDistance = view.findViewById(R.id.tvDistance);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvPace = view.findViewById(R.id.tvPace);
        tvCalories = view.findViewById(R.id.tvCalories);
        fabStartPause = view.findViewById(R.id.fabStartPause);
        btnStop = view.findViewById(R.id.btnStop);
        tvWorkoutInfo = view.findViewById(R.id.tvWorkoutInfo);
        tvCoachingStatus = view.findViewById(R.id.tvCoachingStatus);
    }
    
    /**
     * Load coaching settings from preferences
     */
    private void loadCoachingSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        isCoachingEnabled = preferences.getBoolean(Constants.PREF_COACHING_ENABLED, true);
        coachingType = preferences.getInt(Constants.PREF_COACHING_TYPE, Constants.COACHING_TYPE_BASIC);
        
        // If workout coaching is enabled, get the active workout
        if (coachingType == Constants.COACHING_TYPE_WORKOUT) {
            String activePlanId = preferences.getString(Constants.PREF_ACTIVE_PLAN_ID, null);
            if (activePlanId != null) {
                // Get next scheduled workout
                activeWorkout = coachingManager.getNextScheduledWorkout();
                
                if (activeWorkout != null) {
                    updateWorkoutInfo();
                }
            }
        }
        
        // Update coaching status text
        updateCoachingStatus();
    }
    
    /**
     * Update coaching status text
     */
    private void updateCoachingStatus() {
        if (!isCoachingEnabled) {
            tvCoachingStatus.setText(R.string.coaching_disabled);
            tvCoachingStatus.setVisibility(View.VISIBLE);
            return;
        }
        
        if (coachingType == Constants.COACHING_TYPE_BASIC) {
            tvCoachingStatus.setText(R.string.basic_coaching_enabled);
            tvCoachingStatus.setVisibility(View.VISIBLE);
        } else if (coachingType == Constants.COACHING_TYPE_WORKOUT && activeWorkout != null) {
            tvCoachingStatus.setText(R.string.workout_coaching_enabled);
            tvCoachingStatus.setVisibility(View.VISIBLE);
        } else {
            tvCoachingStatus.setVisibility(View.GONE);
        }
    }
    
    /**
     * Update workout information display
     */
    private void updateWorkoutInfo() {
        if (activeWorkout != null) {
            String workoutName = activeWorkout.getName();
            String workoutType = CoachingWorkout.getTypeName(activeWorkout.getType());
            double estimatedDistance = activeWorkout.getEstimatedDistance();
            long totalDuration = activeWorkout.getTotalDuration();
            
            StringBuilder info = new StringBuilder(workoutName)
                    .append(" (").append(workoutType).append(")\n")
                    .append(getString(R.string.estimated_distance, 
                            FormatUtils.formatDistance(estimatedDistance))).append(" | ")
                    .append(getString(R.string.estimated_duration, 
                            FormatUtils.formatDuration(totalDuration)));
            
            tvWorkoutInfo.setText(info);
            tvWorkoutInfo.setVisibility(View.VISIBLE);
        } else {
            tvWorkoutInfo.setVisibility(View.GONE);
        }
    }
    
    /**
     * Set up button click listeners
     */
    private void setupButtonListeners() {
        // Start/Pause button
        fabStartPause.setOnClickListener(v -> {
            if (!isTracking) {
                startRun();
            } else if (isPaused) {
                resumeRun();
            } else {
                pauseRun();
            }
        });
        
        // Stop button
        btnStop.setOnClickListener(v -> {
            stopRun();
        });
    }
    
    /**
     * Start a new run
     */
    private void startRun() {
        // Start location tracking service
        Intent intent = new Intent(requireContext(), LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_START);
        requireContext().startService(intent);
        
        // Bind to service
        if (!isServiceBound) {
            requireContext().bindService(
                    new Intent(requireContext(), LocationTrackingService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
        
        // Update state
        isTracking = true;
        isPaused = false;
        
        // Clear route points
        routePoints.clear();
        
        // Update UI
        updateUI();
    }
    
    /**
     * Pause the current run
     */
    private void pauseRun() {
        if (isServiceBound && trackingService != null) {
            // Pause tracking
            Intent intent = new Intent(requireContext(), LocationTrackingService.class);
            intent.setAction(LocationTrackingService.ACTION_PAUSE);
            requireContext().startService(intent);
            
            // Update state
            isPaused = true;
            
            // Update UI
            updateUI();
        }
    }
    
    /**
     * Resume the paused run
     */
    private void resumeRun() {
        if (isServiceBound && trackingService != null) {
            // Resume tracking
            Intent intent = new Intent(requireContext(), LocationTrackingService.class);
            intent.setAction(LocationTrackingService.ACTION_RESUME);
            requireContext().startService(intent);
            
            // Update state
            isPaused = false;
            
            // Update UI
            updateUI();
        }
    }
    
    /**
     * Stop the current run
     */
    private void stopRun() {
        if (isServiceBound && trackingService != null) {
            // Stop tracking
            Intent intent = new Intent(requireContext(), LocationTrackingService.class);
            intent.setAction(LocationTrackingService.ACTION_STOP);
            requireContext().startService(intent);
            
            // Update state
            isTracking = false;
            isPaused = false;
            currentRun = null;
            
            // Clear route
            routePoints.clear();
            if (map != null) {
                map.clear();
            }
            
            // Update UI
            updateUI();
        }
    }
    
    /**
     * Update the UI based on current state
     */
    private void updateUI() {
        if (isTracking) {
            // Update button appearances
            if (isPaused) {
                fabStartPause.setImageResource(R.drawable.ic_play);
                fabStartPause.setContentDescription(getString(R.string.resume_run));
            } else {
                fabStartPause.setImageResource(R.drawable.ic_pause);
                fabStartPause.setContentDescription(getString(R.string.pause_run));
            }
            
            // Show stop button
            btnStop.setVisibility(View.VISIBLE);
            
            // Update stats if available
            if (currentRun != null) {
                tvDistance.setText(FormatUtils.formatDistance(currentRun.getTotalDistance()));
                tvDuration.setText(FormatUtils.formatDuration(currentRun.getActiveDuration()));
                
                double pace = currentRun.getPace();
                if (pace > 0) {
                    tvPace.setText(FormatUtils.formatPace(pace));
                } else {
                    tvPace.setText(R.string.pace_placeholder);
                }
                
                tvCalories.setText(FormatUtils.formatCalories(currentRun.getCaloriesBurned()));
            }
        } else {
            // Reset to initial state
            fabStartPause.setImageResource(R.drawable.ic_play);
            fabStartPause.setContentDescription(getString(R.string.start_run));
            btnStop.setVisibility(View.GONE);
            
            // Reset stats
            tvDistance.setText(R.string.distance_placeholder);
            tvDuration.setText(R.string.duration_placeholder);
            tvPace.setText(R.string.pace_placeholder);
            tvCalories.setText(R.string.calories_placeholder);
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        
        // Set initial camera position
        map.moveCamera(CameraUpdateFactory.zoomTo(15));
        
        // If already tracking, redraw route
        if (isTracking && !routePoints.isEmpty()) {
            drawRoute();
        }
    }
    
    @Override
    public void onLocationUpdate(Location location, Run run) {
        // Update current run
        currentRun = run;
        
        // Add point to route
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        routePoints.add(point);
        
        // Update map
        if (map != null) {
            // If this is the first point, move camera
            if (routePoints.size() == 1) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
            } else {
                // Otherwise just update the camera position
                map.animateCamera(CameraUpdateFactory.newLatLng(point));
            }
            
            // Draw route
            drawRoute();
        }
        
        // Update UI
        updateUI();
    }
    
    /**
     * Draw the route on the map
     */
    private void drawRoute() {
        if (map == null || routePoints.isEmpty()) {
            return;
        }
        
        // Create polyline options
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .color(getResources().getColor(R.color.primary, null))
                .width(10);
        
        // Clear previous polylines and add new one
        map.clear();
        map.addPolyline(polylineOptions);
        
        // If there are multiple points, zoom to fit the route
        if (routePoints.size() > 1) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : routePoints) {
                boundsBuilder.include(point);
            }
            
            // Ensure we don't zoom out too far
            int padding = 100; // pixels
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding));
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        // Bind to service if it's running
        if (!isServiceBound) {
            requireContext().bindService(
                    new Intent(requireContext(), LocationTrackingService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        
        // Unbind from service
        if (isServiceBound) {
            if (trackingService != null) {
                trackingService.removeLocationUpdateListener(this);
            }
            requireContext().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}