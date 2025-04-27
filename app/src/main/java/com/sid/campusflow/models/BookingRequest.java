package com.sid.campusflow.models;

import java.util.Date;

public class BookingRequest {
    private String id;
    private String roomId;
    private String userId;
    private String userName;
    private String userDesignation;
    private Date startTime;
    private Date endTime;
    private String purpose;
    private String status; // PENDING, APPROVED, REJECTED
    private String adminId;
    private String adminName;
    private Date processedAt;
    private String rejectionReason;

    public BookingRequest() {
        // Default constructor required for Firestore
    }

    public BookingRequest(String id, String roomId, String userId, String userName, 
                         String userDesignation, Date startTime, Date endTime, String purpose) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.userName = userName;
        this.userDesignation = userDesignation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.status = "PENDING";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDesignation() {
        return userDesignation;
    }

    public void setUserDesignation(String userDesignation) {
        this.userDesignation = userDesignation;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
} 