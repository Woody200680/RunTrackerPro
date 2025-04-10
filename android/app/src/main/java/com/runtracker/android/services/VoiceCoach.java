package com.runtracker.android.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.runtracker.android.R;
import com.runtracker.android.data.models.CoachingWorkout;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.utils.Constants;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Service that provides voice coaching during runs
 */
public class VoiceCoach {
    private static final String TAG = "VoiceCoach";
    
    // Coaching types
    public static final int COACHING_NONE = 0;
    public static final int COACHING_BASIC = 1;
    public static final int COACHING_WORKOUT = 2;
    
    // Target pace feedback
    private static final int PACE_FEEDBACK_PERFECT = 0;
    private static final int PACE_FEEDBACK_TOO_FAST = 1;
    private static final int PACE_FEEDBACK_TOO_SLOW = 2;
    private static final double PACE_TOLERANCE = 0.5; // min/km tolerance
    
    // Update interval for coaching (milliseconds)
    private static final long COACHING_INTERVAL = 30000; // 30 seconds
    
    private final Context context;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private final SharedPreferences preferences;
    private final Handler handler;
    private final Random random;
    
    // Active run info
    private Run activeRun;
    private long startTime;
    private int coachingType = COACHING_BASIC;
    private CoachingWorkout activeWorkout;
    private int currentSegmentIndex = 0;
    private int currentSegmentTimeRemaining = 0;
    private long lastCoachingTime = 0;
    private long lastSegmentAnnouncementTime = 0;
    private long lastPaceFeedbackTime = 0;
    private boolean isWorkoutStarted = false;
    private Runnable coachingRunnable;
    
    // Feedback pools for variety
    private List<String> encouragementMessages;
    private List<String> paceSlowerMessages;
    private List<String> paceFasterMessages;
    private List<String> paceGoodMessages;
    private List<String> intervalStartMessages;
    private List<String> intervalEndMessages;
    private List<String> workoutCompleteMessages;
    
    /**
     * Constructor
     * @param context The application context
     */
    public VoiceCoach(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();
        
        // Initialize TTS engine
        initTextToSpeech();
        
        // Initialize feedback message pools
        initFeedbackPools();
    }
    
    /**
     * Initialize Text-to-Speech engine
     */
    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.getDefault());
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported");
                } else {
                    isTtsReady = true;
                    textToSpeech.setSpeechRate(0.9f); // Slightly slower for clarity
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }
    
    /**
     * Initialize feedback message pools
     */
    private void initFeedbackPools() {
        // Initialize all feedback pools
        encouragementMessages = new ArrayList<>();
        encouragementMessages.add("You're doing great! Keep it up!");
        encouragementMessages.add("Looking strong! Keep pushing!");
        encouragementMessages.add("Excellent work! You've got this!");
        encouragementMessages.add("Great job! Stay focused!");
        encouragementMessages.add("You're making good progress!");
        
        paceSlowerMessages = new ArrayList<>();
        paceSlowerMessages.add("You're going a bit too fast. Try to slow down.");
        paceSlowerMessages.add("Ease off a little. Save energy for later.");
        paceSlowerMessages.add("Slow down to maintain your target pace.");
        paceSlowerMessages.add("Try to relax and reduce your pace slightly.");
        
        paceFasterMessages = new ArrayList<>();
        paceFasterMessages.add("Try to pick up the pace a bit.");
        paceFasterMessages.add("You can go a little faster. Push yourself!");
        paceFasterMessages.add("Increase your pace to reach your target.");
        paceFasterMessages.add("Try to speed up slightly to meet your goal.");
        
        paceGoodMessages = new ArrayList<>();
        paceGoodMessages.add("Great pace! Keep it steady.");
        paceGoodMessages.add("Perfect pace! You're right on target.");
        paceGoodMessages.add("You're maintaining an excellent pace!");
        paceGoodMessages.add("Perfectly on pace! Keep it up!");
        
        intervalStartMessages = new ArrayList<>();
        intervalStartMessages.add("Starting new interval. Push yourself!");
        intervalStartMessages.add("New interval beginning. Let's go!");
        intervalStartMessages.add("Next interval starting now. Give it your all!");
        
        intervalEndMessages = new ArrayList<>();
        intervalEndMessages.add("Interval complete. Good job!");
        intervalEndMessages.add("Interval finished. Take a breather.");
        intervalEndMessages.add("End of interval. Well done!");
        
        workoutCompleteMessages = new ArrayList<>();
        workoutCompleteMessages.add("Workout complete! Excellent job today!");
        workoutCompleteMessages.add("You've finished your workout! Great effort!");
        workoutCompleteMessages.add("Workout complete! You crushed it!");
    }
    
    /**
     * Start coaching for a run
     * @param run The active run
     * @param coachingType Type of coaching
     * @param workout The coaching workout (if workout-based coaching)
     */
    public void startCoaching(Run run, int coachingType, CoachingWorkout workout) {
        this.activeRun = run;
        this.startTime = System.currentTimeMillis();
        this.coachingType = coachingType;
        this.activeWorkout = workout;
        this.currentSegmentIndex = 0;
        this.lastCoachingTime = 0;
        this.lastSegmentAnnouncementTime = 0;
        this.lastPaceFeedbackTime = 0;
        this.isWorkoutStarted = false;
        
        // Stop any existing coaching
        stopCoaching();
        
        // If workout coaching, prepare the segments
        if (coachingType == COACHING_WORKOUT && workout != null) {
            if (!workout.getSegments().isEmpty()) {
                this.currentSegmentTimeRemaining = 
                    workout.getSegments().get(currentSegmentIndex).getDuration();
            }
        }
        
        // Start initial announcement
        announceStartCoaching();
        
        // Start coaching loop
        startCoachingLoop();
    }
    
    /**
     * Stop coaching
     */
    public void stopCoaching() {
        if (coachingRunnable != null) {
            handler.removeCallbacks(coachingRunnable);
            coachingRunnable = null;
        }
        
        this.activeRun = null;
        this.activeWorkout = null;
        this.isWorkoutStarted = false;
    }
    
    /**
     * Announce the start of coaching
     */
    private void announceStartCoaching() {
        if (!isTtsReady || !isCoachingEnabled()) {
            return;
        }
        
        StringBuilder announcement = new StringBuilder();
        
        if (coachingType == COACHING_WORKOUT && activeWorkout != null) {
            // Announce workout start
            announcement.append("Starting workout: ")
                .append(activeWorkout.getName())
                .append(". ");
            
            // Announce first segment if available
            if (!activeWorkout.getSegments().isEmpty()) {
                CoachingWorkout.WorkoutSegment segment = activeWorkout.getSegments().get(0);
                announcement.append("First segment: ")
                    .append(CoachingWorkout.WorkoutSegment.getTypeName(segment.getType()))
                    .append(" for ")
                    .append(FormatUtils.formatDurationWords(segment.getDuration()))
                    .append(". ");
                
                if (segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_REST && 
                    segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_RECOVERY) {
                    announcement.append("Target pace: ")
                        .append(FormatUtils.formatPaceRange(segment.getTargetPaceMin(), segment.getTargetPaceMax()))
                        .append(". ");
                }
            }
        } else {
            // Basic coaching start
            announcement.append("Starting run with coaching. I'll provide updates throughout your run.");
        }
        
        speak(announcement.toString());
    }
    
    /**
     * Start the coaching update loop
     */
    private void startCoachingLoop() {
        if (coachingRunnable != null) {
            handler.removeCallbacks(coachingRunnable);
        }
        
        coachingRunnable = new Runnable() {
            @Override
            public void run() {
                if (activeRun == null) {
                    return;
                }
                
                // Update coaching based on type
                if (coachingType == COACHING_WORKOUT) {
                    updateWorkoutCoaching();
                } else {
                    updateBasicCoaching();
                }
                
                // Schedule next update
                handler.postDelayed(this, 1000); // Check every second
            }
        };
        
        handler.post(coachingRunnable);
    }
    
    /**
     * Update coaching for basic (non-workout) mode
     */
    private void updateBasicCoaching() {
        long currentTime = System.currentTimeMillis();
        
        // Skip if coaching is not enabled or TTS is not ready
        if (!isCoachingEnabled() || !isTtsReady || activeRun == null) {
            return;
        }
        
        // Check if it's time for coaching update
        if (currentTime - lastCoachingTime >= COACHING_INTERVAL) {
            // Time for a coaching update!
            
            // Get current stats
            double distance = activeRun.getTotalDistance();
            long duration = activeRun.getActiveDuration();
            double pace = activeRun.getPace();
            
            // Prepare the coaching message
            StringBuilder message = new StringBuilder();
            
            // Add time and distance
            String distanceUnit = preferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
            boolean useMetric = distanceUnit.equals(Constants.UNIT_KM);
            
            double displayDistance = useMetric ? distance : distance * 0.621371; // Convert to miles if needed
            
            message.append("You've been running for ")
                .append(FormatUtils.formatDurationWords(duration))
                .append(" and covered ")
                .append(FormatUtils.formatDistanceWords(displayDistance, !useMetric))
                .append(". ");
                
            // Add current pace
            if (pace > 0) {
                message.append("Your current pace is ")
                    .append(FormatUtils.formatPaceWords(pace, !useMetric))
                    .append(". ");
            }
            
            // Add random encouragement
            message.append(getRandomMessage(encouragementMessages));
            
            // Speak the message
            speak(message.toString());
            
            // Update last coaching time
            lastCoachingTime = currentTime;
        }
    }
    
    /**
     * Update coaching for workout mode
     */
    public void updateWorkoutCoaching() {
        long currentTime = System.currentTimeMillis();
        
        // Skip if coaching is not enabled or TTS is not ready
        if (!isCoachingEnabled() || !isTtsReady || activeRun == null || activeWorkout == null) {
            return;
        }
        
        // Get workout segments
        List<CoachingWorkout.WorkoutSegment> segments = activeWorkout.getSegments();
        if (segments.isEmpty()) {
            return;
        }
        
        // Start the workout if not started yet
        if (!isWorkoutStarted) {
            isWorkoutStarted = true;
            announceSegmentStart(segments.get(currentSegmentIndex));
            lastSegmentAnnouncementTime = currentTime;
        }
        
        // Update time remaining in current segment
        currentSegmentTimeRemaining--;
        
        // If time is up for current segment, move to next
        if (currentSegmentTimeRemaining <= 0) {
            // Announce segment completion
            announceSegmentComplete(segments.get(currentSegmentIndex));
            
            // Move to next segment if available
            currentSegmentIndex++;
            if (currentSegmentIndex < segments.size()) {
                // Get the next segment
                CoachingWorkout.WorkoutSegment nextSegment = segments.get(currentSegmentIndex);
                currentSegmentTimeRemaining = nextSegment.getDuration();
                
                // Announce start of next segment
                announceSegmentStart(nextSegment);
                lastSegmentAnnouncementTime = currentTime;
            } else {
                // Workout complete!
                announceWorkoutComplete();
            }
        }
        // If there's time left but it's a multiple of 30 seconds or a minute milestone, give feedback
        else if (currentSegmentTimeRemaining % 60 == 0 || 
                (currentSegmentTimeRemaining <= 10 && currentSegmentTimeRemaining > 0)) {
            
            CoachingWorkout.WorkoutSegment segment = segments.get(currentSegmentIndex);
            
            // Only announce time remaining for longer segments (>30 sec)
            if (segment.getDuration() > 30) {
                if (currentSegmentTimeRemaining % 60 == 0 || currentSegmentTimeRemaining <= 10) {
                    // Announce time remaining
                    announceTimeRemaining(segment, currentSegmentTimeRemaining);
                }
            }
        }
        
        // Check if pace feedback is needed (every 30 seconds)
        if (currentTime - lastPaceFeedbackTime >= 30000) {
            CoachingWorkout.WorkoutSegment segment = segments.get(currentSegmentIndex);
            
            // Only give pace feedback for active segments
            if (segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_REST &&
                segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_RECOVERY) {
                
                // Get current pace
                double currentPace = activeRun.getPace();
                
                // Give pace feedback if pace is available
                if (currentPace > 0) {
                    providePaceFeedback(segment, currentPace);
                    lastPaceFeedbackTime = currentTime;
                }
            }
        }
    }
    
    /**
     * Announce the start of a segment
     * @param segment The workout segment
     */
    private void announceSegmentStart(CoachingWorkout.WorkoutSegment segment) {
        StringBuilder message = new StringBuilder();
        
        // Add segment type
        message.append(CoachingWorkout.WorkoutSegment.getTypeName(segment.getType()))
            .append(" segment. ");
        
        // Add duration
        message.append("Duration: ")
            .append(FormatUtils.formatDurationWords(segment.getDuration()))
            .append(". ");
        
        // Add pace target if applicable
        if (segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_REST && 
            segment.getType() != CoachingWorkout.WorkoutSegment.TYPE_RECOVERY) {
            
            message.append("Target pace: ")
                .append(FormatUtils.formatPaceRange(segment.getTargetPaceMin(), segment.getTargetPaceMax()))
                .append(". ");
        }
        
        // Add instructions if available
        if (segment.getInstructions() != null && !segment.getInstructions().isEmpty()) {
            message.append(segment.getInstructions());
        }
        
        // Add encouragement for active segments
        if (segment.getType() == CoachingWorkout.WorkoutSegment.TYPE_ACTIVE) {
            message.append(" ").append(getRandomMessage(intervalStartMessages));
        }
        
        speak(message.toString());
    }
    
    /**
     * Announce the completion of a segment
     * @param segment The workout segment
     */
    private void announceSegmentComplete(CoachingWorkout.WorkoutSegment segment) {
        StringBuilder message = new StringBuilder();
        
        // Different message based on segment type
        switch (segment.getType()) {
            case CoachingWorkout.WorkoutSegment.TYPE_WARMUP:
                message.append("Warm up complete. ");
                break;
            case CoachingWorkout.WorkoutSegment.TYPE_COOLDOWN:
                message.append("Cool down complete. ");
                break;
            case CoachingWorkout.WorkoutSegment.TYPE_ACTIVE:
                message.append(getRandomMessage(intervalEndMessages)).append(" ");
                break;
            case CoachingWorkout.WorkoutSegment.TYPE_REST:
                message.append("Rest period complete. ");
                break;
            case CoachingWorkout.WorkoutSegment.TYPE_RECOVERY:
                message.append("Recovery period complete. ");
                break;
            default:
                message.append("Segment complete. ");
                break;
        }
        
        speak(message.toString());
    }
    
    /**
     * Announce time remaining in a segment
     * @param segment The workout segment
     * @param timeRemaining Time remaining in seconds
     */
    private void announceTimeRemaining(CoachingWorkout.WorkoutSegment segment, int timeRemaining) {
        StringBuilder message = new StringBuilder();
        
        if (timeRemaining == 60) {
            message.append("One minute remaining. ");
        } else if (timeRemaining < 60) {
            message.append(timeRemaining).append(" seconds remaining. ");
        } else {
            int minutes = timeRemaining / 60;
            message.append(minutes).append(" minutes remaining. ");
        }
        
        speak(message.toString());
    }
    
    /**
     * Announce workout completion
     */
    private void announceWorkoutComplete() {
        speak(getRandomMessage(workoutCompleteMessages));
    }
    
    /**
     * Provide feedback on current pace compared to target
     * @param segment The current workout segment
     * @param currentPace The current pace in min/km
     */
    private void providePaceFeedback(CoachingWorkout.WorkoutSegment segment, double currentPace) {
        // Determine if pace is on target, too fast, or too slow
        int paceFeedback = evaluatePace(segment, currentPace);
        
        // Select and speak appropriate feedback
        switch (paceFeedback) {
            case PACE_FEEDBACK_TOO_FAST:
                speak(getRandomMessage(paceSlowerMessages));
                break;
            case PACE_FEEDBACK_TOO_SLOW:
                speak(getRandomMessage(paceFasterMessages));
                break;
            case PACE_FEEDBACK_PERFECT:
                speak(getRandomMessage(paceGoodMessages));
                break;
        }
    }
    
    /**
     * Evaluate current pace against target
     * @param segment The workout segment
     * @param currentPace Current pace in min/km
     * @return Pace feedback type
     */
    private int evaluatePace(CoachingWorkout.WorkoutSegment segment, double currentPace) {
        // For rest or recovery segments, pace doesn't matter
        if (segment.getType() == CoachingWorkout.WorkoutSegment.TYPE_REST || 
            segment.getType() == CoachingWorkout.WorkoutSegment.TYPE_RECOVERY) {
            return PACE_FEEDBACK_PERFECT;
        }
        
        // Get target pace range
        double targetMin = segment.getTargetPaceMin();
        double targetMax = segment.getTargetPaceMax();
        
        // Allow some tolerance
        double adjustedMin = Math.max(0, targetMin - PACE_TOLERANCE);
        double adjustedMax = targetMax + PACE_TOLERANCE;
        
        // Evaluate pace
        if (currentPace < adjustedMin) {
            return PACE_FEEDBACK_TOO_FAST; // Lower pace means faster in min/km
        } else if (currentPace > adjustedMax) {
            return PACE_FEEDBACK_TOO_SLOW;
        } else {
            return PACE_FEEDBACK_PERFECT;
        }
    }
    
    /**
     * Speak a message using TTS
     * @param message The message to speak
     */
    private void speak(String message) {
        if (isTtsReady && isCoachingEnabled()) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "RunCoach");
        }
    }
    
    /**
     * Get a random message from a list
     * @param messages List of possible messages
     * @return Random message
     */
    private String getRandomMessage(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        
        int index = random.nextInt(messages.size());
        return messages.get(index);
    }
    
    /**
     * Check if voice coaching is enabled
     * @return true if enabled
     */
    private boolean isCoachingEnabled() {
        return preferences.getBoolean(Constants.PREF_COACHING_ENABLED, true);
    }
    
    /**
     * Release resources
     */
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isTtsReady = false;
        }
        
        stopCoaching();
    }
}