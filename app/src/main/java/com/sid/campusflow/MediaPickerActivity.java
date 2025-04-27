package com.sid.campusflow;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
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
import com.sid.campusflow.utils.CloudinaryConfig;
import com.sid.campusflow.utils.MediaUtils;

public class MediaPickerActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private VideoView videoPreview;
    private Button btnSelectMedia;
    private Button btnUploadMedia;
    private ProgressBar progressBar;
    private TextView tvProgress;
    
    private Uri selectedMediaUri;
    private int selectedMediaType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_picker);

        // Initialize views
        imagePreview = findViewById(R.id.image_preview);
        videoPreview = findViewById(R.id.video_preview);
        btnSelectMedia = findViewById(R.id.btn_select_media);
        btnUploadMedia = findViewById(R.id.btn_upload_media);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);

        // Set up initial UI state
        imagePreview.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);
        btnUploadMedia.setEnabled(false);
        progressBar.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);

        // Image picker result launcher
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
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

        // Select media button click
        btnSelectMedia.setOnClickListener(v -> {
            showMediaTypeChooser(imagePickerLauncher);
        });

        // Upload media button click
        btnUploadMedia.setOnClickListener(v -> {
            if (selectedMediaUri != null && selectedMediaType != -1) {
                uploadMedia();
            } else {
                Toast.makeText(this, "Please select media first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMediaTypeChooser(ActivityResultLauncher<Intent> launcher) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_media_type_chooser, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        Button btnSelectVideo = dialogView.findViewById(R.id.btn_select_video);
        
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
        int mediaType = MediaUtils.getMediaTypeFromUri(this, mediaUri);
        selectedMediaType = mediaType;
        
        // Reset views
        imagePreview.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);
        
        // Show appropriate preview based on media type
        if (mediaType == MediaUtils.MEDIA_TYPE_IMAGE) {
            imagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(mediaUri).into(imagePreview);
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
        
        btnUploadMedia.setEnabled(true);
    }

    private void uploadMedia() {
        progressBar.setVisibility(View.VISIBLE);
        tvProgress.setVisibility(View.VISIBLE);
        btnUploadMedia.setEnabled(false);
        
        // Determine folder name based on media type
        String folderName = selectedMediaType == MediaUtils.MEDIA_TYPE_IMAGE ? "images" : "videos";
        
        // Upload using MediaUtils
        MediaUtils.uploadMedia(this, selectedMediaUri, folderName, "sample_upload", new CloudinaryConfig.CloudinaryUploadCallback() {
            @Override
            public void onSuccess(String mediaUrl) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    btnUploadMedia.setEnabled(true);
                    
                    // Show success message with the URL
                    String successMessage = selectedMediaType == MediaUtils.MEDIA_TYPE_IMAGE ? 
                            getString(R.string.image_upload_success) : 
                            getString(R.string.video_upload_success);
                    
                    Toast.makeText(MediaPickerActivity.this, 
                            successMessage + ": " + mediaUrl, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvProgress.setVisibility(View.GONE);
                    btnUploadMedia.setEnabled(true);
                    
                    String failureMessage = selectedMediaType == MediaUtils.MEDIA_TYPE_IMAGE ? 
                            getString(R.string.image_upload_failed) : 
                            getString(R.string.video_upload_failed);
                    
                    Toast.makeText(MediaPickerActivity.this, 
                            failureMessage + ": " + error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    String progressMessage = selectedMediaType == MediaUtils.MEDIA_TYPE_IMAGE ? 
                            getString(R.string.image_upload_progress, progress) : 
                            getString(R.string.video_upload_progress, progress);
                    tvProgress.setText(progressMessage);
                });
            }
        });
    }
} 