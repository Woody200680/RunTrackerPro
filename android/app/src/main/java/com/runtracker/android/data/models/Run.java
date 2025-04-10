package com.runtracker.android.data.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Run model class to store information about a running activity
 */
public class Run implements Serializable {
    
    private final String id;
    private final long startTime;
    private long endTime;
    private final List<LocationPoint> locationPoints;
    private final List<PauseInterval> pauseIntervals;
    private long totalTimePaused;
    private double totalDistance; // in kilometers
    private double pace; // in minutes per kilometer
    private int caloriesBurned;
    
    /**
     * Creates a new Run instance with the current time as start time
     */
    public Run() {
        this.id = UUID.randomUUID().toString();
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;
        this.locationPoints = new ArrayList<>();
        this.pauseIntervals = new ArrayList<>();
        this.totalTimePaused = 0;
        this.totalDistance = 0;
        this.pace = 0;
        this.caloriesBurned = 0;
    }
    
    /**
     * Add a location point to the run
     * @param latitude Latitude
     * @param longitude Longitude
     * @param timestamp Timestamp of the location point
     */
    public void addLocationPoint(double latitude, double longitude, long timestamp) {
        LocationPoint point = new LocationPoint(latitude, longitude, timestamp);
        locationPoints.add(point);
        
        // Update total distance if we have at least two points
        if (locationPoints.size() > 1) {
            LocationPoint previousPoint = locationPoints.get(locationPoints.size() - 2);
            double distance = calculateDistance(
                    previousPoint.latitude, previousPoint.longitude,
                    latitude, longitude);
            totalDistance += distance;
            
            // Update pace if we have a valid duration and distance
            long activeDuration = getActiveDuration();
            if (activeDuration > 0 && totalDistance > 0) {
                pace = (activeDuration / 60000.0) / totalDistance; // Convert ms to minutes
            }
        }
    }
    
    /**
     * Start a pause interval
     * @param timestamp Timestamp when the pause started
     */
    public void pause(long timestamp) {
        PauseInterval pauseInterval = new PauseInterval(timestamp);
        pauseIntervals.add(pauseInterval);
    }
    
    /**
     * End the current pause interval
     * @param timestamp Timestamp when the pause ended
     */
    public void resume(long timestamp) {
        if (!pauseIntervals.isEmpty()) {
            PauseInterval lastPause = pauseIntervals.get(pauseIntervals.size() - 1);
            if (!lastPause.isEnded()) {
                lastPause.setEndTime(timestamp);
                totalTimePaused += lastPause.getDuration();
            }
        }
    }
    
    /**
     * End the run
     * @param timestamp Timestamp when the run ended
     * @param caloriesBurned Estimated calories burned
     */
    public void finish(long timestamp, int caloriesBurned) {
        this.endTime = timestamp;
        this.caloriesBurned = caloriesBurned;
        
        // End any open pause interval
        if (!pauseIntervals.isEmpty()) {
            PauseInterval lastPause = pauseIntervals.get(pauseIntervals.size() - 1);
            if (!lastPause.isEnded()) {
                lastPause.setEndTime(timestamp);
                totalTimePaused += lastPause.getDuration();
            }
        }
    }
    
    /**
     * Get the total duration of the run in milliseconds
     * @return Total duration in milliseconds
     */
    public long getTotalDuration() {
        if (endTime > 0) {
            return endTime - startTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }
    
    /**
     * Get the active duration (total duration - pause duration) in milliseconds
     * @return Active duration in milliseconds
     */
    public long getActiveDuration() {
        return getTotalDuration() - totalTimePaused;
    }
    
    /**
     * Check if the run is paused
     * @return True if the run is paused, false otherwise
     */
    public boolean isPaused() {
        if (pauseIntervals.isEmpty()) {
            return false;
        }
        
        PauseInterval lastPause = pauseIntervals.get(pauseIntervals.size() - 1);
        return !lastPause.isEnded();
    }
    
    /**
     * Check if the run is finished
     * @return True if the run is finished, false otherwise
     */
    public boolean isFinished() {
        return endTime > 0;
    }
    
    /**
     * Calculate distance between two points using the Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    // Getter methods
    
    public String getId() {
        return id;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public List<LocationPoint> getLocationPoints() {
        return locationPoints;
    }
    
    public List<PauseInterval> getPauseIntervals() {
        return pauseIntervals;
    }
    
    public long getTotalTimePaused() {
        return totalTimePaused;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public double getPace() {
        return pace;
    }
    
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    
    /**
     * LocationPoint inner class to store location data
     */
    public static class LocationPoint implements Serializable {
        private final double latitude;
        private final double longitude;
        private final long timestamp;
        
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
     * PauseInterval inner class to store pause intervals
     */
    public static class PauseInterval implements Serializable {
        private final long startTime;
        private long endTime;
        
        public PauseInterval(long startTime) {
            this.startTime = startTime;
            this.endTime = 0;
        }
        
        public void setEndTime(long endTime) {
            this.endTime = endTime;
        }
        
        public boolean isEnded() {
            return endTime > 0;
        }
        
        public long getDuration() {
            if (endTime > 0) {
                return endTime - startTime;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public long getEndTime() {
            return endTime;
        }
    }
}