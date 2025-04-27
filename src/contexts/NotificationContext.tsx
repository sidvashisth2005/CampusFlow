import React, { createContext, useContext, useState, useEffect } from 'react';
import { useApp } from './AppContext';
import { useUser } from './UserContext';
import { db } from '../firebase';
import { collection, query, where, onSnapshot, orderBy, limit } from 'firebase/firestore';

interface Notification {
  id: string;
  type: 'message' | 'friend_request' | 'event' | 'system';
  title: string;
  message: string;
  read: boolean;
  timestamp: Date;
  data?: any;
}

interface NotificationContextType {
  notifications: Notification[];
  unreadCount: number;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  clearNotifications: () => void;
}

const NotificationContext = createContext<NotificationContextType>({
  notifications: [],
  unreadCount: 0,
  markAsRead: () => {},
  markAllAsRead: () => {},
  clearNotifications: () => {},
});

export const useNotification = () => useContext(NotificationContext);

export const NotificationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { settings } = useApp();
  const { user } = useUser();
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    if (!user || !settings.notifications.enabled) return;

    const notificationsRef = collection(db, 'notifications');
    const q = query(
      notificationsRef,
      where('userId', '==', user.uid),
      orderBy('timestamp', 'desc'),
      limit(50)
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const newNotifications = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
        timestamp: doc.data().timestamp.toDate(),
      })) as Notification[];

      setNotifications(newNotifications);
    });

    return () => unsubscribe();
  }, [user, settings.notifications.enabled]);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const markAsRead = async (id: string) => {
    if (!user) return;
    // Update notification in Firestore
    // This will trigger the onSnapshot listener
  };

  const markAllAsRead = async () => {
    if (!user) return;
    // Update all notifications in Firestore
    // This will trigger the onSnapshot listener
  };

  const clearNotifications = async () => {
    if (!user) return;
    // Delete all notifications in Firestore
    // This will trigger the onSnapshot listener
  };

  return (
    <NotificationContext.Provider
      value={{
        notifications,
        unreadCount,
        markAsRead,
        markAllAsRead,
        clearNotifications,
      }}
    >
      {children}
    </NotificationContext.Provider>
  );
}; 