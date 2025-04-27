import React, { createContext, useContext, useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, query, where, onSnapshot, addDoc, updateDoc, doc } from 'firebase/firestore';
import { useAuth } from './AuthContext';
import { BookingRequest } from '../types/BookingRequest';

interface BookingContextType {
  bookings: BookingRequest[];
  createBooking: (booking: Omit<BookingRequest, 'id' | 'status'>) => Promise<void>;
  updateBookingStatus: (id: string, status: 'APPROVED' | 'REJECTED') => Promise<void>;
}

const BookingContext = createContext<BookingContextType>({
  bookings: [],
  createBooking: async () => {},
  updateBookingStatus: async () => {},
});

export const useBooking = () => useContext(BookingContext);

export const BookingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [bookings, setBookings] = useState<BookingRequest[]>([]);
  const { user, role } = useAuth();

  useEffect(() => {
    if (!user) return;

    let q;
    if (role === 'admin') {
      q = query(collection(db, 'bookings'));
    } else {
      q = query(
        collection(db, 'bookings'),
        where('requesterId', '==', user.uid)
      );
    }

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const bookingList = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as BookingRequest[];
      setBookings(bookingList);
    });

    return unsubscribe;
  }, [user, role]);

  const createBooking = async (booking: Omit<BookingRequest, 'id' | 'status'>) => {
    await addDoc(collection(db, 'bookings'), {
      ...booking,
      status: 'PENDING'
    });
  };

  const updateBookingStatus = async (id: string, status: 'APPROVED' | 'REJECTED') => {
    await updateDoc(doc(db, 'bookings', id), { status });
  };

  return (
    <BookingContext.Provider value={{ bookings, createBooking, updateBookingStatus }}>
      {children}
    </BookingContext.Provider>
  );
}; 