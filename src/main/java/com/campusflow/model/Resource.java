package com.campusflow.model;

import java.util.List;

public class Resource {
    private String id;
    private String name;
    private String type; // ROOM, EQUIPMENT, VENUE
    private String location;
    private int capacity;
    private List<String> features;
    private String description;
    private boolean isActive;
    private String managedBy; // User ID of the resource manager
    private List<String> allowedRoles; // Roles that can book this resource
    private int maxBookingDuration; // in minutes
    private int minBookingDuration; // in minutes
    private List<String> bookingRestrictions; // Additional restrictions

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String managedBy) {
        this.managedBy = managedBy;
    }

    public List<String> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(List<String> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public int getMaxBookingDuration() {
        return maxBookingDuration;
    }

    public void setMaxBookingDuration(int maxBookingDuration) {
        this.maxBookingDuration = maxBookingDuration;
    }

    public int getMinBookingDuration() {
        return minBookingDuration;
    }

    public void setMinBookingDuration(int minBookingDuration) {
        this.minBookingDuration = minBookingDuration;
    }

    public List<String> getBookingRestrictions() {
        return bookingRestrictions;
    }

    public void setBookingRestrictions(List<String> bookingRestrictions) {
        this.bookingRestrictions = bookingRestrictions;
    }
} 