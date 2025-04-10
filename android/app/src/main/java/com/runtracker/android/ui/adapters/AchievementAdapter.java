package com.runtracker.android.ui.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.runtracker.android.R;
import com.runtracker.android.data.models.Achievement;
import com.runtracker.android.data.models.RunStatistics;
import com.runtracker.android.utils.Constants;
import com.runtracker.android.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying achievements in a RecyclerView
 */
public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {

    private final Context context;
    private List<Achievement> achievements;
    private final RunStatistics statistics;
    private final SharedPreferences preferences;

    // Filter constants
    public static final int FILTER_ALL = 0;
    public static final int FILTER_UNLOCKED = 1;
    public static final int FILTER_LOCKED = 2;
    private int currentFilter = FILTER_ALL;

    /**
     * Constructor
     * @param context The context
     * @param statistics Run statistics for progress calculation
     */
    public AchievementAdapter(Context context, RunStatistics statistics) {
        this.context = context;
        this.achievements = new ArrayList<>();
        this.statistics = statistics;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set achievements to display
     * @param achievements List of achievements
     */
    public void setAchievements(List<Achievement> achievements) {
        this.achievements = new ArrayList<>(achievements);
        applyFilter(currentFilter);
    }

    /**
     * Apply filter to the achievements list
     * @param filter Filter type
     */
    public void applyFilter(int filter) {
        currentFilter = filter;
        List<Achievement> filteredList = new ArrayList<>();

        // Apply the selected filter
        for (Achievement achievement : achievements) {
            boolean include = false;

            switch (filter) {
                case FILTER_ALL:
                    include = true;
                    break;
                case FILTER_UNLOCKED:
                    include = achievement.isUnlocked();
                    break;
                case FILTER_LOCKED:
                    include = !achievement.isUnlocked();
                    break;
            }

            if (include) {
                filteredList.add(achievement);
            }
        }

        // Update the list
        this.achievements = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.bind(achievement);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    /**
     * ViewHolder for achievement items
     */
    class AchievementViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivAchievementIcon;
        private final TextView tvAchievementTitle;
        private final TextView tvAchievementDescription;
        private final LinearProgressIndicator progressAchievement;
        private final TextView tvAchievementProgress;
        private final TextView tvAchievementDate;

        AchievementViewHolder(@NonNull View itemView) {
            super(itemView);

            ivAchievementIcon = itemView.findViewById(R.id.ivAchievementIcon);
            tvAchievementTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvAchievementDescription = itemView.findViewById(R.id.tvAchievementDescription);
            progressAchievement = itemView.findViewById(R.id.progressAchievement);
            tvAchievementProgress = itemView.findViewById(R.id.tvAchievementProgress);
            tvAchievementDate = itemView.findViewById(R.id.tvAchievementDate);
        }

        /**
         * Bind achievement data to the view
         * @param achievement Achievement to display
         */
        void bind(Achievement achievement) {
            // Set the achievement title and description
            tvAchievementTitle.setText(achievement.getTitle());
            tvAchievementDescription.setText(achievement.getDescription());

            // Set the achievement icon based on type and level
            setAchievementIcon(achievement);

            // Set card background color based on unlock status
            View parent = (View) itemView.getParent();
            if (parent != null) {
                parent.setBackgroundColor(ContextCompat.getColor(context,
                        achievement.isUnlocked() ? R.color.achievement_unlocked_bg : R.color.achievement_locked_bg));
            }

            // Set progress and status
            setProgressAndStatus(achievement);
        }

        /**
         * Set the achievement icon based on type and level
         * @param achievement Achievement to display
         */
        private void setAchievementIcon(Achievement achievement) {
            int iconRes = R.drawable.ic_achievement_generic; // Default/fallback icon

            // Set icon based on type
            switch (achievement.getType()) {
                case Achievement.TYPE_DISTANCE:
                    iconRes = R.drawable.ic_distance;
                    break;
                case Achievement.TYPE_RUNS:
                    iconRes = R.drawable.ic_run;
                    break;
                case Achievement.TYPE_STREAK:
                    iconRes = R.drawable.ic_streak;
                    break;
                case Achievement.TYPE_PACE:
                    iconRes = R.drawable.ic_pace;
                    break;
                case Achievement.TYPE_DURATION:
                    iconRes = R.drawable.ic_time;
                    break;
            }

            // Set the icon tint based on level and unlock status
            int tintColor;
            if (achievement.isUnlocked()) {
                // Set color based on level
                switch (achievement.getLevel()) {
                    case Achievement.LEVEL_BRONZE:
                        tintColor = R.color.achievement_bronze;
                        break;
                    case Achievement.LEVEL_SILVER:
                        tintColor = R.color.achievement_silver;
                        break;
                    case Achievement.LEVEL_GOLD:
                        tintColor = R.color.achievement_gold;
                        break;
                    default:
                        tintColor = R.color.primary;
                        break;
                }
            } else {
                // Not unlocked - use gray
                tintColor = R.color.text_secondary;
            }

            ivAchievementIcon.setImageResource(iconRes);
            ivAchievementIcon.setColorFilter(ContextCompat.getColor(context, tintColor));
        }

        /**
         * Set the progress and status for this achievement
         * @param achievement Achievement to display
         */
        private void setProgressAndStatus(Achievement achievement) {
            // Get current value based on achievement type
            double currentValue = 0;
            
            switch (achievement.getType()) {
                case Achievement.TYPE_DISTANCE:
                    currentValue = statistics.getTotalDistance();
                    break;
                case Achievement.TYPE_RUNS:
                    currentValue = statistics.getTotalRuns();
                    break;
                case Achievement.TYPE_STREAK:
                    currentValue = statistics.getLongestStreak();
                    break;
                case Achievement.TYPE_PACE:
                    currentValue = statistics.getBestPace() > 0 ? statistics.getBestPace() : Double.MAX_VALUE;
                    break;
                case Achievement.TYPE_DURATION:
                    currentValue = statistics.getLongestDuration();
                    break;
            }

            // Set progress
            int progress;
            if (achievement.getType() == Achievement.TYPE_PACE) {
                // For pace, lower is better
                if (currentValue == Double.MAX_VALUE || achievement.getTargetValue() <= 0) {
                    progress = 0;
                } else {
                    // Invert the calculation for pace
                    double targetPace = achievement.getTargetValue();
                    double maxPace = targetPace + 5.0; // Worst pace to show on scale
                    
                    if (currentValue <= targetPace) {
                        progress = 100; // Already achieved
                    } else if (currentValue >= maxPace) {
                        progress = 0; // Far from achievement
                    } else {
                        // Scale between maxPace and targetPace
                        progress = (int) ((maxPace - currentValue) / (maxPace - targetPace) * 100);
                    }
                }
            } else {
                // For other achievements, higher is better
                progress = achievement.calculateProgress(currentValue);
            }
            
            progressAchievement.setProgress(progress);
            
            // Set progress color based on achievement level
            int progressColor;
            switch (achievement.getLevel()) {
                case Achievement.LEVEL_BRONZE:
                    progressColor = R.color.achievement_bronze;
                    break;
                case Achievement.LEVEL_SILVER:
                    progressColor = R.color.achievement_silver;
                    break;
                case Achievement.LEVEL_GOLD:
                    progressColor = R.color.achievement_gold;
                    break;
                default:
                    progressColor = R.color.primary;
                    break;
            }
            progressAchievement.setIndicatorColor(ContextCompat.getColor(context, progressColor));
            
            // Set progress text
            String progressText = formatProgress(currentValue, achievement);
            tvAchievementProgress.setText(progressText);
            
            // Set unlock date or hide if not unlocked
            if (achievement.isUnlocked()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String dateStr = sdf.format(new Date(achievement.getUnlockedDate()));
                tvAchievementDate.setText(context.getString(R.string.unlocked_on, dateStr));
                tvAchievementDate.setVisibility(View.VISIBLE);
            } else {
                tvAchievementDate.setVisibility(View.GONE);
            }
        }

        /**
         * Format the progress text based on achievement type
         * @param currentValue Current value
         * @param achievement Achievement
         * @return Formatted progress text
         */
        private String formatProgress(double currentValue, Achievement achievement) {
            String currentValueStr;
            String targetValueStr;
            
            // Get unit from preferences
            String distanceUnit = preferences.getString(Constants.PREF_DISTANCE_UNIT, Constants.UNIT_KM);
            boolean useMetric = distanceUnit.equals(Constants.UNIT_KM);
            
            // Format based on achievement type
            switch (achievement.getType()) {
                case Achievement.TYPE_DISTANCE:
                    // Convert if needed
                    double displayCurrent = useMetric ? currentValue : currentValue * 0.621371;
                    double displayTarget = useMetric ? achievement.getTargetValue() : achievement.getTargetValue() * 0.621371;
                    
                    currentValueStr = FormatUtils.formatDistanceSimple(displayCurrent);
                    targetValueStr = FormatUtils.formatDistanceSimple(displayTarget);
                    break;
                    
                case Achievement.TYPE_RUNS:
                    currentValueStr = String.valueOf((int) currentValue);
                    targetValueStr = String.valueOf((int) achievement.getTargetValue());
                    break;
                    
                case Achievement.TYPE_STREAK:
                    currentValueStr = context.getString(R.string.days_value, (int) currentValue);
                    targetValueStr = context.getString(R.string.days_value, (int) achievement.getTargetValue());
                    break;
                    
                case Achievement.TYPE_PACE:
                    if (currentValue == Double.MAX_VALUE) {
                        return context.getString(R.string.not_available);
                    }
                    currentValueStr = FormatUtils.formatPaceSimple(currentValue);
                    targetValueStr = FormatUtils.formatPaceSimple(achievement.getTargetValue());
                    break;
                    
                case Achievement.TYPE_DURATION:
                    currentValueStr = FormatUtils.formatDurationSimple((long) currentValue);
                    targetValueStr = FormatUtils.formatDurationSimple((long) achievement.getTargetValue());
                    break;
                    
                default:
                    currentValueStr = String.valueOf((int) currentValue);
                    targetValueStr = String.valueOf((int) achievement.getTargetValue());
                    break;
            }
            
            return context.getString(R.string.progress_format, currentValueStr, targetValueStr);
        }
    }
}