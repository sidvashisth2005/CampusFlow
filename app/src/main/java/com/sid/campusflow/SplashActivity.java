package com.sid.campusflow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserLoginStatus, SPLASH_DURATION);
    }

    private void checkUserLoginStatus() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        
        if (currentUser != null) {
            // User already logged in, go to main activity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // User not logged in, go to login activity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        
        // Close splash activity
        finish();
    }
} 
