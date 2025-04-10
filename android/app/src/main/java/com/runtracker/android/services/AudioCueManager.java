package com.runtracker.android.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.runtracker.android.R;
import com.runtracker.android.utils.Constants;
import com.runtracker.android.utils.FormatUtils;

import java.util.Locale;

/**
 * Manages audio cues and text-to-speech for run feedback
 */
public class AudioCueManager {
    private static final String TAG = "AudioCueManager";

    private Context context;
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;
    private MediaPlayer milestoneSound;
    private SharedPreferences sharedPreferences;

    // Tracking variables
    private double lastMilestoneCrossed = 0;
    private double lastAudioCueDistance = 0;
    private long lastAudioCueTime = 0;

    /**
     * Constructor
     * @param context The application context
     */
    public AudioCueManager(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        
        // Initialize TTS
        initTextToSpeech();
        
        // Initialize milestone sound
        milestoneSound = MediaPlayer.create(context, R.raw.milestone_sound);
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
                }
            } else {
                Log.e(TAG, "TTS initialization failed");
            }
        });
    }

    /**
     * Speak a message using TTS
     * @param message Message to speak
     */
    public void speak(String message) {
        if (isTtsReady && isAudioCuesEnabled()) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, "RunTracker");
        }
    }

    /**
     * Check for milestone crossing and provide audio feedback
     * @param distance Current distance in kilometers
     * @param duration Current duration in seconds
     */
    public void checkMilestones(double distance, long duration) {
        // Only check if milestone alerts are enabled
        if (!isMilestoneAlertsEnabled()) {
            return;
        }
        
        // Determine milestone unit (km or mile)
        String unitType = sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
        double milestone = unitType.equals(Constants.UNIT_KM) ? 
            Constants.MILESTONE_KM : Constants.MILESTONE_MILE;
        
        // Convert to display unit if needed
        double displayDistance = distance;
        if (unitType.equals(Constants.UNIT_MILES)) {
            displayDistance = distance * 0.621371; // Convert km to miles
        }
        
        // Check if a new milestone has been crossed
        double currentMilestone = Math.floor(displayDistance / milestone);
        if (currentMilestone > lastMilestoneCrossed && currentMilestone >= 1) {
            // Play milestone sound
            playMilestoneSound();
            
            // Announce milestone
            String unit = unitType.equals(Constants.UNIT_KM) ? "kilometers" : "miles";
            String message = String.format(Locale.getDefault(), 
                "You've reached %.0f %s", currentMilestone * milestone, unit);
            speak(message);
            
            // Update last milestone
            lastMilestoneCrossed = currentMilestone;
        }
    }

    /**
     * Check if it's time for a periodic audio cue
     * @param distance Current distance in kilometers
     * @param duration Current duration in seconds
     * @param pace Current pace in min/km
     */
    public void checkPeriodicCue(double distance, long duration, double pace) {
        // Check if enough time has passed since last cue
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAudioCueTime < Constants.AUDIO_CUE_INTERVAL) {
            return;
        }
        
        // Skip if audio cues are disabled
        if (!isAudioCuesEnabled()) {
            return;
        }
        
        // Get units
        String distanceUnit = sharedPreferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
        
        // Convert to display units if needed
        double displayDistance = distance;
        if (distanceUnit.equals(Constants.UNIT_MILES)) {
            displayDistance = distance * 0.621371; // Convert km to miles
        }
        
        // Format display values
        String formattedDistance = FormatUtils.formatDistance(displayDistance);
        String formattedDuration = FormatUtils.formatDuration(duration);
        String formattedPace = FormatUtils.formatPace(pace);
        
        // Build message
        StringBuilder message = new StringBuilder();
        message.append("Current status: ");
        message.append(formattedDistance);
        message.append(", time ");
        message.append(formattedDuration);
        message.append(", pace ");
        message.append(formattedPace);
        
        // Speak message
        speak(message.toString());
        
        // Update tracking variables
        lastAudioCueTime = currentTime;
        lastAudioCueDistance = distance;
    }

    /**
     * Play milestone achievement sound
     */
    private void playMilestoneSound() {
        if (milestoneSound != null) {
            milestoneSound.seekTo(0);
            milestoneSound.start();
        }
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
        
        if (milestoneSound != null) {
            milestoneSound.release();
            milestoneSound = null;
        }
    }

    /**
     * Reset tracking variables for a new run
     */
    public void reset() {
        lastMilestoneCrossed = 0;
        lastAudioCueDistance = 0;
        lastAudioCueTime = 0;
    }

    /**
     * Check if audio cues are enabled
     * @return true if enabled
     */
    private boolean isAudioCuesEnabled() {
        return sharedPreferences.getBoolean(Constants.PREF_AUDIO_CUES, true);
    }

    /**
     * Check if milestone alerts are enabled
     * @return true if enabled
     */
    private boolean isMilestoneAlertsEnabled() {
        return sharedPreferences.getBoolean(Constants.PREF_MILESTONE_ALERTS, true);
    }
}