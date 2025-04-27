package com.campusflow.context;

import com.campusflow.model.Notification;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class NotificationContext {
    private static NotificationContext instance;
    private final ConcurrentHashMap<String, List<Notification>> userNotifications;
    private final ExecutorService notificationExecutor;
    private final List<Consumer<Notification>> notificationListeners;

    private NotificationContext() {
        this.userNotifications = new ConcurrentHashMap<>();
        this.notificationExecutor = Executors.newSingleThreadExecutor();
        this.notificationListeners = new ArrayList<>();
    }

    public static synchronized NotificationContext getInstance() {
        if (instance == null) {
            instance = new NotificationContext();
        }
        return instance;
    }

    public void addNotificationListener(Consumer<Notification> listener) {
        notificationListeners.add(listener);
    }

    public void removeNotificationListener(Consumer<Notification> listener) {
        notificationListeners.remove(listener);
    }

    public void sendNotification(String userId, Notification notification) {
        notificationExecutor.submit(() -> {
            List<Notification> notifications = userNotifications.computeIfAbsent(
                userId, k -> new ArrayList<>()
            );
            notifications.add(notification);
            
            // Notify listeners
            for (Consumer<Notification> listener : notificationListeners) {
                listener.accept(notification);
            }
            
            // Persist notification
            persistNotification(userId, notification);
        });
    }

    private void persistNotification(String userId, Notification notification) {
        // Implement database persistence
        // This could be JDBC, JPA, etc.
    }

    public List<Notification> getUserNotifications(String userId) {
        return userNotifications.getOrDefault(userId, new ArrayList<>());
    }

    public void markAsRead(String userId, String notificationId) {
        notificationExecutor.submit(() -> {
            List<Notification> notifications = userNotifications.get(userId);
            if (notifications != null) {
                notifications.stream()
                    .filter(n -> n.getId().equals(notificationId))
                    .findFirst()
                    .ifPresent(notification -> {
                        notification.setRead(true);
                        updateNotificationInDatabase(userId, notification);
                    });
            }
        });
    }

    private void updateNotificationInDatabase(String userId, Notification notification) {
        // Implement database update
    }

    public void markAllAsRead(String userId) {
        notificationExecutor.submit(() -> {
            List<Notification> notifications = userNotifications.get(userId);
            if (notifications != null) {
                notifications.forEach(notification -> {
                    notification.setRead(true);
                    updateNotificationInDatabase(userId, notification);
                });
            }
        });
    }

    public void clearNotifications(String userId) {
        notificationExecutor.submit(() -> {
            userNotifications.remove(userId);
            // Clear from database
            clearNotificationsFromDatabase(userId);
        });
    }

    private void clearNotificationsFromDatabase(String userId) {
        // Implement database clearing
    }

    public int getUnreadCount(String userId) {
        List<Notification> notifications = userNotifications.get(userId);
        if (notifications == null) return 0;
        return (int) notifications.stream()
            .filter(notification -> !notification.isRead())
            .count();
    }

    public void shutdown() {
        notificationExecutor.shutdown();
    }
} 
} 