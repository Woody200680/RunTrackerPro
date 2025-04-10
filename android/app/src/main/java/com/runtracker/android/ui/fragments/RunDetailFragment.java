package com.runtracker.android.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.ui.MainActivity;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying details of a specific run
 */
public class RunDetailFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvRunTitle;
    private TextView tvDetailDate;
    private TextView tvDetailDuration;
    private TextView tvDetailDistance;
    private TextView tvDetailPace;
    private TextView tvDetailCalories;
    private ImageButton btnBack;
    private ImageButton btnDelete;
    
    private RunRepository runRepository;
    private GoogleMap map;
    private Run run;
    private String runId;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvRunTitle = view.findViewById(R.id.tvRunTitle);
        tvDetailDate = view.findViewById(R.id.tvDetailDate);
        tvDetailDuration = view.findViewById(R.id.tvDetailDuration);
        tvDetailDistance = view.findViewById(R.id.tvDetailDistance);
        tvDetailPace = view.findViewById(R.id.tvDetailPace);
        tvDetailCalories = view.findViewById(R.id.tvDetailCalories);
        btnBack = view.findViewById(R.id.btnBack);
        btnDelete = view.findViewById(R.id.btnDelete);
        
        // Get dependencies
        runRepository = ((MainActivity) requireActivity()).getRunRepository();
        
        // Get run ID from arguments
        RunDetailFragmentArgs args = RunDetailFragmentArgs.fromBundle(requireArguments());
        runId = args.getRunId();
        
        // Load run data
        loadRunData();
        
        // Set up map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapViewDetail);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Set up button listeners
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }
    
    /**
     * Load run data from repository
     */
    private void loadRunData() {
        run = runRepository.getRunById(runId);
        
        if (run != null) {
            // Set run title
            String formattedDate = FormatUtils.formatDate(run.getStartTime());
            tvRunTitle.setText(getString(R.string.run_date, formattedDate));
            
            // Set run details
            String formattedDateTime = FormatUtils.formatDateTime(run.getStartTime());
            tvDetailDate.setText(getString(R.string.run_start_time, 
                    FormatUtils.formatDate(run.getStartTime()), 
                    FormatUtils.formatTime(run.getStartTime())));
            
            tvDetailDuration.setText(getString(R.string.run_duration, 
                    FormatUtils.formatDuration(run.getActiveDuration())));
            
            tvDetailDistance.setText(getString(R.string.run_distance, 
                    FormatUtils.formatDistance(run.getTotalDistance())));
            
            tvDetailPace.setText(getString(R.string.run_pace, 
                    FormatUtils.formatPace(run.getPace())));
            
            tvDetailCalories.setText(getString(R.string.run_calories, 
                    FormatUtils.formatCalories(run.getCaloriesBurned())));
        }
    }
    
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        
        if (run != null && !run.getLocationPoints().isEmpty()) {
            drawRoute();
        }
    }
    
    /**
     * Draw the route on the map
     */
    private void drawRoute() {
        if (map != null && run != null && !run.getLocationPoints().isEmpty()) {
            // Clear previous polylines
            map.clear();
            
            // Get location points
            List<Run.LocationPoint> points = run.getLocationPoints();
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
            } else if (routePoints.size() == 1) {
                // If there's only one point, just center on it
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 15));
            }
        }
    }
    
    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_run)
                .setMessage(R.string.delete_confirmation)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteRun();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }
    
    /**
     * Delete the run and navigate back
     */
    private void deleteRun() {
        if (runRepository.deleteRun(runId)) {
            // Navigate back
            Navigation.findNavController(requireView()).popBackStack();
        }
    }
}