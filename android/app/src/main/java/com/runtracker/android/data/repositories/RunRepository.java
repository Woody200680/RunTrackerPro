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
 * Repository class to handle data operations for Run objects
 */
public class RunRepository {
    private static final String TAG = "RunRepository";
    private static final String PREF_NAME = "run_tracker_prefs";
    private static final String KEY_RUNS = "runs";
    
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    
    public RunRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Get all runs, sorted by most recent first
     */
    public List<Run> getAllRuns() {
        String runsJson = sharedPreferences.getString(KEY_RUNS, null);
        if (runsJson == null) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<Run>>() {}.getType();
        List<Run> runs = gson.fromJson(runsJson, type);
        
        // Sort by most recent first
        Collections.sort(runs, (run1, run2) -> {
            if (run1.getStartTime() == null || run2.getStartTime() == null) {
                return 0;
            }
            return run2.getStartTime().compareTo(run1.getStartTime());
        });
        
        return runs;
    }
    
    /**
     * Get a specific run by ID
     */
    public Run getRunById(String id) {
        List<Run> runs = getAllRuns();
        for (Run run : runs) {
            if (run.getId().equals(id)) {
                return run;
            }
        }
        return null;
    }
    
    /**
     * Insert a new run
     */
    public void insertRun(Run run) {
        List<Run> runs = getAllRuns();
        
        // Check if run with same ID already exists
        for (int i = 0; i < runs.size(); i++) {
            if (runs.get(i).getId().equals(run.getId())) {
                runs.set(i, run);
                saveRuns(runs);
                return;
            }
        }
        
        // Add new run
        runs.add(run);
        saveRuns(runs);
    }
    
    /**
     * Update an existing run
     */
    public void updateRun(Run run) {
        List<Run> runs = getAllRuns();
        for (int i = 0; i < runs.size(); i++) {
            if (runs.get(i).getId().equals(run.getId())) {
                runs.set(i, run);
                saveRuns(runs);
                return;
            }
        }
    }
    
    /**
     * Delete a run by ID
     */
    public void deleteRun(String id) {
        List<Run> runs = getAllRuns();
        for (int i = 0; i < runs.size(); i++) {
            if (runs.get(i).getId().equals(id)) {
                runs.remove(i);
                saveRuns(runs);
                return;
            }
        }
    }
    
    /**
     * Get total statistics
     * @return Array with [totalDistance (meters), totalDuration (seconds), averagePace (min/km), totalCalories]
     */
    public float[] getStatistics() {
        List<Run> runs = getAllRuns();
        float totalDistance = 0;
        int totalDuration = 0;
        int totalCalories = 0;
        
        for (Run run : runs) {
            totalDistance += run.getDistanceInMeters();
            totalDuration += run.getDurationInSeconds();
            totalCalories += run.getCaloriesBurned();
        }
        
        // Calculate average pace (min/km)
        float averagePace = 0;
        if (totalDistance > 0) {
            averagePace = (totalDuration / 60f) / (totalDistance / 1000f);
        }
        
        return new float[]{totalDistance, totalDuration, averagePace, totalCalories};
    }
    
    /**
     * Save runs to SharedPreferences
     */
    private void saveRuns(List<Run> runs) {
        String runsJson = gson.toJson(runs);
        sharedPreferences.edit().putString(KEY_RUNS, runsJson).apply();
    }
}