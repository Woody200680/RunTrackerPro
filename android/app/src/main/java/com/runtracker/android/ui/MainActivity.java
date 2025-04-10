package com.runtracker.android.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.runtracker.android.R;
import com.runtracker.android.data.repositories.RunRepository;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    private RunRepository runRepository;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted
                    showSnackbar(getString(R.string.tracking_enabled));
                } else {
                    // Permission denied
                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showLocationPermissionDialog();
                    } else {
                        showSettingsDialog();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize repository
        runRepository = RunRepository.getInstance(this);
        
        // Set up navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavigationView, navController);
        }
        
        // Check for location permission
        checkLocationPermission();
    }
    
    /**
     * Check if the app has location permission
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
        } else {
            // Request the permission
            requestLocationPermission();
        }
    }
    
    /**
     * Request location permission
     */
    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationPermissionDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }
    
    /**
     * Show dialog explaining why we need location permission
     */
    private void showLocationPermissionDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_needed)
                .setMessage(R.string.location_permission_needed)
                .setPositiveButton(R.string.grant_permission, (dialog, which) -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }
    
    /**
     * Show dialog to go to app settings to enable location permission
     */
    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.location_permission_denied)
                .setMessage(R.string.location_permission_denied)
                .setPositiveButton(R.string.open_settings, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }
    
    /**
     * Show a snackbar with message
     * @param message Message to show
     */
    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
    
    /**
     * Get the run repository instance
     * @return RunRepository
     */
    public RunRepository getRunRepository() {
        return runRepository;
    }
}