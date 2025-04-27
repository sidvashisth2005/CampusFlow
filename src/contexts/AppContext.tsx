import React, { createContext, useContext, useState, useEffect } from 'react';
import { useUser } from './UserContext';
import { db } from '../firebase';
import { doc, onSnapshot } from 'firebase/firestore';

interface AppSettings {
  language: string;
  notifications: {
    email: boolean;
    push: boolean;
    sound: boolean;
  };
  privacy: {
    profileVisibility: 'public' | 'private' | 'friends';
    showEmail: boolean;
    showPhone: boolean;
  };
}

interface AppContextType {
  settings: AppSettings;
  updateSettings: (settings: Partial<AppSettings>) => void;
  isOnline: boolean;
  isMobile: boolean;
}

const defaultSettings: AppSettings = {
  language: 'en',
  notifications: {
    email: true,
    push: true,
    sound: true,
  },
  privacy: {
    profileVisibility: 'public',
    showEmail: false,
    showPhone: false,
  },
};

const AppContext = createContext<AppContextType>({
  settings: defaultSettings,
  updateSettings: () => {},
  isOnline: true,
  isMobile: false,
});

export const useApp = () => useContext(AppContext);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { userProfile } = useUser();
  const [settings, setSettings] = useState<AppSettings>(defaultSettings);
  const [isOnline, setIsOnline] = useState(true);
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    if (!userProfile) return;

    const unsubscribe = onSnapshot(
      doc(db, 'settings', userProfile.id),
      (doc) => {
        if (doc.exists()) {
          setSettings({ ...defaultSettings, ...doc.data() });
        }
      }
    );

    return () => unsubscribe();
  }, [userProfile]);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    const handleResize = () => setIsMobile(window.innerWidth < 768);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    window.addEventListener('resize', handleResize);
    handleResize();

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  const updateSettings = async (newSettings: Partial<AppSettings>) => {
    if (!userProfile) return;
    // Implement Firestore update
    setSettings((prev) => ({ ...prev, ...newSettings }));
  };

  return (
    <AppContext.Provider
      value={{
        settings,
        updateSettings,
        isOnline,
        isMobile,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}; 