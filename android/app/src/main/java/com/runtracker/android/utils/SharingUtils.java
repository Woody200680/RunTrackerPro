package com.runtracker.android.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.runtracker.android.R;
import com.runtracker.android.data.models.Run;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility class for sharing run data and images
 */
public class SharingUtils {
    private static final String TAG = "SharingUtils";

    /**
     * Share run as text with distance, time, and pace
     * @param context The context
     * @param run The run to share
     */
    public static void shareRunText(Context context, Run run) {
        if (run == null) return;

        // Create share text
        String shareText = createShareText(context, run);

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_run_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Start share chooser
        context.startActivity(Intent.createChooser(shareIntent, 
                context.getString(R.string.share_run_via)));
    }

    /**
     * Share run details with an image of the run summary
     * @param context The context
     * @param run The run to share
     * @param summaryView The view containing the run summary to be captured
     */
    public static void shareRunImage(Context context, Run run, View summaryView) {
        if (run == null || summaryView == null) return;

        try {
            // Create bitmap from view
            Bitmap bitmap = captureView(summaryView);
            
            // Save bitmap to temp file
            File cachePath = new File(context.getCacheDir(), "images");
            cachePath.mkdirs();
            File imageFile = new File(cachePath, "run_summary_" + run.getId() + ".jpg");
            
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            
            // Get URI for the file
            Uri imageUri = FileProvider.getUriForFile(context, 
                    context.getPackageName() + ".fileprovider", imageFile);
            
            // Create share text
            String shareText = createShareText(context, run);
            
            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_run_subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Start share chooser
            context.startActivity(Intent.createChooser(shareIntent, 
                    context.getString(R.string.share_run_via)));
            
        } catch (IOException e) {
            Log.e(TAG, "Error sharing run image", e);
            Toast.makeText(context, R.string.error_sharing_run, Toast.LENGTH_SHORT).show();
            
            // Fallback to text sharing if image fails
            shareRunText(context, run);
        }
    }

    /**
     * Create formatted text for sharing
     * @param context The context
     * @param run The run
     * @return Formatted share text
     */
    private static String createShareText(Context context, Run run) {
        StringBuilder shareText = new StringBuilder();
        
        // Add run date
        shareText.append(context.getString(R.string.share_run_date, 
                FormatUtils.formatDate(run.getStartTime())));
        shareText.append("\n\n");
        
        // Add run stats
        shareText.append(context.getString(R.string.share_distance, 
                FormatUtils.formatDistance(run.getTotalDistance())));
        shareText.append("\n");
        
        shareText.append(context.getString(R.string.share_duration, 
                FormatUtils.formatDuration(run.getActiveDuration())));
        shareText.append("\n");
        
        shareText.append(context.getString(R.string.share_pace, 
                FormatUtils.formatPace(run.getPace())));
        shareText.append("\n");
        
        shareText.append(context.getString(R.string.share_calories, 
                FormatUtils.formatCalories(run.getCaloriesBurned())));
        
        // Add app signature
        shareText.append("\n\n");
        shareText.append(context.getString(R.string.share_app_signature));
        
        return shareText.toString();
    }

    /**
     * Capture view as bitmap
     * @param view The view to capture
     * @return Bitmap of the view
     */
    private static Bitmap captureView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}