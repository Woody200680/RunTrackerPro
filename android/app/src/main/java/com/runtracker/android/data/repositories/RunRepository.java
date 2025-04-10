package com.runtracker.android.data.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.runtracker.android.data.models.Run;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Repository for managing Run data
 */
public class RunRepository {
    
    private static final String TAG = "RunRepository";
    private static final String PREF_NAME = "run_tracker_prefs";
    private static final String PREF_RUNS = "runs";
    private static final String PREF_CURRENT_RUN = "current_run";
    
    private static RunRepository instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    private List<Run> runs;
    private Run currentRun;
    
    /**
     * Get singleton instance of RunRepository
     * @param context Application context
     * @return RunRepository instance
     */
    public static synchronized RunRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RunRepository(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Private constructor
     * @param context Application context
     */
    private RunRepository(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadRuns();
        loadCurrentRun();
    }
    
    /**
     * Load runs from SharedPreferences
     */
    private void loadRuns() {
        String runsJson = sharedPreferences.getString(PREF_RUNS, null);
        if (runsJson != null) {
            try {
                Type type = new TypeToken<ArrayList<Run>>() {}.getType();
                runs = gson.fromJson(runsJson, type);
            } catch (Exception e) {
                Log.e(TAG, "Error loading runs", e);
                runs = new ArrayList<>();
            }
        } else {
            runs = new ArrayList<>();
        }
    }
    
    /**
     * Save runs to SharedPreferences
     */
    private void saveRuns() {
        try {
            String runsJson = gson.toJson(runs);
            sharedPreferences.edit().putString(PREF_RUNS, runsJson).apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving runs", e);
        }
    }
    
    /**
     * Load current run from SharedPreferences
     */
    private void loadCurrentRun() {
        String currentRunJson = sharedPreferences.getString(PREF_CURRENT_RUN, null);
        if (currentRunJson != null) {
            try {
                currentRun = gson.fromJson(currentRunJson, Run.class);
            } catch (Exception e) {
                Log.e(TAG, "Error loading current run", e);
                currentRun = null;
            }
        } else {
            currentRun = null;
        }
    }
    
    /**
     * Save current run to SharedPreferences
     */
    private void saveCurrentRun() {
        try {
            if (currentRun != null) {
                String currentRunJson = gson.toJson(currentRun);
                sharedPreferences.edit().putString(PREF_CURRENT_RUN, currentRunJson).apply();
            } else {
                sharedPreferences.edit().remove(PREF_CURRENT_RUN).apply();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving current run", e);
        }
    }
    
    /**
     * Get all runs
     * @return List of all runs
     */
    public List<Run> getAllRuns() {
        return new ArrayList<>(runs);
    }
    
    /**
     * Get all completed runs sorted by start time (newest first)
     * @return List of completed runs
     */
    public List<Run> getCompletedRuns() {
        List<Run> completedRuns = new ArrayList<>();
        for (Run run : runs) {
            if (run.isFinished()) {
                completedRuns.add(run);
            }
        }
        
        // Sort by start time (newest first)
        Collections.sort(completedRuns, (run1, run2) -> 
                Long.compare(run2.getStartTime(), run1.getStartTime()));
        
        return completedRuns;
    }
    
    /**
     * Get a run by ID
     * @param id Run ID
     * @return Run with the given ID, or null if not found
     */
    public Run getRunById(String id) {
        for (Run run : runs) {
            if (run.getId().equals(id)) {
                return run;
            }
        }
        return null;
    }
    
    /**
     * Get the current run
     * @return Current run, or null if no run is in progress
     */
    public Run getCurrentRun() {
        return currentRun;
    }
    
    /**
     * Start a new run
     * @return The new run
     */
    public Run startRun() {
        currentRun = new Run();
        saveCurrentRun();
        return currentRun;
    }
    
    /**
     * Pause the current run
     * @return Updated run, or null if no run is in progress
     */
    public Run pauseRun() {
        if (currentRun != null && !currentRun.isPaused()) {
            currentRun.pause(System.currentTimeMillis());
            saveCurrentRun();
            return currentRun;
        }
        return null;
    }
    
    /**
     * Resume the current run
     * @return Updated run, or null if no run is in progress or not paused
     */
    public Run resumeRun() {
        if (currentRun != null && currentRun.isPaused()) {
            currentRun.resume(System.currentTimeMillis());
            saveCurrentRun();
            return currentRun;
        }
        return null;
    }
    
    /**
     * Add a location point to the current run
     * @param latitude Latitude
     * @param longitude Longitude
     * @return Updated run, or null if no run is in progress
     */
    public Run addLocationPoint(double latitude, double longitude) {
        if (currentRun != null && !currentRun.isPaused() && !currentRun.isFinished()) {
            currentRun.addLocationPoint(latitude, longitude, System.currentTimeMillis());
            saveCurrentRun();
            return currentRun;
        }
        return null;
    }
    
    /**
     * Stop and save the current run
     * @param caloriesBurned Estimated calories burned
     * @return Completed run, or null if no run is in progress
     */
    public Run stopRun(int caloriesBurned) {
        if (currentRun != null && !currentRun.isFinished()) {
            currentRun.finish(System.currentTimeMillis(), caloriesBurned);
            runs.add(currentRun);
            saveRuns();
            
            Run completedRun = currentRun;
            currentRun = null;
            saveCurrentRun();
            
            return completedRun;
        }
        return null;
    }
    
    /**
     * Delete a run
     * @param id Run ID
     * @return True if the run was deleted, false otherwise
     */
    public boolean deleteRun(String id) {
        Run runToRemove = null;
        for (Run run : runs) {
            if (run.getId().equals(id)) {
                runToRemove = run;
                break;
            }
        }
        
        if (runToRemove != null) {
            runs.remove(runToRemove);
            saveRuns();
            return true;
        }
        
        return false;
    }
    
    /**
     * Calculate total distance of all completed runs
     * @return Total distance in kilometers
     */
    public double getTotalDistance() {
        double totalDistance = 0;
        for (Run run : getCompletedRuns()) {
            totalDistance += run.getTotalDistance();
        }
        return totalDistance;
    }
    
    /**
     * Calculate total active duration of all completed runs
     * @return Total active duration in milliseconds
     */
    public long getTotalDuration() {
        long totalDuration = 0;
        for (Run run : getCompletedRuns()) {
            totalDuration += run.getActiveDuration();
        }
        return totalDuration;
    }
    
    /**
     * Calculate average pace of all completed runs
     * @return Average pace in minutes per kilometer, or 0 if no data
     */
    public double getAveragePace() {
        double totalDistance = getTotalDistance();
        long totalDuration = getTotalDuration();
        
        if (totalDistance > 0 && totalDuration > 0) {
            return (totalDuration / 60000.0) / totalDistance; // Convert ms to minutes
        }
        
        return 0;
    }
    
    /**
     * Calculate total calories burned in all completed runs
     * @return Total calories burned
     */
    public int getTotalCalories() {
        int totalCalories = 0;
        for (Run run : getCompletedRuns()) {
            totalCalories += run.getCaloriesBurned();
        }
        return totalCalories;
    }
}