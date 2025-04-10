package com.runtracker.android.data.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class for coaching plans
 */
public class CoachingPlan {

    // Difficulty levels
    public static final int DIFFICULTY_BEGINNER = 0;
    public static final int DIFFICULTY_INTERMEDIATE = 1;
    public static final int DIFFICULTY_ADVANCED = 2;
    
    // Goals
    public static final int GOAL_FITNESS = 0;
    public static final int GOAL_WEIGHT_LOSS = 1;
    public static final int GOAL_5K = 2;
    public static final int GOAL_10K = 3;
    public static final int GOAL_HALF_MARATHON = 4;
    public static final int GOAL_MARATHON = 5;
    
    // Fields
    private String id;
    private String name;
    private String description;
    private int difficulty;
    private int goal;
    private int durationWeeks;
    private int workoutsPerWeek;
    private String thumbnailUrl;
    private long createdAt;
    private final List<CoachingWorkout> workouts;
    
    /**
     * Constructor with auto-generated ID
     */
    public CoachingPlan() {
        this.id = UUID.randomUUID().toString();
        this.workouts = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }
    
    /**
     * Constructor with ID
     * @param id Plan ID
     */
    public CoachingPlan(String id) {
        this.id = id;
        this.workouts = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }
    
    /**
     * Get plan ID
     * @return Plan ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set plan ID
     * @param id Plan ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Get plan name
     * @return Plan name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set plan name
     * @param name Plan name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get plan description
     * @return Plan description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set plan description
     * @param description Plan description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get plan difficulty
     * @return Plan difficulty
     */
    public int getDifficulty() {
        return difficulty;
    }
    
    /**
     * Set plan difficulty
     * @param difficulty Plan difficulty
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    
    /**
     * Get plan goal
     * @return Plan goal
     */
    public int getGoal() {
        return goal;
    }
    
    /**
     * Set plan goal
     * @param goal Plan goal
     */
    public void setGoal(int goal) {
        this.goal = goal;
    }
    
    /**
     * Get plan duration in weeks
     * @return Plan duration in weeks
     */
    public int getDurationWeeks() {
        return durationWeeks;
    }
    
    /**
     * Set plan duration in weeks
     * @param durationWeeks Plan duration in weeks
     */
    public void setDurationWeeks(int durationWeeks) {
        this.durationWeeks = durationWeeks;
    }
    
    /**
     * Get workouts per week
     * @return Workouts per week
     */
    public int getWorkoutsPerWeek() {
        return workoutsPerWeek;
    }
    
    /**
     * Set workouts per week
     * @param workoutsPerWeek Workouts per week
     */
    public void setWorkoutsPerWeek(int workoutsPerWeek) {
        this.workoutsPerWeek = workoutsPerWeek;
    }
    
    /**
     * Get thumbnail URL
     * @return Thumbnail URL
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    /**
     * Set thumbnail URL
     * @param thumbnailUrl Thumbnail URL
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    /**
     * Get creation timestamp
     * @return Creation timestamp
     */
    public long getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set creation timestamp
     * @param createdAt Creation timestamp
     */
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Get workouts
     * @return List of workouts
     */
    public List<CoachingWorkout> getWorkouts() {
        return workouts;
    }
    
    /**
     * Add a workout
     * @param workout Workout to add
     */
    public void addWorkout(CoachingWorkout workout) {
        workout.setPlanId(this.id);
        workouts.add(workout);
    }
    
    /**
     * Get workouts for a specific week
     * @param week Week number (1-based)
     * @return List of workouts for the week
     */
    public List<CoachingWorkout> getWorkoutsForWeek(int week) {
        List<CoachingWorkout> weekWorkouts = new ArrayList<>();
        for (CoachingWorkout workout : workouts) {
            if (workout.getWeek() == week) {
                weekWorkouts.add(workout);
            }
        }
        return weekWorkouts;
    }
    
    /**
     * Get the total number of workouts
     * @return Total number of workouts
     */
    public int getTotalWorkouts() {
        return workouts.size();
    }
    
    /**
     * Get the total completed workouts
     * @return Number of completed workouts
     */
    public int getCompletedWorkouts() {
        int count = 0;
        for (CoachingWorkout workout : workouts) {
            if (workout.getState() == CoachingWorkout.STATE_COMPLETED) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get completion percentage
     * @return Completion percentage (0-100)
     */
    public int getCompletionPercentage() {
        if (workouts.isEmpty()) {
            return 0;
        }
        return (int) ((float) getCompletedWorkouts() / workouts.size() * 100);
    }
    
    /**
     * Get difficulty name
     * @param difficulty Difficulty level
     * @return Difficulty name
     */
    public static String getDifficultyName(int difficulty) {
        switch (difficulty) {
            case DIFFICULTY_BEGINNER:
                return "Beginner";
            case DIFFICULTY_INTERMEDIATE:
                return "Intermediate";
            case DIFFICULTY_ADVANCED:
                return "Advanced";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get goal name
     * @param goal Goal type
     * @return Goal name
     */
    public static String getGoalName(int goal) {
        switch (goal) {
            case GOAL_FITNESS:
                return "General Fitness";
            case GOAL_WEIGHT_LOSS:
                return "Weight Loss";
            case GOAL_5K:
                return "5K Race";
            case GOAL_10K:
                return "10K Race";
            case GOAL_HALF_MARATHON:
                return "Half Marathon";
            case GOAL_MARATHON:
                return "Marathon";
            default:
                return "Unknown";
        }
    }
}