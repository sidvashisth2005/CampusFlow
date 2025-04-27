package com.sid.campusflow.models;

import java.util.Date;
import java.util.Map;

public class TimeTable {
    private String id;
    private String title;
    private String year;
    private String department;
    private String semester;
    private Map<String, Map<String, String>> schedule; // day -> timeSlot -> subject
    private Date lastUpdated;
    private String uploadedBy;

    // Default constructor required for Firebase
    public TimeTable() {
    }

    public TimeTable(String id, String title, String year, String department, String semester, 
                    Map<String, Map<String, String>> schedule, Date lastUpdated, String uploadedBy) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.department = department;
        this.semester = semester;
        this.schedule = schedule;
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

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Map<String, Map<String, String>> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, Map<String, String>> schedule) {
        this.schedule = schedule;
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
} 