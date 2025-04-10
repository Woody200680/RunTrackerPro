package com.runtracker.android.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.runtracker.android.R;
import com.runtracker.android.data.AchievementManager;
import com.runtracker.android.data.CoachingManager;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.data.repositories.RunRepository;
import com.runtracker.android.services.AudioCueManager;
import com.runtracker.android.services.LocationTrackingService;
import com.runtracker.android.services.VoiceCoach;

/**
 * Main activity for the app
 */
public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    private BottomNavigationView bottomNavigationView;
    private NavController navController;
    
    // Repositories and managers
    private RunRepository runRepository;
    private AchievementManager achievementManager;
    private CoachingManager coachingManager;
    
    // Services
    private AudioCueManager audioCueManager;
    private VoiceCoach voiceCoach;
    
    // Active run state
    private Run activeRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize repositories and managers
        runRepository = new RunRepository(this);
        achievementManager = new AchievementManager(this);
        coachingManager = new CoachingManager(this);
        
        // Initialize services
        audioCueManager = new AudioCueManager(this);
        voiceCoach = new VoiceCoach(this);
        
        // Set up navigation
        setupNavigation();
        
        // Check location permissions
        checkLocationPermission();
    }
    
    /**
     * Set up bottom navigation with NavController
     */
    private void setupNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
        
        // Set up item selection listener for custom behavior if needed
        bottomNavigationView.setOnNavigationItemSelectedListener(this::handleNavigationItemSelected);
    }
    
    /**
     * Handle navigation item selection
     * @param item Selected menu item
     * @return true if the item should be selected
     */
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        // Custom navigation logic if needed
        
        // Default navigation behavior
        return NavigationUI.onNavDestinationSelected(item, navController);
    }
    
    /**
     * Check if location permission is granted, request if not
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied. Tracking won't work.", 
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * Start location tracking service
     */
    public void startLocationTracking() {
        Intent intent = new Intent(this, LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_START);
        startService(intent);
    }
    
    /**
     * Stop location tracking service
     */
    public void stopLocationTracking() {
        Intent intent = new Intent(this, LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_STOP);
        startService(intent);
    }
    
    /**
     * Pause location tracking
     */
    public void pauseLocationTracking() {
        Intent intent = new Intent(this, LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_PAUSE);
        startService(intent);
    }
    
    /**
     * Resume location tracking
     */
    public void resumeLocationTracking() {
        Intent intent = new Intent(this, LocationTrackingService.class);
        intent.setAction(LocationTrackingService.ACTION_RESUME);
        startService(intent);
    }
    
    /**
     * Get the run repository
     * @return Run repository
     */
    public RunRepository getRunRepository() {
        return runRepository;
    }
    
    /**
     * Get the achievement manager
     * @return Achievement manager
     */
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    
    /**
     * Get the coaching manager
     * @return Coaching manager
     */
    public CoachingManager getCoachingManager() {
        return coachingManager;
    }
    
    /**
     * Get the audio cue manager
     * @return Audio cue manager
     */
    public AudioCueManager getAudioCueManager() {
        return audioCueManager;
    }
    
    /**
     * Get the voice coach
     * @return Voice coach
     */
    public VoiceCoach getVoiceCoach() {
        return voiceCoach;
    }
    
    /**
     * Set the active run
     * @param run Active run
     */
    public void setActiveRun(Run run) {
        this.activeRun = run;
    }
    
    /**
     * Get the active run
     * @return Active run
     */
    public Run getActiveRun() {
        return activeRun;
    }
    
    @Override
    protected void onDestroy() {
        // Cleanup resources
        if (audioCueManager != null) {
            audioCueManager.shutdown();
        }
        
        if (voiceCoach != null) {
            voiceCoach.shutdown();
        }
        
        super.onDestroy();
    }
}