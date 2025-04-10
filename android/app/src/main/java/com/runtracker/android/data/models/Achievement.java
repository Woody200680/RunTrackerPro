package com.runtracker.android.data.models;

/**
 * Model class for an achievement
 */
public class Achievement {
    
    // Achievement types
    public static final int TYPE_DISTANCE = 1;
    public static final int TYPE_RUNS = 2;
    public static final int TYPE_STREAK = 3;
    public static final int TYPE_PACE = 4;
    public static final int TYPE_DURATION = 5;
    
    // Achievement levels
    public static final int LEVEL_BRONZE = 1;
    public static final int LEVEL_SILVER = 2;
    public static final int LEVEL_GOLD = 3;
    
    private final String id;
    private final String title;
    private final String description;
    private final int type;
    private final int level;
    private final double targetValue;
    private boolean unlocked;
    private long unlockedDate;
    
    /**
     * Constructor for creating a new achievement
     * 
     * @param id Unique ID
     * @param title Achievement title
     * @param description Achievement description
     * @param type Achievement type (one of TYPE_* constants)
     * @param level Achievement level (one of LEVEL_* constants)
     * @param targetValue Target value to unlock this achievement
     */
    public Achievement(String id, String title, String description, int type, int level, double targetValue) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.level = level;
        this.targetValue = targetValue;
        this.unlocked = false;
        this.unlockedDate = 0L;
    }
    
    /**
     * Constructor with unlocked status
     */
    public Achievement(String id, String title, String description, int type, int level, 
                      double targetValue, boolean unlocked, long unlockedDate) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.level = level;
        this.targetValue = targetValue;
        this.unlocked = unlocked;
        this.unlockedDate = unlockedDate;
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getType() {
        return type;
    }
    
    public int getLevel() {
        return level;
    }
    
    public double getTargetValue() {
        return targetValue;
    }
    
    public boolean isUnlocked() {
        return unlocked;
    }
    
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
    
    public long getUnlockedDate() {
        return unlockedDate;
    }
    
    public void setUnlockedDate(long unlockedDate) {
        this.unlockedDate = unlockedDate;
    }
    
    /**
     * Get icon resource ID based on type and level
     * @return Resource ID for the achievement icon
     */
    public int getIconResourceId() {
        // This would return the appropriate icon based on type and level
        // We'll implement this with actual resource IDs once the icons are created
        return 0;
    }
    
    /**
     * Check if the current value meets the target for this achievement
     * @param currentValue Current value to check
     * @return true if target is met
     */
    public boolean checkIsAchieved(double currentValue) {
        return currentValue >= targetValue;
    }
    
    /**
     * Calculate progress percentage towards achievement
     * @param currentValue Current value
     * @return Percentage (0-100) of progress
     */
    public int calculateProgress(double currentValue) {
        if (targetValue <= 0) return 0;
        
        int progress = (int) (currentValue / targetValue * 100);
        return Math.min(100, Math.max(0, progress));
    }
    
    /**
     * Create a clone of this achievement
     * @return Cloned achievement
     */
    public Achievement clone() {
        return new Achievement(id, title, description, type, level, targetValue, unlocked, unlockedDate);
    }
}