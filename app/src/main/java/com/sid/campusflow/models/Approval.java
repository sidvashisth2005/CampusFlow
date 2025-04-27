package com.sid.campusflow.models;

import java.util.Date;
import java.util.UUID;

public class Approval {
    private String id;
    private String userId;
    private String userName;
    private String userPhotoUrl;
    private String requestType;
    private String requestDetails;
    private String status; // PENDING, APPROVED, REJECTED
    private Date requestDate;
    private Date responseDate;
    private String responderId;
    
    // Empty constructor for Firebase
    public Approval() {
    }
    
    public Approval(String userId, String userName, String userPhotoUrl, 
                   String requestType, String requestDetails) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
        this.requestType = requestType;
        this.requestDetails = requestDetails;
        this.status = "PENDING";
        this.requestDate = new Date();
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
    
    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }
    
    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
    
    public String getRequestType() {
        return requestType;
    }
    
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    
    public String getRequestDetails() {
        return requestDetails;
    }
    
    public void setRequestDetails(String requestDetails) {
        this.requestDetails = requestDetails;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }
    
    public Date getResponseDate() {
        return responseDate;
    }
    
    public void setResponseDate(Date responseDate) {
        this.responseDate = responseDate;
    }
    
    public String getResponderId() {
        return responderId;
    }
    
    public void setResponderId(String responderId) {
        this.responderId = responderId;
    }
    
    // Helper methods
    public boolean isPending() {
        return "PENDING".equals(status);
    }
    
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }
    
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
    
    public void approve(String responderId) {
        this.status = "APPROVED";
        this.responseDate = new Date();
        this.responderId = responderId;
    }
    
    public void reject(String responderId) {
        this.status = "REJECTED";
        this.responseDate = new Date();
        this.responderId = responderId;
    }
} 
