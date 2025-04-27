package com.sid.campusflow.utils;

import android.content.Context;
import android.net.Uri;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    private static boolean isInitialized = false;

    public interface CloudinaryUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
        void onProgress(int progress);
    }

    public static void init(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            // Replace with your Cloudinary cloud name
            config.put("cloud_name", "your_cloud_name");
            // Don't include API key and secret in app - use unsigned uploads with upload preset
            config.put("api_key", "");
            config.put("api_secret", "");
            config.put("secure", "true");
            
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }

    public static void uploadImage(Context context, Uri imageUri, final String folderName, final CloudinaryUploadCallback callback) {
        init(context);

        // Create upload request
        String requestId = MediaManager.get().upload(imageUri)
                .option("folder", folderName)
                .unsigned("ml_default") // Replace with your unsigned upload preset name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        callback.onSuccess(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onFailure(error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Upload video to Cloudinary
     * @param context Application context
     * @param videoUri URI of the video to upload
     * @param folderName Folder to store video in Cloudinary
     * @param callback Callback for upload events
     */
    public static void uploadVideo(Context context, Uri videoUri, final String folderName, final CloudinaryUploadCallback callback) {
        init(context);

        // Create upload request for video
        String requestId = MediaManager.get().upload(videoUri)
                .option("folder", folderName)
                .option("resource_type", "video") // Specify resource type as video
                .unsigned("ml_default") // Replace with your unsigned upload preset name
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Upload started
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        callback.onSuccess(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onFailure(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        callback.onFailure(error.getDescription());
                    }
                })
                .dispatch();
    }
} 