package com.runtracker.android.data.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class for coaching workouts
 */
public class CoachingWorkout {

    // Workout types
    public static final int TYPE_ENDURANCE = 0;
    public static final int TYPE_SPEED = 1;
    public static final int TYPE_INTERVAL = 2;
    public static final int TYPE_TEMPO = 3;
    public static final int TYPE_RECOVERY = 4;
    public static final int TYPE_RACE = 5;
    
    // Workout state
    public static final int STATE_SCHEDULED = 0;
    public static final int STATE_COMPLETED = 1;
    public static final int STATE_SKIPPED = 2;
    
    // Fields
    private String id;
    private String planId;
    private String name;
    private String description;
    private int type;
    private int week;
    private int dayOfWeek;
    private int state;
    private String completedRunId;
    private long scheduledDate;
    private final List<WorkoutSegment> segments;
    
    /**
     * Constructor with auto-generated ID
     */
    public CoachingWorkout() {
        this.id = UUID.randomUUID().toString();
        this.segments = new ArrayList<>();
        this.state = STATE_SCHEDULED;
    }
    
    /**
     * Constructor with ID
     * @param id Workout ID
     */
    public CoachingWorkout(String id) {
        this.id = id;
        this.segments = new ArrayList<>();
        this.state = STATE_SCHEDULED;
    }
    
    /**
     * Get workout ID
     * @return Workout ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set workout ID
     * @param id Workout ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Get plan ID
     * @return Plan ID
     */
    public String getPlanId() {
        return planId;
    }
    
    /**
     * Set plan ID
     * @param planId Plan ID
     */
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    /**
     * Get workout name
     * @return Workout name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set workout name
     * @param name Workout name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get workout description
     * @return Workout description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set workout description
     * @param description Workout description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get workout type
     * @return Workout type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Set workout type
     * @param type Workout type
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * Get week number
     * @return Week number
     */
    public int getWeek() {
        return week;
    }
    
    /**
     * Set week number
     * @param week Week number
     */
    public void setWeek(int week) {
        this.week = week;
    }
    
    /**
     * Get day of week
     * @return Day of week (1-7)
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    /**
     * Set day of week
     * @param dayOfWeek Day of week (1-7)
     */
    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    /**
     * Get workout state
     * @return Workout state
     */
    public int getState() {
        return state;
    }
    
    /**
     * Set workout state
     * @param state Workout state
     */
    public void setState(int state) {
        this.state = state;
    }
    
    /**
     * Get completed run ID
     * @return Completed run ID
     */
    public String getCompletedRunId() {
        return completedRunId;
    }
    
    /**
     * Set completed run ID
     * @param completedRunId Completed run ID
     */
    public void setCompletedRunId(String completedRunId) {
        this.completedRunId = completedRunId;
        if (completedRunId != null && !completedRunId.isEmpty()) {
            this.state = STATE_COMPLETED;
        }
    }
    
    /**
     * Get scheduled date
     * @return Scheduled date (timestamp)
     */
    public long getScheduledDate() {
        return scheduledDate;
    }
    
    /**
     * Set scheduled date
     * @param scheduledDate Scheduled date (timestamp)
     */
    public void setScheduledDate(long scheduledDate) {
        this.scheduledDate = scheduledDate;
    }
    
    /**
     * Get workout segments
     * @return List of workout segments
     */
    public List<WorkoutSegment> getSegments() {
        return segments;
    }
    
    /**
     * Add a segment to the workout
     * @param segment Workout segment
     */
    public void addSegment(WorkoutSegment segment) {
        segments.add(segment);
    }
    
    /**
     * Get total duration of the workout in seconds
     * @return Total duration in seconds
     */
    public long getTotalDuration() {
        long total = 0;
        for (WorkoutSegment segment : segments) {
            total += segment.getDuration();
        }
        return total;
    }
    
    /**
     * Get estimated distance of the workout in kilometers
     * @return Estimated distance in kilometers
     */
    public double getEstimatedDistance() {
        double total = 0;
        
        // Calculate based on pace and duration
        for (WorkoutSegment segment : segments) {
            // Skip rest segments
            if (segment.getType() == WorkoutSegment.TYPE_REST) {
                continue;
            }
            
            // Calculate distance based on average pace
            double avgPace = (segment.getTargetPaceMin() + segment.getTargetPaceMax()) / 2;
            if (avgPace > 0) {
                // Pace is in minutes per km, convert to km
                double km = segment.getDuration() / 60.0 / avgPace;
                total += km;
            }
        }
        
        return total;
    }
    
    /**
     * Get workout type name
     * @param type Workout type
     * @return Workout type name
     */
    public static String getTypeName(int type) {
        switch (type) {
            case TYPE_ENDURANCE:
                return "Endurance";
            case TYPE_SPEED:
                return "Speed";
            case TYPE_INTERVAL:
                return "Interval";
            case TYPE_TEMPO:
                return "Tempo";
            case TYPE_RECOVERY:
                return "Recovery";
            case TYPE_RACE:
                return "Race";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Workout segment
     */
    public static class WorkoutSegment {
        
        // Segment types
        public static final int TYPE_WARMUP = 0;
        public static final int TYPE_ACTIVE = 1;
        public static final int TYPE_REST = 2;
        public static final int TYPE_RECOVERY = 3;
        public static final int TYPE_COOLDOWN = 4;
        
        // Intensity levels
        public static final int INTENSITY_EASY = 0;
        public static final int INTENSITY_MODERATE = 1;
        public static final int INTENSITY_HARD = 2;
        public static final int INTENSITY_MAX = 3;
        
        // Fields
        private int type;
        private int duration; // in seconds
        private double targetPaceMin; // in minutes per km
        private double targetPaceMax; // in minutes per km
        private int intensity;
        private int repeats;
        private String instructions;
        
        /**
         * Constructor
         */
        public WorkoutSegment() {
            this.repeats = 1;
        }
        
        /**
         * Constructor with type and duration
         * @param type Segment type
         * @param duration Duration in seconds
         */
        public WorkoutSegment(int type, int duration) {
            this.type = type;
            this.duration = duration;
            this.repeats = 1;
        }
        
        /**
         * Get segment type
         * @return Segment type
         */
        public int getType() {
            return type;
        }
        
        /**
         * Set segment type
         * @param type Segment type
         */
        public void setType(int type) {
            this.type = type;
        }
        
        /**
         * Get segment duration
         * @return Duration in seconds
         */
        public int getDuration() {
            return duration;
        }
        
        /**
         * Set segment duration
         * @param duration Duration in seconds
         */
        public void setDuration(int duration) {
            this.duration = duration;
        }
        
        /**
         * Get minimum target pace
         * @return Minimum target pace in minutes per km
         */
        public double getTargetPaceMin() {
            return targetPaceMin;
        }
        
        /**
         * Set minimum target pace
         * @param targetPaceMin Minimum target pace in minutes per km
         */
        public void setTargetPaceMin(double targetPaceMin) {
            this.targetPaceMin = targetPaceMin;
        }
        
        /**
         * Get maximum target pace
         * @return Maximum target pace in minutes per km
         */
        public double getTargetPaceMax() {
            return targetPaceMax;
        }
        
        /**
         * Set maximum target pace
         * @param targetPaceMax Maximum target pace in minutes per km
         */
        public void setTargetPaceMax(double targetPaceMax) {
            this.targetPaceMax = targetPaceMax;
        }
        
        /**
         * Get segment intensity
         * @return Segment intensity
         */
        public int getIntensity() {
            return intensity;
        }
        
        /**
         * Set segment intensity
         * @param intensity Segment intensity
         */
        public void setIntensity(int intensity) {
            this.intensity = intensity;
        }
        
        /**
         * Get number of repeats
         * @return Number of repeats
         */
        public int getRepeats() {
            return repeats;
        }
        
        /**
         * Set number of repeats
         * @param repeats Number of repeats
         */
        public void setRepeats(int repeats) {
            this.repeats = repeats;
        }
        
        /**
         * Get segment instructions
         * @return Segment instructions
         */
        public String getInstructions() {
            return instructions;
        }
        
        /**
         * Set segment instructions
         * @param instructions Segment instructions
         */
        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }
        
        /**
         * Get segment type name
         * @param type Segment type
         * @return Segment type name
         */
        public static String getTypeName(int type) {
            switch (type) {
                case TYPE_WARMUP:
                    return "Warm Up";
                case TYPE_ACTIVE:
                    return "Active";
                case TYPE_REST:
                    return "Rest";
                case TYPE_RECOVERY:
                    return "Recovery";
                case TYPE_COOLDOWN:
                    return "Cool Down";
                default:
                    return "Unknown";
            }
        }
        
        /**
         * Get intensity name
         * @param intensity Intensity level
         * @return Intensity name
         */
        public static String getIntensityName(int intensity) {
            switch (intensity) {
                case INTENSITY_EASY:
                    return "Easy";
                case INTENSITY_MODERATE:
                    return "Moderate";
                case INTENSITY_HARD:
                    return "Hard";
                case INTENSITY_MAX:
                    return "Maximum";
                default:
                    return "Unknown";
            }
        }
    }
}