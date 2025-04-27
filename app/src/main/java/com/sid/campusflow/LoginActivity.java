package com.sid.campusflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterPrompt, tvForgotPassword;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegisterPrompt = findViewById(R.id.tv_register_prompt);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);

        // Set click listeners
        btnLogin.setOnClickListener(this::onLoginClick);
        tvRegisterPrompt.setOnClickListener(this::onRegisterPromptClick);
        tvForgotPassword.setOnClickListener(this::onForgotPasswordClick);
    }

    private void onLoginClick(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Please wait...");

        // Attempt login
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Login successful
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Login failed
                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    
                    // Reset button state
                    btnLogin.setEnabled(true);
                    btnLogin.setText(R.string.login_button);
                }
            });
    }

    private void onRegisterPromptClick(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    private void onForgotPasswordClick(View view) {
        // Implement forgot password logic
        String email = etEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to send reset email: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }
} 
