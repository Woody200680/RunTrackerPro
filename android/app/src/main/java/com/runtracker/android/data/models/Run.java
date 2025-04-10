package com.runtracker.android.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing a run
 */
public class Run implements Serializable {
    
    private String id;
    private long startTime;
    private long endTime;
    private long totalDuration; // in seconds (excluding pauses)
    private double totalDistance; // in kilometers
    private double averagePace; // in minutes per kilometer
    private int caloriesBurned;
    private List<LocationPoint> route;
    private List<PauseInterval> pauseIntervals;
    private RunStatus status;
    
    /**
     * Model class representing a location point (latitude, longitude)
     */
    public static class LocationPoint implements Serializable {
        private double latitude;
        private double longitude;
        private long timestamp;
        
        public LocationPoint(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
        
        public double getLatitude() {
            return latitude;
        }
        
        public double getLongitude() {
            return longitude;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Model class representing a pause interval during a run
     */
    public static class PauseInterval implements Serializable {
        private long pauseTime;
        private long resumeTime;
        
        public PauseInterval(long pauseTime) {
            this.pauseTime = pauseTime;
        }
        
        public void setResumeTime(long resumeTime) {
            this.resumeTime = resumeTime;
        }
        
        public long getPauseTime() {
            return pauseTime;
        }
        
        public long getResumeTime() {
            return resumeTime;
        }
        
        public long getDuration() {
            return resumeTime > 0 ? resumeTime - pauseTime : 0;
        }
    }
    
    /**
     * Enum representing the status of a run
     */
    public enum RunStatus {
        ACTIVE,
        PAUSED,
        COMPLETED
    }
    
    /**
     * Default constructor for creating a new run
     */
    public Run() {
        this.id = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
        this.status = RunStatus.ACTIVE;
        this.route = new ArrayList<>();
        this.pauseIntervals = new ArrayList<>();
        this.totalDistance = 0;
        this.totalDuration = 0;
        this.averagePace = 0;
        this.caloriesBurned = 0;
    }
    
    /**
     * Add a location point to the route
     */
    public void addLocationPoint(double latitude, double longitude) {
        if (status == RunStatus.ACTIVE) {
            this.route.add(new LocationPoint(latitude, longitude, System.currentTimeMillis()));
            updateMetrics();
        }
    }
    
    /**
     * Pause the run
     */
    public void pause() {
        if (status == RunStatus.ACTIVE) {
            status = RunStatus.PAUSED;
            pauseIntervals.add(new PauseInterval(System.currentTimeMillis()));
        }
    }
    
    /**
     * Resume the run
     */
    public void resume() {
        if (status == RunStatus.PAUSED && !pauseIntervals.isEmpty()) {
            status = RunStatus.ACTIVE;
            PauseInterval lastPause = pauseIntervals.get(pauseIntervals.size() - 1);
            lastPause.setResumeTime(System.currentTimeMillis());
        }
    }
    
    /**
     * Complete the run
     */
    public void complete() {
        if (status != RunStatus.COMPLETED) {
            status = RunStatus.COMPLETED;
            endTime = System.currentTimeMillis();
            updateMetrics();
        }
    }
    
    /**
     * Update the run metrics (distance, duration, pace)
     */
    private void updateMetrics() {
        calculateTotalDistance();
        calculateTotalDuration();
        calculateAveragePace();
        estimateCaloriesBurned();
    }
    
    /**
     * Calculate the total distance of the run using the Haversine formula
     */
    private void calculateTotalDistance() {
        double total = 0;
        
        if (route.size() < 2) {
            totalDistance = 0;
            return;
        }
        
        for (int i = 0; i < route.size() - 1; i++) {
            LocationPoint p1 = route.get(i);
            LocationPoint p2 = route.get(i + 1);
            
            total += calculateDistanceBetween(
                    p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude());
        }
        
        totalDistance = total;
    }
    
    /**
     * Calculate the distance between two points using the Haversine formula
     */
    private double calculateDistanceBetween(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
    
    /**
     * Calculate the total duration of the run in seconds
     */
    private void calculateTotalDuration() {
        long pausedTime = 0;
        for (PauseInterval pause : pauseIntervals) {
            if (pause.getResumeTime() > 0) {
                pausedTime += pause.getDuration();
            } else if (status == RunStatus.PAUSED) {
                pausedTime += (System.currentTimeMillis() - pause.getPauseTime());
            }
        }
        
        long end = (status == RunStatus.COMPLETED) ? endTime : System.currentTimeMillis();
        totalDuration = (end - startTime - pausedTime) / 1000; // Convert to seconds
    }
    
    /**
     * Calculate the average pace in minutes per kilometer
     */
    private void calculateAveragePace() {
        if (totalDistance > 0 && totalDuration > 0) {
            // Pace is minutes per kilometer
            averagePace = (totalDuration / 60.0) / totalDistance;
        } else {
            averagePace = 0;
        }
    }
    
    /**
     * Estimate calories burned based on distance and duration
     * This is a very simplified estimation
     */
    private void estimateCaloriesBurned() {
        // Simplified formula: ~62 calories per kilometer for an average person
        caloriesBurned = (int) (totalDistance * 62);
    }

    // Getters
    
    public String getId() {
        return id;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public long getTotalDuration() {
        return totalDuration;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public double getAveragePace() {
        return averagePace;
    }
    
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public List<LocationPoint> getRoute() {
        return route;
    }
    
    public RunStatus getStatus() {
        return status;
    }
}