package com.runtracker.android.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.services.LocationTrackingService;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Fragment for tracking runs
 */
public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvGpsStatus;
    private TextView tvDuration;
    private TextView tvDistance;
    private TextView tvPace;
    private TextView tvCalories;
    private FloatingActionButton fabAction;
    private FloatingActionButton fabStop;

    private RunRepository runRepository;
    private LocationTrackingService locationTrackingService;
    private GoogleMap map;
    private Run currentRun;
    private boolean isTracking = false;
    private Handler timerHandler;
    private Runnable timerRunnable;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvGpsStatus = view.findViewById(R.id.tvGpsStatus);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvPace = view.findViewById(R.id.tvPace);
        tvCalories = view.findViewById(R.id.tvCalories);
        fabAction = view.findViewById(R.id.fabAction);
        fabStop = view.findViewById(R.id.fabStop);
        
        // Get dependencies
        runRepository = ((MainActivity) requireActivity()).getRunRepository();
        locationTrackingService = new LocationTrackingService(requireContext(), this::onLocationUpdate);
        
        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set up timer handler
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTrackingInfo();
                timerHandler.postDelayed(this, 1000);
            }
        };
        
        // Set up button listeners
        fabAction.setOnClickListener(v -> toggleTracking());
        fabStop.setOnClickListener(v -> stopRun());
        
        // Check if there's a run in progress
        currentRun = runRepository.getCurrentRun();
        updateUI();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (isTracking) {
            timerHandler.post(timerRunnable);
        }
        
        currentRun = runRepository.getCurrentRun();
        updateUI();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }
        
        // Draw route if we have a current run with location points
        if (currentRun != null && !currentRun.getLocationPoints().isEmpty()) {
            drawRoute();
        }
    }
    
    /**
     * Toggle tracking (start, pause, resume)
     */
    private void toggleTracking() {
        if (currentRun == null) {
            // Start new run
            startRun();
        } else if (currentRun.isPaused()) {
            // Resume run
            resumeRun();
        } else {
            // Pause run
            pauseRun();
        }
    }
    
    /**
     * Start a new run
     */
    private void startRun() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        currentRun = runRepository.startRun();
        isTracking = true;
        
        // Start tracking location
        locationTrackingService.startTracking();
        
        // Start timer
        timerHandler.post(timerRunnable);
        
        updateUI();
    }
    
    /**
     * Pause the current run
     */
    private void pauseRun() {
        if (currentRun != null && !currentRun.isPaused()) {
            runRepository.pauseRun();
            
            // Stop tracking location
            locationTrackingService.stopTracking();
            
            updateUI();
        }
    }
    
    /**
     * Resume the current run
     */
    private void resumeRun() {
        if (currentRun != null && currentRun.isPaused()) {
            runRepository.resumeRun();
            
            // Start tracking location
            locationTrackingService.startTracking();
            
            updateUI();
        }
    }
    
    /**
     * Stop and save the current run
     */
    private void stopRun() {
        if (currentRun != null) {
            // Calculate calories (simplified formula: 60 calories per km)
            int estimatedCalories = (int) (currentRun.getTotalDistance() * 60);
            
            // Stop run
            runRepository.stopRun(estimatedCalories);
            
            // Stop tracking location
            locationTrackingService.stopTracking();
            
            // Stop timer
            timerHandler.removeCallbacks(timerRunnable);
            isTracking = false;
            
            // Reset UI
            currentRun = null;
            if (map != null) {
                map.clear();
            }
            
            updateUI();
        }
    }
    
    /**
     * Update the UI based on current tracking state
     */
    private void updateUI() {
        if (currentRun == null) {
            // No run in progress
            fabAction.setImageResource(R.drawable.ic_run);
            fabAction.setContentDescription(getString(R.string.start_run));
            fabStop.setVisibility(View.GONE);
            tvGpsStatus.setText(R.string.tracking_disabled);
            
            // Reset metrics
            tvDuration.setText("00:00:00");
            tvDistance.setText("0.00 km");
            tvPace.setText("0:00");
            tvCalories.setText("0 kcal");
            
        } else if (currentRun.isPaused()) {
            // Run is paused
            fabAction.setImageResource(R.drawable.ic_run);
            fabAction.setContentDescription(getString(R.string.resume_run));
            fabStop.setVisibility(View.VISIBLE);
            tvGpsStatus.setText(R.string.tracking_disabled);
            
            // Update metrics
            updateTrackingInfo();
            
        } else {
            // Run is active
            fabAction.setImageResource(R.drawable.ic_pause);
            fabAction.setContentDescription(getString(R.string.pause_run));
            fabStop.setVisibility(View.VISIBLE);
            tvGpsStatus.setText(R.string.tracking_enabled);
            
            // Update metrics
            updateTrackingInfo();
        }
    }
    
    /**
     * Update tracking information (duration, distance, pace, calories)
     */
    private void updateTrackingInfo() {
        if (currentRun != null) {
            // Update duration
            long duration = currentRun.getActiveDuration();
            String formattedDuration = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(duration),
                    TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
            tvDuration.setText(formattedDuration);
            
            // Update distance
            double distance = currentRun.getTotalDistance();
            String formattedDistance = String.format(Locale.getDefault(), "%.2f km", distance);
            tvDistance.setText(formattedDistance);
            
            // Update pace
            double pace = currentRun.getPace();
            if (pace > 0) {
                int paceMinutes = (int) pace;
                int paceSeconds = (int) ((pace - paceMinutes) * 60);
                String formattedPace = String.format(Locale.getDefault(), "%d:%02d", 
                        paceMinutes, paceSeconds);
                tvPace.setText(formattedPace);
            } else {
                tvPace.setText("0:00");
            }
            
            // Update calories (simplified formula: 60 calories per km)
            int estimatedCalories = (int) (distance * 60);
            tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", estimatedCalories));
        }
    }
    
    /**
     * Handle location updates from the tracking service
     * @param latitude Latitude
     * @param longitude Longitude
     */
    private void onLocationUpdate(double latitude, double longitude) {
        if (currentRun != null && !currentRun.isPaused()) {
            // Add location point to run
            runRepository.addLocationPoint(latitude, longitude);
            
            // Update map
            updateMap(latitude, longitude);
        }
    }
    
    /**
     * Update map with new location
     * @param latitude Latitude
     * @param longitude Longitude
     */
    private void updateMap(double latitude, double longitude) {
        if (map != null) {
            LatLng latLng = new LatLng(latitude, longitude);
            
            // Move camera
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
            
            // Draw route
            drawRoute();
        }
    }
    
    /**
     * Draw the route on the map
     */
    private void drawRoute() {
        if (map != null && currentRun != null && !currentRun.getLocationPoints().isEmpty()) {
            // Clear previous polylines
            map.clear();
            
            // Get location points
            List<Run.LocationPoint> points = currentRun.getLocationPoints();
            List<LatLng> routePoints = new ArrayList<>();
            
            for (Run.LocationPoint point : points) {
                routePoints.add(new LatLng(point.getLatitude(), point.getLongitude()));
            }
            
            // Draw polyline
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(routePoints)
                    .color(ContextCompat.getColor(requireContext(), R.color.primary))
                    .width(12);
            
            map.addPolyline(polylineOptions);
            
            // Zoom to fit route
            if (routePoints.size() > 1) {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                for (LatLng point : routePoints) {
                    boundsBuilder.include(point);
                }
                LatLngBounds bounds = boundsBuilder.build();
                
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }
}