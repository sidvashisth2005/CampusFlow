package com.sid.campusflow;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sid.campusflow.models.EventGlimpse;
import com.sid.campusflow.utils.CloudinaryConfig;
import com.sid.campusflow.utils.MediaUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Activity for uploading event glimpses (images and videos)
 */
public class GlimpseUploadActivity extends AppCompatActivity {

    private TextView tvEventTitle;
    private TextView tvInstructions;
    private Button btnSelectMedia;
    private Button btnUploadGlimpse;
    private ImageView ivPreview;
    private VideoView videoPreview;
    private ProgressBar progressBar;
    private TextView tvProgress;

    private String eventId;
    private String eventTitle;
    private Uri selectedMediaUri;
    private int mediaType = -1;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glimpse_upload);

        // Get event details from intent
        eventId = getIntent().getStringExtra("event_id");
        eventTitle = getIntent().getStringExtra("event_title");

        if (eventId == null || eventTitle == null) {
            Toast.makeText(this, "Invalid event information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvInstructions = findViewById(R.id.tv_instructions);
        btnSelectMedia = findViewById(R.id.btn_select_media);
        btnUploadGlimpse = findViewById(R.id.btn_upload_glimpse);
        ivPreview = findViewById(R.id.iv_preview);
        videoPreview = findViewById(R.id.video_preview);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);

        // Set event title
        tvEventTitle.setText(eventTitle);

        // Set initial UI state
        ivPreview.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);
        btnUploadGlimpse.setEnabled(false);
        progressBar.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);

        // Media selection launcher
        ActivityResultLauncher<Intent> mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri mediaUri = result.getData().getData();
                        if (mediaUri != null) {
                            handleSelectedMedia(mediaUri);
                        }
                    }
                }
        );

        // Set select media button click listener
        btnSelectMedia.setOnClickListener(v -> {
            showMediaTypeChooser(mediaPickerLauncher);
        });

        // Set upload button click listener
        btnUploadGlimpse.setOnClickListener(v -> {
            if (selectedMediaUri != null && mediaType != -1) {
                uploadGlimpse();
            } else {
                Toast.makeText(this, "Please select an image or video first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMediaTypeChooser(ActivityResultLauncher<Intent> launcher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_media_type_chooser, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        Button btnSelectVideo = dialogView.findViewById(R.id.btn_select_video);

        // Check media permissions first
        if (!MediaUtils.checkMediaPermissions(this)) {
            dialog.dismiss();
            return;
        }

        btnSelectImage.setOnClickListener(v -> {
            MediaUtils.launchMediaPicker(launcher, MediaUtils.MEDIA_TYPE_IMAGE);
            dialog.dismiss();
        });

        btnSelectVideo.setOnClickListener(v -> {
            MediaUtils.launchMediaPicker(launcher, MediaUtils.MEDIA_TYPE_VIDEO);
            dialog.dismiss();
        });
    }

    private void handleSelectedMedia(Uri mediaUri) {
        selectedMediaUri = mediaUri;
        mediaType = MediaUtils.getMediaTypeFromUri(this, mediaUri);

        // Reset preview views
        ivPreview.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);

        // Show appropriate preview based on media type
        if (mediaType == MediaUtils.MEDIA_TYPE_IMAGE) {
            ivPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(mediaUri).into(ivPreview);
        } else if (mediaType == MediaUtils.MEDIA_TYPE_VIDEO) {
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoURI(mediaUri);
            videoPreview.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                videoPreview.start();
            });
        } else {
            Toast.makeText(this, "Unsupported media type", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUploadGlimpse.setEnabled(true);
    }

    private void uploadGlimpse() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to upload glimpses", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress UI
        progressBar.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);
        btnUploadGlimpse.setEnabled(false);
        btnSelectMedia.setEnabled(false);

        // Determine folder name based on media type
        String folderName = "event_glimpses/" + eventId;

        // Use MediaUtils to upload to Cloudinary
        MediaUtils.uploadMedia(this, selectedMediaUri, folderName, eventId, new CloudinaryConfig.CloudinaryUploadCallback() {
            @Override
            public void onSuccess(String mediaUrl) {
                // Create event glimpse in Firestore
                saveGlimpseToFirestore(mediaUrl, currentUser.getUid());
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    btnUploadGlimpse.setEnabled(true);
                    btnSelectMedia.setEnabled(true);
                    Toast.makeText(GlimpseUploadActivity.this, 
                            getString(R.string.glimpse_upload_failed) + ": " + error, 
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    String progressMessage = getString(R.string.image_upload_progress, progress);
                    tvProgress.setText(progressMessage);
                });
            }
        });
    }

    private void saveGlimpseToFirestore(String mediaUrl, String userId) {
        // Create glimpse data object
        Map<String, Object> glimpseData = new HashMap<>();
        glimpseData.put("eventId", eventId);
        glimpseData.put("mediaUrl", mediaUrl);
        glimpseData.put("mediaType", mediaType);
        glimpseData.put("uploadedAt", System.currentTimeMillis());
        glimpseData.put("uploadedBy", userId);

        // For videos, add a thumbnail URL
        if (mediaType == MediaUtils.MEDIA_TYPE_VIDEO) {
            // Cloudinary generates thumbnails automatically by modifying the URL
            String thumbnailUrl = mediaUrl.replace("/upload/", "/upload/c_thumb,w_200,g_face/");
            glimpseData.put("thumbnailUrl", thumbnailUrl);
        }

        // Save to Firestore
        db.collection("eventGlimpses")
                .add(glimpseData)
                .addOnSuccessListener(documentReference -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvProgress.setVisibility(View.GONE);
                        Toast.makeText(GlimpseUploadActivity.this, 
                                R.string.glimpse_uploaded, 
                                Toast.LENGTH_SHORT).show();
                        
                        // Close activity and return to previous screen
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvProgress.setVisibility(View.GONE);
                        btnUploadGlimpse.setEnabled(true);
                        btnSelectMedia.setEnabled(true);
                        Toast.makeText(GlimpseUploadActivity.this, 
                                R.string.glimpse_upload_failed + ": " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
                });
    }
} 