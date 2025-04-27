import React, { createContext, useContext, useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, onSnapshot, addDoc, updateDoc, doc, query, where } from 'firebase/firestore';
import { Feedback } from '../types/Feedback';
import { useAuth } from './AuthContext';

interface FeedbackContextType {
  feedbacks: Feedback[];
  createFeedback: (feedback: Omit<Feedback, 'id' | 'createdAt' | 'status'>) => Promise<void>;
  updateFeedback: (id: string, feedback: Partial<Feedback>) => Promise<void>;
  getFeedbackById: (id: string) => Feedback | undefined;
}

const FeedbackContext = createContext<FeedbackContextType>({
  feedbacks: [],
  createFeedback: async () => {},
  updateFeedback: async () => {},
  getFeedbackById: () => undefined,
});

export const useFeedback = () => useContext(FeedbackContext);

export const FeedbackProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [feedbacks, setFeedbacks] = useState<Feedback[]>([]);
  const { user } = useAuth();

  useEffect(() => {
    if (!user) return;

    const q = query(
      collection(db, 'feedbacks'),
      where('userId', '==', user.uid)
    );
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const feedbackList = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as Feedback[];
      setFeedbacks(feedbackList);
    });

    return unsubscribe;
  }, [user]);

  const createFeedback = async (feedback: Omit<Feedback, 'id' | 'createdAt' | 'status'>) => {
    const newFeedback = {
      ...feedback,
      createdAt: new Date(),
      status: 'pending'
    };
    await addDoc(collection(db, 'feedbacks'), newFeedback);
  };

  const updateFeedback = async (id: string, feedback: Partial<Feedback>) => {
    await updateDoc(doc(db, 'feedbacks', id), feedback);
  };

  const getFeedbackById = (id: string) => {
    return feedbacks.find(feedback => feedback.id === id);
  };

  return (
    <FeedbackContext.Provider value={{ feedbacks, createFeedback, updateFeedback, getFeedbackById }}>
      {children}
    </FeedbackContext.Provider>
  );
}; 