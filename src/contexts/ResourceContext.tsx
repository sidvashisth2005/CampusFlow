import React, { createContext, useContext, useState, useEffect } from 'react';
import { db } from '../firebase';
import { collection, onSnapshot, addDoc, updateDoc, doc, deleteDoc } from 'firebase/firestore';
import { Resource } from '../types/Resource';
import { useAuth } from './AuthContext';

interface ResourceContextType {
  resources: Resource[];
  createResource: (resource: Omit<Resource, 'id'>) => Promise<void>;
  updateResource: (id: string, resource: Partial<Resource>) => Promise<void>;
  deleteResource: (id: string) => Promise<void>;
}

const ResourceContext = createContext<ResourceContextType>({
  resources: [],
  createResource: async () => {},
  updateResource: async () => {},
  deleteResource: async () => {},
});

export const useResource = () => useContext(ResourceContext);

export const ResourceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [resources, setResources] = useState<Resource[]>([]);
  const { role } = useAuth();

  useEffect(() => {
    const q = collection(db, 'resources');
    const unsubscribe = onSnapshot(q, (snapshot) => {
      const resourceList = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      })) as Resource[];
      setResources(resourceList);
    });

    return unsubscribe;
  }, []);

  const createResource = async (resource: Omit<Resource, 'id'>) => {
    if (role !== 'admin') throw new Error('Only admins can create resources');
    await addDoc(collection(db, 'resources'), resource);
  };

  const updateResource = async (id: string, resource: Partial<Resource>) => {
    if (role !== 'admin') throw new Error('Only admins can update resources');
    await updateDoc(doc(db, 'resources', id), resource);
  };

  const deleteResource = async (id: string) => {
    if (role !== 'admin') throw new Error('Only admins can delete resources');
    await deleteDoc(doc(db, 'resources', id));
  };

  return (
    <ResourceContext.Provider value={{ resources, createResource, updateResource, deleteResource }}>
      {children}
    </ResourceContext.Provider>
  );
}; 