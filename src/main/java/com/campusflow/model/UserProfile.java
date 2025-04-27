package com.campusflow.model;

import java.util.List;
import java.util.Map;

public class UserProfile {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String designation;
    private List<String> roles;
    private List<String> permissions;
    private boolean isActive;
    private String profilePicture;
    private String phoneNumber;
    private String officeLocation;
    private List<String> managedResources; // Resources this user manages
    private List<String> bookingPreferences; // User's booking preferences

    public enum Role {
        STUDENT,
        FACULTY,
        STAFF,
        ADMIN,
        RESOURCE_MANAGER,
        DEPARTMENT_HEAD,
        DEAN,
        VICE_CHANCELLOR
    }

    public enum Permission {
        BOOK_RESOURCES,
        MANAGE_RESOURCES,
        APPROVE_BOOKINGS,
        VIEW_ALL_BOOKINGS,
        MANAGE_USERS,
        MANAGE_ROLES,
        MANAGE_DEPARTMENTS,
        VIEW_REPORTS,
        MANAGE_SETTINGS
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOfficeLocation() {
        return officeLocation;
    }

    public void setOfficeLocation(String officeLocation) {
        this.officeLocation = officeLocation;
    }

    public List<String> getManagedResources() {
        return managedResources;
    }

    public void setManagedResources(List<String> managedResources) {
        this.managedResources = managedResources;
    }

    public List<String> getBookingPreferences() {
        return bookingPreferences;
    }

    public void setBookingPreferences(List<String> bookingPreferences) {
        this.bookingPreferences = bookingPreferences;
    }

    // Helper methods
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean canBookResource(String resourceId) {
        // Check if user has booking permission and resource is available for their role
        return hasPermission(Permission.BOOK_RESOURCES.name());
    }

    public boolean canManageResource(String resourceId) {
        // Check if user is resource manager or has manage resources permission
        return hasRole(Role.RESOURCE_MANAGER.name()) || 
               hasPermission(Permission.MANAGE_RESOURCES.name()) ||
               managedResources.contains(resourceId);
    }

    public boolean canApproveBookings() {
        return hasRole(Role.RESOURCE_MANAGER.name()) || 
               hasRole(Role.DEPARTMENT_HEAD.name()) ||
               hasRole(Role.DEAN.name()) ||
               hasRole(Role.ADMIN.name()) ||
               hasPermission(Permission.APPROVE_BOOKINGS.name());
    }
} 