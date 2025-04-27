package com.sid.campusflow;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.sid.campusflow.utils.MediaUtils;

/**
 * Activity for viewing event media glimpses in full-screen mode
 */
public class MediaViewerActivity extends AppCompatActivity {

    private ImageView ivFullImage;
    private VideoView videoView;
    private ProgressBar progressBar;
    private TextView tvError;
    private ImageView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        // Initialize views
        ivFullImage = findViewById(R.id.iv_full_image);
        videoView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.progress_bar);
        tvError = findViewById(R.id.tv_error);
        btnClose = findViewById(R.id.btn_close);

        // Get media details from intent
        String mediaUrl = getIntent().getStringExtra("media_url");
        int mediaType = getIntent().getIntExtra("media_type", -1);

        if (mediaUrl == null || mediaUrl.isEmpty() || mediaType == -1) {
            showError("Invalid media information");
            return;
        }

        // Close button click listener
        btnClose.setOnClickListener(v -> finish());

        // Display the appropriate media type
        if (mediaType == MediaUtils.MEDIA_TYPE_IMAGE) {
            displayImage(mediaUrl);
        } else if (mediaType == MediaUtils.MEDIA_TYPE_VIDEO) {
            displayVideo(mediaUrl);
        } else {
            showError("Unsupported media type");
        }
    }

    private void displayImage(String imageUrl) {
        // Show image view, hide video view
        ivFullImage.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Load image with Glide
        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .error(R.drawable.placeholder_image)
                .into(ivFullImage);

        progressBar.setVisibility(View.GONE);
    }

    private void displayVideo(String videoUrl) {
        // Show video view, hide image view
        ivFullImage.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Set up video view with media controls
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            
            // Set video URI and prepare for playback
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(Uri.parse(videoUrl));
            
            // Set event listeners
            videoView.setOnPreparedListener(mp -> {
                progressBar.setVisibility(View.GONE);
                mp.start();
            });
            
            videoView.setOnErrorListener((mp, what, extra) -> {
                progressBar.setVisibility(View.GONE);
                showError("Error playing video");
                return true;
            });
            
            videoView.requestFocus();
            
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            showError("Error loading video: " + e.getMessage());
        }
    }

    private void showError(String message) {
        ivFullImage.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video playback if needed
        if (videoView.isShown()) {
            videoView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video playback to conserve resources
        if (videoView.isShown()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up video resources
        if (videoView.isShown()) {
            videoView.stopPlayback();
        }
    }
} 