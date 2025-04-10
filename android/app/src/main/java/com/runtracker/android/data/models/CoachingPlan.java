package com.runtracker.android.data.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a coaching plan with workouts and goals
 */
public class CoachingPlan {
    
    // Plan types
    public static final int TYPE_BEGINNER = 1;
    public static final int TYPE_INTERMEDIATE = 2;
    public static final int TYPE_ADVANCED = 3;
    
    // Plan goals
    public static final int GOAL_5K = 1;
    public static final int GOAL_10K = 2;
    public static final int GOAL_HALF_MARATHON = 3;
    public static final int GOAL_MARATHON = 4;
    public static final int GOAL_WEIGHT_LOSS = 5;
    public static final int GOAL_ENDURANCE = 6;
    
    private String id;
    private String name;
    private String description;
    private int type;
    private int goal;
    private int durationWeeks;
    private List<CoachingWorkout> workouts;
    private boolean active;
    private int currentWeek;
    private int currentDay;
    
    /**
     * Constructor for creating a new coaching plan
     */
    public CoachingPlan(String id, String name, String description, int type, int goal, int durationWeeks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.goal = goal;
        this.durationWeeks = durationWeeks;
        this.workouts = new ArrayList<>();
        this.active = false;
        this.currentWeek = 1;
        this.currentDay = 1;
    }
    
    /**
     * Get the coaching plan ID
     * @return Plan ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get the coaching plan name
     * @return Plan name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the coaching plan description
     * @return Plan description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Get the coaching plan type
     * @return Plan type (beginner, intermediate, advanced)
     */
    public int getType() {
        return type;
    }
    
    /**
     * Get the coaching plan goal
     * @return Plan goal
     */
    public int getGoal() {
        return goal;
    }
    
    /**
     * Get the coaching plan duration in weeks
     * @return Plan duration in weeks
     */
    public int getDurationWeeks() {
        return durationWeeks;
    }
    
    /**
     * Get all workouts in this plan
     * @return List of workouts
     */
    public List<CoachingWorkout> getWorkouts() {
        return new ArrayList<>(workouts);
    }
    
    /**
     * Add a workout to this plan
     * @param workout Workout to add
     */
    public void addWorkout(CoachingWorkout workout) {
        if (workout != null) {
            workouts.add(workout);
        }
    }
    
    /**
     * Check if this plan is active
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Set the plan's active status
     * @param active Active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Get the current week in the plan
     * @return Current week (1-based)
     */
    public int getCurrentWeek() {
        return currentWeek;
    }
    
    /**
     * Set the current week in the plan
     * @param currentWeek Current week (1-based)
     */
    public void setCurrentWeek(int currentWeek) {
        if (currentWeek >= 1 && currentWeek <= durationWeeks) {
            this.currentWeek = currentWeek;
        }
    }
    
    /**
     * Get the current day in the week
     * @return Current day (1-based, 1=Monday, 7=Sunday)
     */
    public int getCurrentDay() {
        return currentDay;
    }
    
    /**
     * Set the current day in the week
     * @param currentDay Current day (1-based, 1=Monday, 7=Sunday)
     */
    public void setCurrentDay(int currentDay) {
        if (currentDay >= 1 && currentDay <= 7) {
            this.currentDay = currentDay;
        }
    }
    
    /**
     * Get workouts for the current week
     * @return List of workouts for the current week
     */
    public List<CoachingWorkout> getCurrentWeekWorkouts() {
        List<CoachingWorkout> weekWorkouts = new ArrayList<>();
        for (CoachingWorkout workout : workouts) {
            if (workout.getWeek() == currentWeek) {
                weekWorkouts.add(workout);
            }
        }
        return weekWorkouts;
    }
    
    /**
     * Get the next scheduled workout
     * @return Next scheduled workout or null if no more workouts
     */
    public CoachingWorkout getNextWorkout() {
        // First, try to find a workout for the current day and week
        for (CoachingWorkout workout : workouts) {
            if (workout.getWeek() == currentWeek && workout.getDayOfWeek() == currentDay) {
                return workout;
            }
        }
        
        // Look ahead in the current week
        for (int day = currentDay + 1; day <= 7; day++) {
            for (CoachingWorkout workout : workouts) {
                if (workout.getWeek() == currentWeek && workout.getDayOfWeek() == day) {
                    return workout;
                }
            }
        }
        
        // Look ahead in future weeks
        for (int week = currentWeek + 1; week <= durationWeeks; week++) {
            for (int day = 1; day <= 7; day++) {
                for (CoachingWorkout workout : workouts) {
                    if (workout.getWeek() == week && workout.getDayOfWeek() == day) {
                        return workout;
                    }
                }
            }
        }
        
        // No more workouts found
        return null;
    }
    
    /**
     * Get the type name based on type constant
     * @param type Type constant
     * @return Type name
     */
    public static String getTypeName(int type) {
        switch (type) {
            case TYPE_BEGINNER:
                return "Beginner";
            case TYPE_INTERMEDIATE:
                return "Intermediate";
            case TYPE_ADVANCED:
                return "Advanced";
            default:
                return "Unknown";
        }
    }
    
    /**
     * Get the goal name based on goal constant
     * @param goal Goal constant
     * @return Goal name
     */
    public static String getGoalName(int goal) {
        switch (goal) {
            case GOAL_5K:
                return "5K";
            case GOAL_10K:
                return "10K";
            case GOAL_HALF_MARATHON:
                return "Half Marathon";
            case GOAL_MARATHON:
                return "Marathon";
            case GOAL_WEIGHT_LOSS:
                return "Weight Loss";
            case GOAL_ENDURANCE:
                return "Endurance";
            default:
                return "Unknown";
        }
    }
}