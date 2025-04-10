package com.runtracker.android.data.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing a run activity
 */
public class Run {
    private String id;
    private Date startTime;
    private Date endTime;
    private Date pauseTime;
    private Date resumeTime;
    private boolean isPaused;
    private int durationInSeconds;
    private float distanceInMeters;
    private float pace; // minutes per kilometer
    private int caloriesBurned;
    private List<String> routePoints;
    
    public Run() {
        this.id = UUID.randomUUID().toString();
        this.routePoints = new ArrayList<>();
        this.isPaused = false;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public Date getPauseTime() {
        return pauseTime;
    }
    
    public void setPauseTime(Date pauseTime) {
        this.pauseTime = pauseTime;
    }
    
    public Date getResumeTime() {
        return resumeTime;
    }
    
    public void setResumeTime(Date resumeTime) {
        this.resumeTime = resumeTime;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    
    public int getDurationInSeconds() {
        return durationInSeconds;
    }
    
    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
    
    public float getDistanceInMeters() {
        return distanceInMeters;
    }
    
    public void setDistanceInMeters(float distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }
    
    public float getPace() {
        return pace;
    }
    
    public void setPace(float pace) {
        this.pace = pace;
    }
    
    public int getCaloriesBurned() {
        return caloriesBurned;
    }
    
    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }
    
    public List<String> getRoutePoints() {
        return routePoints;
    }
    
    public void setRoutePoints(List<String> routePoints) {
        this.routePoints = routePoints;
    }
}