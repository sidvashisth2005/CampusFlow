package com.campus.campusflow.model;

import java.util.Date;

public class ApprovalItem {
    private String id;
    private String userId;
    private String userName;
    private String userDesignation;
    private String userImageUrl;
    private String requestType;
    private String description;
    private Date requestDate;
    private String status; // "pending", "approved", "rejected"

    // Empty constructor for Firebase
    public ApprovalItem() {
    }

    public ApprovalItem(String id, String userId, String userName, String userDesignation, 
                      String userImageUrl, String requestType, String description, 
                      Date requestDate, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userDesignation = userDesignation;
        this.userImageUrl = userImageUrl;
        this.requestType = requestType;
        this.description = description;
        this.requestDate = requestDate;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 