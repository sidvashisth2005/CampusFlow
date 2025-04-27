package com.campusflow.context;

import com.campusflow.model.UserProfile;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

public class UserContext {
    private static UserContext instance;
    private final ConcurrentHashMap<String, UserProfile> userCache;
    private final Preferences preferences;
    private UserProfile currentUser;
    private boolean isAuthenticated;

    private UserContext() {
        this.userCache = new ConcurrentHashMap<>();
        this.preferences = Preferences.userNodeForPackage(UserContext.class);
        this.currentUser = null;
        this.isAuthenticated = false;
    }

    public static synchronized UserContext getInstance() {
        if (instance == null) {
            instance = new UserContext();
        }
        return instance;
    }

    public void initialize() {
        // Load last logged in user if exists
        String lastUserId = preferences.get("lastUserId", null);
        if (lastUserId != null) {
            // Load user from cache or database
            currentUser = userCache.get(lastUserId);
            if (currentUser != null) {
                isAuthenticated = true;
            }
        }
    }

    public boolean authenticate(String username, String password) {
        // Implement your authentication logic here
        // This is a placeholder - replace with actual authentication
        UserProfile user = authenticateUser(username, password);
        if (user != null) {
            currentUser = user;
            isAuthenticated = true;
            userCache.put(user.getId(), user);
            preferences.put("lastUserId", user.getId());
            return true;
        }
        return false;
    }

    private UserProfile authenticateUser(String username, String password) {
        // Implement actual authentication logic
        // This could involve database queries, LDAP, etc.
        return null; // Placeholder
    }

    public void logout() {
        currentUser = null;
        isAuthenticated = false;
        preferences.remove("lastUserId");
    }

    public UserProfile getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void updateUserProfile(UserProfile profile) {
        if (currentUser != null && currentUser.getId().equals(profile.getId())) {
            currentUser = profile;
            userCache.put(profile.getId(), profile);
            // Update in database
            updateUserInDatabase(profile);
        }
    }

    private void updateUserInDatabase(UserProfile profile) {
        // Implement database update logic
    }

    public UserProfile getUserById(String userId) {
        // Check cache first
        UserProfile user = userCache.get(userId);
        if (user == null) {
            // Load from database
            user = loadUserFromDatabase(userId);
            if (user != null) {
                userCache.put(userId, user);
            }
        }
        return user;
    }

    private UserProfile loadUserFromDatabase(String userId) {
        // Implement database loading logic
        return null; // Placeholder
    }

    public void clearCache() {
        userCache.clear();
    }
} 