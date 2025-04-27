package com.sid.campusflow.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for media-related operations
 */
public class MediaUtils {

    public static final int REQUEST_PERMISSION_CODE = 101;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /**
     * Check and request required permissions for media access
     * @param activity Current activity
     * @return true if permissions already granted, false if requested
     */
    public static boolean checkMediaPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO}, 
                    REQUEST_PERMISSION_CODE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    REQUEST_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    /**
     * Launch media picker for image or video selection
     * @param launcher ActivityResultLauncher to handle the result
     * @param mediaType Type of media to select (MEDIA_TYPE_IMAGE or MEDIA_TYPE_VIDEO)
     */
    public static void launchMediaPicker(ActivityResultLauncher<Intent> launcher, int mediaType) {
        Intent intent;
        
        if (mediaType == MEDIA_TYPE_IMAGE) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        } else {
            throw new IllegalArgumentException("Invalid media type");
        }
        
        launcher.launch(intent);
    }

    /**
     * Get file type from URI
     * @param context Application context
     * @param uri Media URI
     * @return MEDIA_TYPE_IMAGE, MEDIA_TYPE_VIDEO, or -1 if unknown
     */
    public static int getMediaTypeFromUri(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) return -1;
        
        if (mimeType.startsWith("image/")) {
            return MEDIA_TYPE_IMAGE;
        } else if (mimeType.startsWith("video/")) {
            return MEDIA_TYPE_VIDEO;
        } else {
            return -1;
        }
    }
    
    /**
     * Upload media to Cloudinary based on its type
     * @param context Application context
     * @param mediaUri URI of the media to upload
     * @param folderName Folder name in Cloudinary
     * @param objectId Identifier for the media (optional)
     * @param callback Upload callback
     */
    public static void uploadMedia(Context context, Uri mediaUri, String folderName, 
                                  String objectId, CloudinaryConfig.CloudinaryUploadCallback callback) {
        if (mediaUri == null) {
            callback.onFailure("Media URI is null");
            return;
        }
        
        int mediaType = getMediaTypeFromUri(context, mediaUri);
        
        switch (mediaType) {
            case MEDIA_TYPE_IMAGE:
                CloudinaryConfig.uploadImage(context, mediaUri, folderName, callback);
                break;
                
            case MEDIA_TYPE_VIDEO:
                CloudinaryConfig.uploadVideo(context, mediaUri, folderName, callback);
                break;
                
            default:
                callback.onFailure("Unsupported media type");
                break;
        }
    }
} 