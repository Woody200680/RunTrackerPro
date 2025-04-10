package com.runtracker.android.ui.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
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
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class RunDetailFragment extends Fragment implements OnMapReadyCallback {

    private RunRepository runRepository;
    private Run run;
    private String runId;
    private NavController navController;
    
    private TextView tvRunDate, tvDistance, tvDuration, tvPace, tvCalories;
    private Button btnDelete;
    private Toolbar toolbar;
    private GoogleMap googleMap;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_run_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        tvRunDate = view.findViewById(R.id.tvRunDate);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvDuration = view.findViewById(R.id.tvDuration);
        tvPace = view.findViewById(R.id.tvPace);
        tvCalories = view.findViewById(R.id.tvCalories);
        btnDelete = view.findViewById(R.id.btnDelete);
        toolbar = view.findViewById(R.id.toolbar);
        
        // Set up toolbar navigation
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        
        // Initialize navigation controller
        navController = Navigation.findNavController(view);
        
        // Get run ID from arguments
        if (getArguments() != null) {
            runId = getArguments().getString("runId");
        }
        
        // Initialize repository
        runRepository = new RunRepository(requireContext());
        
        // Load run data
        loadRunDetails();
        
        // Set up delete button
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }
    
    private void loadRunDetails() {
        if (runId == null) {
            Toast.makeText(requireContext(), "Run not found", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return;
        }
        
        run = runRepository.getRunById(runId);
        if (run == null) {
            Toast.makeText(requireContext(), "Run not found", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
            return;
        }
        
        // Display run data
        tvRunDate.setText(FormatUtils.formatDateTime(run.getStartTime()));
        tvDistance.setText(FormatUtils.formatDistance(run.getDistanceInMeters()));
        tvDuration.setText(FormatUtils.formatDuration(run.getDurationInSeconds()));
        tvPace.setText(FormatUtils.formatPace(run.getPace()));
        tvCalories.setText(FormatUtils.formatCalories(run.getCaloriesBurned()));
    }
    
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        
        if (run != null) {
            drawRouteOnMap();
        }
    }
    
    private void drawRouteOnMap() {
        if (run.getRoutePoints() == null || run.getRoutePoints().isEmpty()) {
            // No route points available
            return;
        }
        
        // Parse route points
        List<LatLng> points = new ArrayList<>();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        
        for (String pointStr : run.getRoutePoints()) {
            String[] latLng = pointStr.split(",");
            if (latLng.length == 2) {
                try {
                    double lat = Double.parseDouble(latLng[0]);
                    double lng = Double.parseDouble(latLng[1]);
                    LatLng point = new LatLng(lat, lng);
                    points.add(point);
                    boundsBuilder.include(point);
                } catch (NumberFormatException e) {
                    // Skip invalid point
                }
            }
        }
        
        if (points.isEmpty()) {
            return;
        }
        
        // Draw polyline
        googleMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(Color.BLUE));
        
        // Zoom to show the entire route with padding
        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100; // offset from edges of the map in pixels
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (IllegalStateException e) {
            // This can happen if bounds has no points
            // In this case, we'll just center on the first point
            if (!points.isEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 15f));
            }
        }
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Run")
                .setMessage("Are you sure you want to delete this run? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteRun())
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    
    private void deleteRun() {
        if (runId != null) {
            runRepository.deleteRun(runId);
            Toast.makeText(requireContext(), "Run deleted", Toast.LENGTH_SHORT).show();
            navController.navigateUp();
        }
    }
}