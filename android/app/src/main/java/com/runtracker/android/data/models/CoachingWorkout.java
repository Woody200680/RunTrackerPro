package com.runtracker.android.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a coaching workout with segments
 */
public class CoachingWorkout {
    
    // Workout types
    public static final int TYPE_EASY_RUN = 1;
    public static final int TYPE_TEMPO_RUN = 2;
    public static final int TYPE_INTERVAL = 3;
    public static final int TYPE_LONG_RUN = 4;
    public static final int TYPE_RECOVERY = 5;
    public static final int TYPE_FARTLEK = 6;
    public static final int TYPE_HILL_REPEATS = 7;
    public static final int TYPE_TIME_TRIAL = 8;
    public static final int TYPE_CROSS_TRAINING = 9;
    public static final int TYPE_REST = 10;
    
    private String id;
    private String name;
    private String description;
    private int type;
    private int week;
    private int dayOfWeek;
    private List<WorkoutSegment> segments;
    private String planId;
    private boolean completed;
    private String runId; // ID of the associated run if completed
    
    /**
     * Constructor for creating a new coaching workout
     */
    public CoachingWorkout(String id, String name, String description, int type, 
                          int week, int dayOfWeek, String planId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.week = week;
        this.dayOfWeek = dayOfWeek;
        this.planId = planId;
        this.segments = new ArrayList<>();
        this.completed = false;
        this.runId = null;
    }
    
    /**
     * Get the workout ID
     * @return Workout ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the workout name
     * @return Workout name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the workout description
     * @return Workout description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the workout type
     * @return Workout type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Get the week number (1-based)
     * @return Week number
     */
    public int getWeek() {
        return week;
    }
    
    /**
     * Get the day of week (1-based, 1=Monday, 7=Sunday)
     * @return Day of week
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }
    
    /**
     * Get all segments in this workout
     * @return List of workout segments
     */
    public List<WorkoutSegment> getSegments() {
        return new ArrayList<>(segments);
    }
    
    /**
     * Add a segment to this workout
     * @param segment Segment to add
     */
    public void addSegment(WorkoutSegment segment) {
        if (segment != null) {
            segments.add(segment);
        }
    }
    
    /**
     * Get the plan ID
     * @return Plan ID
     */
    public String getPlanId() {
        return planId;
    }
    
    /**
     * Check if this workout is completed
     * @return true if completed
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Set the workout's completed status
     * @param completed Completed status
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    /**
     * Get the associated run ID
     * @return Run ID or null if not completed
     */
    public String getRunId() {
        return runId;
    }
    
    /**
     * Set the associated run ID
     * @param runId Run ID
     */
    public void setRunId(String runId) {
        this.runId = runId;
        
        if (runId != null) {
            this.completed = true;
        }
    }
    
    /**
     * Get the total duration of this workout in seconds
     * @return Total workout duration
     */
    public long getTotalDuration() {
        long total = 0;
        for (WorkoutSegment segment : segments) {
            total += segment.getDuration();
        }
        return total;
    }
    
    /**
     * Get the estimated distance of this workout in kilometers
     * @return Estimated workout distance
     */
    public double getEstimatedDistance() {
        double total = 0;
        for (WorkoutSegment segment : segments) {
            if (segment.getType() != WorkoutSegment.TYPE_REST) {
                // Estimate distance based on duration and target pace
                double durationHours = segment.getDuration() / 3600.0;
                double paceMinsPerKm = segment.getTargetPaceMax();
                
                if (paceMinsPerKm > 0) {
                    // Convert pace from mins/km to km/hour
                    double speedKmPerHour = 60.0 / paceMinsPerKm;
                    total += speedKmPerHour * durationHours;
                }
            }
        }
        return total;
    }
    
    /**
     * Get the type name based on type constant
     * @param type Type constant
     * @return Type name
     */
    public static String getTypeName(int type) {
        switch (type) {
            case TYPE_EASY_RUN:
                return "Easy Run";
            case TYPE_TEMPO_RUN:
                return "Tempo Run";
            case TYPE_INTERVAL:
                return "Interval Training";
            case TYPE_LONG_RUN:
                return "Long Run";
            case TYPE_RECOVERY:
                return "Recovery Run";
            case TYPE_FARTLEK:
                return "Fartlek";
            case TYPE_HILL_REPEATS:
                return "Hill Repeats";
            case TYPE_TIME_TRIAL:
                return "Time Trial";
            case TYPE_CROSS_TRAINING:
                return "Cross Training";
            case TYPE_REST:
                return "Rest Day";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Class representing a segment of a workout
     */
    public static class WorkoutSegment {
        // Segment types
        public static final int TYPE_WARMUP = 1;
        public static final int TYPE_COOLDOWN = 2;
        public static final int TYPE_ACTIVE = 3;
        public static final int TYPE_REST = 4;
        public static final int TYPE_RECOVERY = 5;
        
        // Intensity levels
        public static final int INTENSITY_EASY = 1;
        public static final int INTENSITY_MODERATE = 2;
        public static final int INTENSITY_HARD = 3;
        public static final int INTENSITY_MAX = 4;
        
        private int type;
        private int duration; // in seconds
        private int repeatCount;
        private int intensity;
        private double targetPaceMin; // min/km
        private double targetPaceMax; // min/km
        private String instructions;
        
        /**
         * Constructor for creating a new workout segment
         */
        public WorkoutSegment(int type, int duration, int repeatCount, int intensity,
                             double targetPaceMin, double targetPaceMax, String instructions) {
            this.type = type;
            this.duration = duration;
            this.repeatCount = repeatCount;
            this.intensity = intensity;
            this.targetPaceMin = targetPaceMin;
            this.targetPaceMax = targetPaceMax;
            this.instructions = instructions;
        }
        
        /**
         * Get the segment type
         * @return Segment type
         */
        public int getType() {
            return type;
        }
        
        /**
         * Get the segment duration in seconds
         * @return Segment duration
         */
        public int getDuration() {
            return duration;
        }
        
        /**
         * Get the repeat count
         * @return Repeat count
         */
        public int getRepeatCount() {
            return repeatCount;
        }
        
        /**
         * Get the segment intensity
         * @return Segment intensity
         */
        public int getIntensity() {
            return intensity;
        }
        
        /**
         * Get the minimum target pace in min/km
         * @return Minimum target pace
         */
        public double getTargetPaceMin() {
            return targetPaceMin;
        }
        
        /**
         * Get the maximum target pace in min/km
         * @return Maximum target pace
         */
        public double getTargetPaceMax() {
            return targetPaceMax;
        }
        
        /**
         * Get the segment instructions
         * @return Segment instructions
         */
        public String getInstructions() {
            return instructions;
        }
        
        /**
         * Get the segment total duration including repeats
         * @return Total segment duration
         */
        public int getTotalDuration() {
            return duration * Math.max(1, repeatCount);
        }
        
        /**
         * Get the type name based on type constant
         * @param type Type constant
         * @return Type name
         */
        public static String getTypeName(int type) {
            switch (type) {
                case TYPE_WARMUP:
                    return "Warm Up";
                case TYPE_COOLDOWN:
                    return "Cool Down";
                case TYPE_ACTIVE:
                    return "Active";
                case TYPE_REST:
                    return "Rest";
                case TYPE_RECOVERY:
                    return "Recovery";
                default:
                    return "Unknown";
            }
        }
        
        /**
         * Get the intensity name based on intensity constant
         * @param intensity Intensity constant
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