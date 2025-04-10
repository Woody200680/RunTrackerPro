package com.runtracker.android.ui.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TrackFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 5000; // 5 seconds
    private static final long FASTEST_LOCATION_INTERVAL = 2000; // 2 seconds

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private RunRepository runRepository;
    
    private TextView tvTime, tvDistance, tvPace, tvGpsStatus;
    private Button btnStart, btnPause, btnResume, btnStop;
    
    private long startTimeMillis = 0;
    private long timeInMillis = 0;
    private long pausedTimeMillis = 0;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private boolean isPaused = false;
    
    private Run currentRun;
    private List<LatLng> routePoints = new ArrayList<>();
    private float totalDistanceInMeters = 0;
    private LatLng lastLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvTime = view.findViewById(R.id.tvTime);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvPace = view.findViewById(R.id.tvPace);
        tvGpsStatus = view.findViewById(R.id.tvGpsStatus);
        
        btnStart = view.findViewById(R.id.btnStart);
        btnPause = view.findViewById(R.id.btnPause);
        btnResume = view.findViewById(R.id.btnResume);
        btnStop = view.findViewById(R.id.btnStop);
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        
        // Create location request
        locationRequest = LocationRequest.create()
                .setInterval(LOCATION_UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_LOCATION_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        
        // Create location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                
                for (Location location : locationResult.getLocations()) {
                    onLocationUpdate(location);
                }
            }
        };
        
        // Set up click listeners
        btnStart.setOnClickListener(v -> startRun());
        btnPause.setOnClickListener(v -> pauseRun());
        btnResume.setOnClickListener(v -> resumeRun());
        btnStop.setOnClickListener(v -> stopRun());
        
        // Initialize repository
        runRepository = new RunRepository(requireContext());
        
        // Reset UI
        resetUI();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            
            // Get current location to center the map
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                            tvGpsStatus.setText(R.string.gps_connected);
                        }
                    });
        } else {
            requestLocationPermissions();
        }
    }

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reload the map
                if (googleMap != null && getActivity() != null) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), 
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        googleMap.setMyLocationEnabled(true);
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Location permission is required for tracking runs", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startRun() {
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }
        
        // Reset values
        routePoints.clear();
        totalDistanceInMeters = 0;
        lastLocation = null;
        
        // Update UI
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        
        // Create new run
        currentRun = new Run();
        currentRun.setStartTime(new Date());
        
        // Start timer
        startTimeMillis = SystemClock.elapsedRealtime();
        timerHandler.postDelayed(timerRunnable, 0);
        
        // Start location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        
        isRunning = true;
        isPaused = false;
    }

    private void pauseRun() {
        if (!isRunning || isPaused) return;
        
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.VISIBLE);
        
        // Pause timer
        pausedTimeMillis = timeInMillis;
        timerHandler.removeCallbacks(timerRunnable);
        
        // Stop location updates to save battery
        fusedLocationClient.removeLocationUpdates(locationCallback);
        
        isPaused = true;
        
        // Update run in database
        if (currentRun != null) {
            currentRun.setPaused(true);
            currentRun.setPauseTime(new Date());
            runRepository.updateRun(currentRun);
        }
    }

    private void resumeRun() {
        if (!isRunning || !isPaused) return;
        
        btnResume.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);
        
        // Calculate new start time that accounts for the paused duration
        startTimeMillis = SystemClock.elapsedRealtime() - pausedTimeMillis;
        timerHandler.postDelayed(timerRunnable, 0);
        
        // Resume location updates
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        
        isPaused = false;
        
        // Update run in database
        if (currentRun != null) {
            currentRun.setPaused(false);
            currentRun.setResumeTime(new Date());
            runRepository.updateRun(currentRun);
        }
    }

    private void stopRun() {
        if (!isRunning) return;
        
        // Stop timer
        timerHandler.removeCallbacks(timerRunnable);
        
        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback);
        
        // Reset flags
        isRunning = false;
        isPaused = false;
        
        // Update run in database with final stats
        if (currentRun != null) {
            currentRun.setEndTime(new Date());
            currentRun.setDurationInSeconds((int) (timeInMillis / 1000));
            currentRun.setDistanceInMeters(totalDistanceInMeters);
            
            // Calculate pace (minutes per kilometer)
            float paceMinutesPerKm = 0;
            if (totalDistanceInMeters > 0) {
                paceMinutesPerKm = (timeInMillis / 1000f / 60f) / (totalDistanceInMeters / 1000f);
            }
            currentRun.setPace(paceMinutesPerKm);
            
            // Save route points
            List<String> routeStringList = new ArrayList<>();
            for (LatLng point : routePoints) {
                routeStringList.add(point.latitude + "," + point.longitude);
            }
            currentRun.setRoutePoints(routeStringList);
            
            // Calculate calories burned (simplified calculation)
            // Assuming 60 calories per kilometer for an average person
            float caloriesBurned = (totalDistanceInMeters / 1000f) * 60;
            currentRun.setCaloriesBurned(Math.round(caloriesBurned));
            
            // Save completed run
            runRepository.insertRun(currentRun);
            
            // Navigate to run details if needed
            // navController.navigate(R.id.action_trackFragment_to_runDetailFragment, bundleWithRunId);
        }
        
        // Reset UI
        resetUI();
        
        Toast.makeText(getContext(), "Run saved!", Toast.LENGTH_SHORT).show();
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timeInMillis = SystemClock.elapsedRealtime() - startTimeMillis;
            updateTimerUI();
            timerHandler.postDelayed(this, 1000);
        }
    };

    private void updateTimerUI() {
        int hours = (int) (timeInMillis / (1000 * 60 * 60));
        int minutes = (int) (timeInMillis / (1000 * 60)) % 60;
        int seconds = (int) (timeInMillis / 1000) % 60;
        
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTime.setText(timeString);
    }

    private void onLocationUpdate(Location location) {
        // Update GPS status
        tvGpsStatus.setText(R.string.gps_tracking);
        
        // Get current location
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        
        // Center map on current location with zoom level 15
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
        
        // Add point to route
        routePoints.add(currentLatLng);
        
        // Calculate distance increment if we have previous point
        if (lastLocation != null) {
            float[] results = new float[1];
            Location.distanceBetween(
                    lastLocation.latitude, lastLocation.longitude,
                    currentLatLng.latitude, currentLatLng.longitude,
                    results);
            
            // Only add the distance if it's reasonable (to filter out GPS jumps)
            if (results[0] < 50) { // 50 meters max between points
                totalDistanceInMeters += results[0];
                updateDistanceUI();
                updatePaceUI();
                
                // Draw route on map
                googleMap.addPolyline(new PolylineOptions()
                        .add(lastLocation, currentLatLng)
                        .width(10)
                        .color(Color.BLUE));
            }
        }
        
        // Update last location
        lastLocation = currentLatLng;
        
        // Update run in database
        if (currentRun != null) {
            currentRun.setDistanceInMeters(totalDistanceInMeters);
        }
    }

    private void updateDistanceUI() {
        // Convert meters to kilometers with 2 decimal places
        float distanceKm = totalDistanceInMeters / 1000f;
        tvDistance.setText(String.format("%.2f km", distanceKm));
    }

    private void updatePaceUI() {
        if (totalDistanceInMeters > 0 && timeInMillis > 0) {
            // Calculate pace in minutes per kilometer
            float paceMinPerKm = (timeInMillis / 1000f / 60f) / (totalDistanceInMeters / 1000f);
            
            // Format pace as minutes:seconds per km
            int paceMinutes = (int) paceMinPerKm;
            int paceSeconds = (int) ((paceMinPerKm - paceMinutes) * 60);
            
            tvPace.setText(String.format("%d:%02d /km", paceMinutes, paceSeconds));
        } else {
            tvPace.setText("--:-- /km");
        }
    }

    private void resetUI() {
        // Reset timer display
        tvTime.setText("00:00:00");
        
        // Reset distance display
        tvDistance.setText("0.00 km");
        
        // Reset pace display
        tvPace.setText("--:-- /km");
        
        // Reset GPS status
        tvGpsStatus.setText(R.string.gps_ready);
        
        // Reset button states
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        
        // Clear map if needed
        if (googleMap != null) {
            googleMap.clear();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // If a run is active, we should save its state or pause it
        if (isRunning && !isPaused) {
            pauseRun();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // Clean up timer and location updates
        timerHandler.removeCallbacks(timerRunnable);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}