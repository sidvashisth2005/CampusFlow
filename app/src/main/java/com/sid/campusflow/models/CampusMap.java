package com.sid.campusflow.models;

import java.util.Date;
import java.util.Map;

public class CampusMap {
    private String id;
    private String title;
    private String mapImageUrl;
    private String description;
    private Map<String, MapLocation> locations; // id -> location details
    private Date lastUpdated;
    private String uploadedBy;

    // Default constructor required for Firebase
    public CampusMap() {
    }

    public CampusMap(String id, String title, String mapImageUrl, String description,
                   Map<String, MapLocation> locations, Date lastUpdated, String uploadedBy) {
        this.id = id;
        this.title = title;
        this.mapImageUrl = mapImageUrl;
        this.description = description;
        this.locations = locations;
        this.lastUpdated = lastUpdated;
        this.uploadedBy = uploadedBy;
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

    public String getMapImageUrl() {
        return mapImageUrl;
    }

    public void setMapImageUrl(String mapImageUrl) {
        this.mapImageUrl = mapImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, MapLocation> getLocations() {
        return locations;
    }

    public void setLocations(Map<String, MapLocation> locations) {
        this.locations = locations;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    // Inner class for map locations
    public static class MapLocation {
        private String name;
        private String description;
        private float xCoordinate;
        private float yCoordinate;
        private String buildingCode;
        private String locationType; // classroom, office, lab, etc.

        public MapLocation() {
        }

        public MapLocation(String name, String description, float xCoordinate, float yCoordinate, 
                        String buildingCode, String locationType) {
            this.name = name;
            this.description = description;
            this.xCoordinate = xCoordinate;
            this.yCoordinate = yCoordinate;
            this.buildingCode = buildingCode;
            this.locationType = locationType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public float getXCoordinate() {
            return xCoordinate;
        }

        public void setXCoordinate(float xCoordinate) {
            this.xCoordinate = xCoordinate;
        }

        public float getYCoordinate() {
            return yCoordinate;
        }

        public void setYCoordinate(float yCoordinate) {
            this.yCoordinate = yCoordinate;
        }

        public String getBuildingCode() {
            return buildingCode;
        }

        public void setBuildingCode(String buildingCode) {
            this.buildingCode = buildingCode;
        }

        public String getLocationType() {
            return locationType;
        }

        public void setLocationType(String locationType) {
            this.locationType = locationType;
        }
    }
} 