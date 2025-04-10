package com.runtracker.android.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying runs in a RecyclerView
 */
public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {

    private final Context context;
    private final RunClickListener listener;
    private List<Run> runs;
    
    /**
     * Interface for handling run item clicks
     */
    public interface RunClickListener {
        void onRunClick(String runId);
    }
    
    public RunAdapter(Context context, RunClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.runs = new ArrayList<>();
    }
    
    /**
     * Set runs to display
     * @param runs List of runs
     */
    public void setRuns(List<Run> runs) {
        this.runs = runs;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_run, parent, false);
        return new RunViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RunViewHolder holder, int position) {
        Run run = runs.get(position);
        holder.bind(run);
    }
    
    @Override
    public int getItemCount() {
        return runs.size();
    }
    
    /**
     * ViewHolder for run items
     */
    class RunViewHolder extends RecyclerView.ViewHolder {
        
        private final TextView tvRunDate;
        private final TextView tvDistance;
        private final TextView tvDuration;
        private final TextView tvPace;
        private final TextView tvCalories;
        
        RunViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvRunDate = itemView.findViewById(R.id.tvRunDate);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPace = itemView.findViewById(R.id.tvPace);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRunClick(runs.get(position).getId());
                }
            });
        }
        
        /**
         * Bind run data to the view
         * @param run Run to display
         */
        void bind(Run run) {
            // Format date and time
            String formattedDate = FormatUtils.formatDate(run.getStartTime());
            String formattedTime = FormatUtils.formatTime(run.getStartTime());
            tvRunDate.setText(context.getString(R.string.run_date_time, formattedDate, formattedTime));
            
            // Format metrics
            tvDistance.setText(FormatUtils.formatDistance(run.getTotalDistance()));
            tvDuration.setText(FormatUtils.formatDuration(run.getActiveDuration()));
            tvPace.setText(FormatUtils.formatPace(run.getPace()));
            tvCalories.setText(FormatUtils.formatCalories(run.getCaloriesBurned()));
        }
    }
}