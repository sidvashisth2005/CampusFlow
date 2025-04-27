package com.sid.campusflow.models;

import java.util.List;

public class Room {
    private String id;
    private String name;
    private String type;
    private int capacity;
    private String building;
    private String floor;
    private boolean isAvailable;
    private String description;
    private List<String> currentBookings; // List of booking IDs that are currently active
    private String location;
    private String buildingName;
    private double latitude;
    private double longitude;

    public Room() {
        // Default constructor required for Firestore
    }

    public Room(String id, String name, String type, int capacity, String building, String floor, boolean isAvailable, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.building = building;
        this.floor = floor;
        this.isAvailable = isAvailable;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getCurrentBookings() { return currentBookings; }
    public void setCurrentBookings(List<String> currentBookings) { this.currentBookings = currentBookings; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
} 