package com.sid.campusflow.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateUtils {
    
    /**
     * Returns a string representing how long ago the date was
     * @param date The date to convert
     * @return A string like "2d ago", "1h ago", etc.
     */
    public static String getTimeAgo(Date date) {
        if (date == null) {
            return "Unknown";
        }
        
        long timeDiff = new Date().getTime() - date.getTime();
        
        // Convert difference to appropriate unit
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        if (seconds < 60) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + "m ago";
        } else if (hours < 24) {
            return hours + "h ago";
        } else if (days < 7) {
            return days + "d ago";
        } else if (weeks < 4) {
            return weeks + "w ago";
        } else if (months < 12) {
            return months + "mo ago";
        } else {
            return years + "y ago";
        }
    }
    
    /**
     * Formats date in a friendly format for display
     * @param date The date to format
     * @return A string like "MMM dd, yyyy"
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "Unknown";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Formats time in a friendly format for display
     * @param date The date object containing the time to format
     * @return A string like "hh:mm a"
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "Unknown";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(date);
    }
    
    /**
     * Formats date and time in a friendly format for display
     * @param date The date to format
     * @return A string like "MMM dd, yyyy hh:mm a"
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "Unknown";
        }
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy hh:mm a", java.util.Locale.getDefault());
        return sdf.format(date);
    }
} 
