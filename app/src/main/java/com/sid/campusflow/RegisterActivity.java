package com.sid.campusflow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etCollegeEmail, etPassword, etConfirmPassword;
    private AutoCompleteTextView dropdownDesignation;
    private Button btnRegister;
    private TextView tvLoginPrompt;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        etFullName = findViewById(R.id.et_full_name);
        etCollegeEmail = findViewById(R.id.et_college_email);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        dropdownDesignation = findViewById(R.id.dropdown_designation);
        btnRegister = findViewById(R.id.btn_register);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);

        // Setup designation dropdown
        setupDesignationDropdown();

        // Set click listeners
        btnRegister.setOnClickListener(this::onRegisterClick);
        tvLoginPrompt.setOnClickListener(this::onLoginPromptClick);
    }

    private void setupDesignationDropdown() {
        String[] designations = new String[]{"Student", "Faculty", "Secretary", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, designations);
        dropdownDesignation.setAdapter(adapter);
    }

    private void onRegisterClick(View view) {
        // Get input values
        String fullName = etFullName.getText().toString().trim();
        String email = etCollegeEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String designation = dropdownDesignation.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etCollegeEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (TextUtils.isEmpty(designation)) {
            dropdownDesignation.setError("Designation is required");
            return;
        }

        // Show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        // Create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Registration successful
                    saveUserDataToFirestore(firebaseAuth.getCurrentUser().getUid(), fullName, email, designation);
                } else {
                    // Registration failed
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    
                    // Reset button state
                    btnRegister.setEnabled(true);
                    btnRegister.setText(R.string.register_button);
                }
            });
    }

    private void saveUserDataToFirestore(String userId, String fullName, String email, String designation) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("designation", designation);
        userData.put("createdAt", System.currentTimeMillis());
        
        // Save to Firestore users collection
        firestore.collection("users").document(userId)
            .set(userData)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User data saved successfully
                    Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    
                    // Go to main activity
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    // Failed to save user data
                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    
                    // Reset button state
                    btnRegister.setEnabled(true);
                    btnRegister.setText(R.string.register_button);
                }
            });
    }

    private void onLoginPromptClick(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }
} 
