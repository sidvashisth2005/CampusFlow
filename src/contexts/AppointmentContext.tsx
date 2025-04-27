import React, { createContext, useContext, useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, onSnapshot, addDoc, updateDoc, doc, query, where } from 'firebase/firestore';
import { Appointment } from '../types/Appointment';
import { useAuth } from './AuthContext';

interface AppointmentContextType {
  appointments: Appointment[];
  createAppointment: (appointment: Omit<Appointment, 'id' | 'createdAt' | 'status'>) => Promise<void>;
  updateAppointment: (id: string, appointment: Partial<Appointment>) => Promise<void>;
  getAppointmentById: (id: string) => Appointment | undefined;
}

const AppointmentContext = createContext<AppointmentContextType>({
  appointments: [],
  createAppointment: async () => {},
  updateAppointment: async () => {},
  getAppointmentById: () => undefined,
});

export const useAppointment = () => useContext(AppointmentContext);

export const AppointmentProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const { user } = useAuth();

  useEffect(() => {
    if (!user) return;

    const q = query(
      collection(db, 'appointments'),
      where('userId', '==', user.uid)
    );
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const appointmentList = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as Appointment[];
      setAppointments(appointmentList);
    });

    return unsubscribe;
  }, [user]);

  const createAppointment = async (appointment: Omit<Appointment, 'id' | 'createdAt' | 'status'>) => {
    const newAppointment = {
      ...appointment,
      createdAt: new Date(),
      status: 'pending'
    };
    await addDoc(collection(db, 'appointments'), newAppointment);
  };

  const updateAppointment = async (id: string, appointment: Partial<Appointment>) => {
    await updateDoc(doc(db, 'appointments', id), appointment);
  };

  const getAppointmentById = (id: string) => {
    return appointments.find(appointment => appointment.id === id);
  };

  return (
    <AppointmentContext.Provider value={{ appointments, createAppointment, updateAppointment, getAppointmentById }}>
      {children}
    </AppointmentContext.Provider>
  );
}; 