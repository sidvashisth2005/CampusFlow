package com.sid.campusflow.models;

import java.util.Date;

public class Event {
    private String id;
    private String title;
    private String description;
    private String location;
    private String imageUrl;
    private Date startDate;
    private Date endDate;
    private String organizerId;
    private int maxAttendees;
    private boolean requiresRegistration;
    private boolean requiresApproval;
    private boolean featured;
    private long createdAt;

    // Default constructor required for Firebase
    public Event() {
    }

    public Event(String id, String title, String description, String location, String imageUrl,
                 Date startDate, Date endDate, String organizerId, int maxAttendees,
                 boolean requiresRegistration, boolean requiresApproval) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizerId = organizerId;
        this.maxAttendees = maxAttendees;
        this.requiresRegistration = requiresRegistration;
        this.requiresApproval = requiresApproval;
        this.createdAt = new Date().getTime();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public int getMaxAttendees() {
        return maxAttendees;
    }

    public void setMaxAttendees(int maxAttendees) {
        this.maxAttendees = maxAttendees;
    }

    public boolean isRequiresRegistration() {
        return requiresRegistration;
    }

    public void setRequiresRegistration(boolean requiresRegistration) {
        this.requiresRegistration = requiresRegistration;
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Added method to fix compilation error in PreviousEventAdapter
    public Date getEventDate() {
        return startDate;
    }
}
