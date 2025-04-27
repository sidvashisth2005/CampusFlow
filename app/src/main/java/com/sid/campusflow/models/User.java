package com.sid.campusflow.models;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String designation;
    private String profileImageUrl;
    private String department;
    private boolean isEmailVerified;
    private long createdAt;
    private long lastLogin;

    // Default constructor required for Firebase
    public User() {
    }

    public User(String userId, String fullName, String email, String designation, String profileImageUrl,
                String department, boolean isEmailVerified, long createdAt, long lastLogin) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.designation = designation;
        this.profileImageUrl = profileImageUrl;
        this.department = department;
        this.isEmailVerified = isEmailVerified;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
} 
