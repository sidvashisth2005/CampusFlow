import React, { createContext, useContext, useState, useEffect } from 'react';
import { db } from '../firebase';
import { doc, getDoc, updateDoc, onSnapshot } from 'firebase/firestore';
import { UserProfile } from '../types/UserProfile';
import { useAuth } from './AuthContext';

interface UserContextType {
  userProfile: UserProfile | null;
  updateUserProfile: (data: Partial<UserProfile>) => Promise<void>;
  isLoading: boolean;
}

const UserContext = createContext<UserContextType>({
  userProfile: null,
  updateUserProfile: async () => {},
  isLoading: true,
});

export const useUser = () => useContext(UserContext);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    if (!user) {
      setUserProfile(null);
      setIsLoading(false);
      return;
    }

    const userRef = doc(db, 'users', user.uid);
    const unsubscribe = onSnapshot(userRef, (doc) => {
      if (doc.exists()) {
        setUserProfile(doc.data() as UserProfile);
      } else {
        setUserProfile(null);
      }
      setIsLoading(false);
    });

    return unsubscribe;
  }, [user]);

  const updateUserProfile = async (data: Partial<UserProfile>) => {
    if (!user) return;
    const userRef = doc(db, 'users', user.uid);
    await updateDoc(userRef, data);
  };

  return (
    <UserContext.Provider value={{ userProfile, updateUserProfile, isLoading }}>
      {children}
    </UserContext.Provider>
  );
}; 