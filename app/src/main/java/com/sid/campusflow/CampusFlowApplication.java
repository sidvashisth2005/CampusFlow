package com.sid.campusflow;

import android.app.Application;
import com.sid.campusflow.utils.CloudinaryConfig;

public class CampusFlowApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary
        CloudinaryConfig.init(this);
    }
} 