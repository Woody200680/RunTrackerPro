package com.runtracker.android.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runtracker.android.data.models.Run;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository class for managing Run data
 */
public class RunRepository {
    private static final String TAG = "RunRepository";
    private static final String PREFS_NAME = "run_tracker_prefs";
    private static final String KEY_RUNS = "runs";
    private static final String KEY_CURRENT_RUN = "current_run";
    
    private static RunRepository instance;
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    private final MutableLiveData<List<Run>> allRuns = new MutableLiveData<>();
    private final MutableLiveData<Run> currentRun = new MutableLiveData<>();
    
    private RunRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        
        // Load saved runs from SharedPreferences
        loadRuns();
        loadCurrentRun();
    }
    
    /**
     * Get the singleton instance of RunRepository
     */
    public static synchronized RunRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RunRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Load runs from SharedPreferences
     */
    private void loadRuns() {
        String runsJson = prefs.getString(KEY_RUNS, null);
        List<Run> runs = new ArrayList<>();
        
        if (runsJson != null) {
            try {
                Type type = new TypeToken<List<Run>>() {}.getType();
                runs = gson.fromJson(runsJson, type);
            } catch (Exception e) {
                Log.e(TAG, "Error loading runs: " + e.getMessage());
            }
        }
        
        allRuns.setValue(runs);
    }
    
    /**
     * Load current run from SharedPreferences
     */
    private void loadCurrentRun() {
        String currentRunJson = prefs.getString(KEY_CURRENT_RUN, null);
        
        if (currentRunJson != null) {
            try {
                Run run = gson.fromJson(currentRunJson, Run.class);
                currentRun.setValue(run);
            } catch (Exception e) {
                Log.e(TAG, "Error loading current run: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save runs to SharedPreferences
     */
    private void saveRuns() {
        List<Run> runs = allRuns.getValue();
        
        if (runs != null) {
            try {
                String runsJson = gson.toJson(runs);
                prefs.edit().putString(KEY_RUNS, runsJson).apply();
            } catch (Exception e) {
                Log.e(TAG, "Error saving runs: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save current run to SharedPreferences
     */
    private void saveCurrentRun() {
        Run run = currentRun.getValue();
        
        if (run != null) {
            try {
                String currentRunJson = gson.toJson(run);
                prefs.edit().putString(KEY_CURRENT_RUN, currentRunJson).apply();
            } catch (Exception e) {
                Log.e(TAG, "Error saving current run: " + e.getMessage());
            }
        } else {
            // Clear current run
            prefs.edit().remove(KEY_CURRENT_RUN).apply();
        }
    }
    
    /**
     * Get all saved runs as LiveData
     */
    public LiveData<List<Run>> getAllRuns() {
        return allRuns;
    }
    
    /**
     * Get the current active run as LiveData
     */
    public LiveData<Run> getCurrentRun() {
        return currentRun;
    }
    
    /**
     * Get a run by ID
     */
    public Run getRunById(String id) {
        List<Run> runs = allRuns.getValue();
        
        if (runs != null) {
            for (Run run : runs) {
                if (run.getId().equals(id)) {
                    return run;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Start a new run
     */
    public void startRun() {
        Run run = new Run();
        currentRun.setValue(run);
        saveCurrentRun();
    }
    
    /**
     * Update location data for the current run
     */
    public void updateLocation(double latitude, double longitude) {
        Run run = currentRun.getValue();
        
        if (run != null && run.getStatus() == Run.RunStatus.ACTIVE) {
            run.addLocationPoint(latitude, longitude);
            currentRun.setValue(run);
            saveCurrentRun();
        }
    }
    
    /**
     * Pause the current run
     */
    public void pauseRun() {
        Run run = currentRun.getValue();
        
        if (run != null && run.getStatus() == Run.RunStatus.ACTIVE) {
            run.pause();
            currentRun.setValue(run);
            saveCurrentRun();
        }
    }
    
    /**
     * Resume the current run
     */
    public void resumeRun() {
        Run run = currentRun.getValue();
        
        if (run != null && run.getStatus() == Run.RunStatus.PAUSED) {
            run.resume();
            currentRun.setValue(run);
            saveCurrentRun();
        }
    }
    
    /**
     * Stop and save the current run
     */
    public void stopRun() {
        Run run = currentRun.getValue();
        
        if (run != null && run.getStatus() != Run.RunStatus.COMPLETED) {
            run.complete();
            
            // Add to completed runs list
            List<Run> runs = allRuns.getValue();
            if (runs == null) {
                runs = new ArrayList<>();
            }
            runs.add(run);
            
            // Update and save
            allRuns.setValue(runs);
            currentRun.setValue(null);
            
            saveRuns();
            saveCurrentRun();
        }
    }
    
    /**
     * Delete a run by ID
     */
    public void deleteRun(String id) {
        List<Run> runs = allRuns.getValue();
        
        if (runs != null) {
            List<Run> updatedRuns = new ArrayList<>();
            
            for (Run run : runs) {
                if (!run.getId().equals(id)) {
                    updatedRuns.add(run);
                }
            }
            
            allRuns.setValue(updatedRuns);
            saveRuns();
        }
    }
    
    /**
     * Get total statistics for all runs
     */
    public RunStatistics getRunStatistics() {
        List<Run> runs = allRuns.getValue();
        RunStatistics stats = new RunStatistics();
        
        if (runs != null && !runs.isEmpty()) {
            double totalDistance = 0;
            long totalDuration = 0;
            int totalCalories = 0;
            
            for (Run run : runs) {
                totalDistance += run.getTotalDistance();
                totalDuration += run.getTotalDuration();
                totalCalories += run.getCaloriesBurned();
            }
            
            stats.totalRuns = runs.size();
            stats.totalDistance = totalDistance;
            stats.totalDuration = totalDuration;
            stats.totalCalories = totalCalories;
            stats.averagePace = totalDistance > 0 ? (totalDuration / 60.0) / totalDistance : 0;
        }
        
        return stats;
    }
    
    /**
     * Class to hold run statistics
     */
    public static class RunStatistics {
        public int totalRuns = 0;
        public double totalDistance = 0; // km
        public long totalDuration = 0; // seconds
        public double averagePace = 0; // min/km
        public int totalCalories = 0;
    }
}