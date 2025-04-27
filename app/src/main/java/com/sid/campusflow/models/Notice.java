package com.sid.campusflow.models;

import java.util.Date;

public class Notice {
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String authorName;
    private Date publishDate;
    private Date expiryDate;
    private boolean isImportant;
    private String departmentId;
    private String attachmentUrl;

    // Default constructor for Firebase
    public Notice() {
    }

    public Notice(String id, String title, String content, String authorId, String authorName, 
                 Date publishDate, Date expiryDate, boolean isImportant, String departmentId,
                 String attachmentUrl) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.authorName = authorName;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.isImportant = isImportant;
        this.departmentId = departmentId;
        this.attachmentUrl = attachmentUrl;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }
} 