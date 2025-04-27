package com.campusflow.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class ApplicationContext {
    private static ApplicationContext instance;
    private final Map<String, Object> settings;
    private final Properties config;
    private final Preferences preferences;
    private boolean isOnline;
    private boolean isMobile;

    private ApplicationContext() {
        this.settings = new HashMap<>();
        this.config = new Properties();
        this.preferences = Preferences.userNodeForPackage(ApplicationContext.class);
        this.isOnline = true;
        this.isMobile = false;
        initializeDefaultSettings();
    }

    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    private void initializeDefaultSettings() {
        settings.put("language", "en");
        settings.put("notifications", Map.of(
            "email", true,
            "push", true,
            "sound", true
        ));
        settings.put("privacy", Map.of(
            "profileVisibility", "public",
            "showEmail", false,
            "showPhone", false
        ));
    }

    public void loadConfiguration() {
        // Load configuration from properties file
        try {
            config.load(getClass().getResourceAsStream("/application.properties"));
        } catch (Exception e) {
            // Handle configuration loading error
        }

        // Load user preferences
        settings.put("language", preferences.get("language", "en"));
        settings.put("theme", preferences.get("theme", "light"));
    }

    public void saveConfiguration() {
        // Save user preferences
        preferences.put("language", (String) settings.get("language"));
        preferences.put("theme", (String) settings.get("theme"));
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void updateSettings(Map<String, Object> newSettings) {
        settings.putAll(newSettings);
        saveConfiguration();
    }

    public String getProperty(String key) {
        return config.getProperty(key);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isMobile() {
        return isMobile;
    }

    public void setMobile(boolean mobile) {
        isMobile = mobile;
    }
} 