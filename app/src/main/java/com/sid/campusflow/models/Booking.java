package com.sid.campusflow.models;

import java.util.Date;

public class Booking {
    private String id;
    private String roomId;
    private String requesterId;
    private String requesterName;
    private String requesterDesignation;
    private Date startTime;
    private Date endTime;
    private String purpose;
    private String approvedBy;
    private Date approvedAt;
    private Date createdAt;

    public Booking() {
        // Default constructor required for Firestore
    }

    public Booking(String id, String roomId, String requesterId, String requesterName, 
                  String requesterDesignation, Date startTime, Date endTime, 
                  String purpose, String approvedBy) {
        this.id = id;
        this.roomId = roomId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterDesignation = requesterDesignation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.purpose = purpose;
        this.approvedBy = approvedBy;
        this.approvedAt = new Date();
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getRequesterDesignation() { return requesterDesignation; }
    public void setRequesterDesignation(String requesterDesignation) { this.requesterDesignation = requesterDesignation; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Date getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Date approvedAt) { this.approvedAt = approvedAt; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
} 