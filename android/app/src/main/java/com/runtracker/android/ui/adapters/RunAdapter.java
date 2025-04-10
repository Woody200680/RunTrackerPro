package com.runtracker.android.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;
import com.runtracker.android.utils.FormatUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunViewHolder> {

    private List<Run> runs = new ArrayList<>();
    private final OnRunClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public interface OnRunClickListener {
        void onRunClick(Run run);
    }

    public RunAdapter(OnRunClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RunViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_run, parent, false);
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

    public void setRuns(List<Run> runs) {
        this.runs = runs;
        notifyDataSetChanged();
    }

    class RunViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivRunImage;
        private final TextView tvDate;
        private final TextView tvDistance;
        private final TextView tvDuration;
        private final TextView tvPace;
        private final TextView tvCalories;

        public RunViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRunImage = itemView.findViewById(R.id.ivRunImage);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPace = itemView.findViewById(R.id.tvPace);
            tvCalories = itemView.findViewById(R.id.tvCalories);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRunClick(runs.get(position));
                }
            });
        }

        public void bind(Run run) {
            // Set date
            if (run.getStartTime() != null) {
                tvDate.setText(dateFormat.format(run.getStartTime()));
            }

            // Set distance
            float distanceKm = run.getDistanceInMeters() / 1000f;
            tvDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

            // Set duration
            int durationSeconds = run.getDurationInSeconds();
            int hours = durationSeconds / 3600;
            int minutes = (durationSeconds % 3600) / 60;
            int seconds = durationSeconds % 60;

            if (hours > 0) {
                tvDuration.setText(String.format(Locale.getDefault(), 
                        "%d:%02d:%02d", hours, minutes, seconds));
            } else {
                tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            // Set pace
            float paceMinPerKm = run.getPace();
            int paceMinutes = (int) paceMinPerKm;
            int paceSeconds = (int) ((paceMinPerKm - paceMinutes) * 60);
            tvPace.setText(String.format(Locale.getDefault(), "%d:%02d /km", paceMinutes, paceSeconds));

            // Set calories
            tvCalories.setText(String.format(Locale.getDefault(), "%d kcal", run.getCaloriesBurned()));

            // TODO: Load map snapshot image for the run route when available
            // For now, we'll use the default run icon
        }
    }
}