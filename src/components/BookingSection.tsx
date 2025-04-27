import React, { useState, useEffect } from 'react';
import { collection, query, where, getDocs, addDoc, updateDoc, doc } from 'firebase/firestore';
import { db } from '../firebase';
import { useAuth } from '../contexts/AuthContext';
import { Room } from '../types/Room';
import { BookingRequest } from '../types/BookingRequest';

const BookingSection: React.FC = () => {
  const { currentUser } = useAuth();
  const [rooms, setRooms] = useState<Room[]>([]);
  const [filteredRooms, setFilteredRooms] = useState<Room[]>([]);
  const [bookingRequests, setBookingRequests] = useState<BookingRequest[]>([]);
  const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [purpose, setPurpose] = useState('');
  const [filters, setFilters] = useState({
    building: '',
    capacity: '',
    type: ''
  });

  useEffect(() => {
    fetchRooms();
    fetchBookingRequests();
  }, []);

  useEffect(() => {
    filterRooms();
  }, [rooms, filters]);

  const fetchRooms = async () => {
    const roomsRef = collection(db, 'rooms');
    const snapshot = await getDocs(roomsRef);
    const roomsData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as Room));
    setRooms(roomsData);
  };

  const fetchBookingRequests = async () => {
    if (!currentUser) return;

    const requestsRef = collection(db, 'bookingRequests');
    let q = query(requestsRef);

    if (currentUser.role === 'student') {
      q = query(requestsRef, where('requesterId', '==', currentUser.uid));
    } else if (currentUser.role === 'faculty') {
      q = query(requestsRef, where('requesterId', '==', currentUser.uid));
    } else if (currentUser.role === 'secretary') {
      q = query(requestsRef, where('status', '==', 'PENDING'));
    }

    const snapshot = await getDocs(q);
    const requestsData = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() } as BookingRequest));
    setBookingRequests(requestsData);
  };

  const filterRooms = () => {
    let filtered = [...rooms];

    if (filters.building) {
      filtered = filtered.filter(room => room.buildingName === filters.building);
    }

    if (filters.capacity) {
      filtered = filtered.filter(room => room.capacity >= parseInt(filters.capacity));
    }

    if (filters.type) {
      filtered = filtered.filter(room => room.type === filters.type);
    }

    setFilteredRooms(filtered);
  };

  const handleBookingRequest = async () => {
    if (!currentUser || !selectedRoom) return;

    const request: Omit<BookingRequest, 'id'> = {
      roomId: selectedRoom.id,
      requesterId: currentUser.uid,
      requesterName: currentUser.name,
      requesterDesignation: currentUser.role,
      startTime: new Date(startTime),
      endTime: new Date(endTime),
      status: 'PENDING',
      purpose
    };

    await addDoc(collection(db, 'bookingRequests'), request);
    fetchBookingRequests();
  };

  const handleApproveRequest = async (requestId: string) => {
    if (!currentUser || currentUser.role !== 'secretary') return;

    const requestRef = doc(db, 'bookingRequests', requestId);
    await updateDoc(requestRef, { status: 'APPROVED' });
    fetchBookingRequests();
  };

  const handleRejectRequest = async (requestId: string) => {
    if (!currentUser || currentUser.role !== 'secretary') return;

    const requestRef = doc(db, 'bookingRequests', requestId);
    await updateDoc(requestRef, { status: 'REJECTED' });
    fetchBookingRequests();
  };

  return (
    <div className="p-4">
      <h2 className="text-2xl font-bold mb-4">Room Booking</h2>
      
      {/* Filters */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <select
          className="p-2 border rounded"
          value={filters.building}
          onChange={(e) => setFilters({ ...filters, building: e.target.value })}
        >
          <option value="">All Buildings</option>
          {Array.from(new Set(rooms.map(room => room.buildingName))).map(building => (
            <option key={building} value={building}>{building}</option>
          ))}
        </select>

        <select
          className="p-2 border rounded"
          value={filters.capacity}
          onChange={(e) => setFilters({ ...filters, capacity: e.target.value })}
        >
          <option value="">Any Capacity</option>
          <option value="10">10+</option>
          <option value="20">20+</option>
          <option value="30">30+</option>
          <option value="50">50+</option>
        </select>

        <select
          className="p-2 border rounded"
          value={filters.type}
          onChange={(e) => setFilters({ ...filters, type: e.target.value })}
        >
          <option value="">All Types</option>
          <option value="Classroom">Classroom</option>
          <option value="Laboratory">Laboratory</option>
          <option value="Conference Room">Conference Room</option>
        </select>
      </div>

      {/* Available Rooms */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        {filteredRooms.map(room => (
          <div key={room.id} className="border p-4 rounded">
            <h3 className="font-bold">{room.name}</h3>
            <p>Building: {room.buildingName}</p>
            <p>Capacity: {room.capacity}</p>
            <p>Type: {room.type}</p>
            <button
              className="mt-2 bg-blue-500 text-white px-4 py-2 rounded"
              onClick={() => setSelectedRoom(room)}
            >
              Book This Room
            </button>
          </div>
        ))}
      </div>

      {/* Booking Form */}
      {selectedRoom && (
        <div className="border p-4 rounded mb-6">
          <h3 className="font-bold mb-4">Book {selectedRoom.name}</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <input
              type="datetime-local"
              className="p-2 border rounded"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
            />
            <input
              type="datetime-local"
              className="p-2 border rounded"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
            />
            <input
              type="text"
              className="p-2 border rounded"
              placeholder="Purpose"
              value={purpose}
              onChange={(e) => setPurpose(e.target.value)}
            />
            <button
              className="bg-green-500 text-white px-4 py-2 rounded"
              onClick={handleBookingRequest}
            >
              Submit Booking Request
            </button>
          </div>
        </div>
      )}

      {/* Booking Requests */}
      <div className="mt-8">
        <h3 className="text-xl font-bold mb-4">Booking Requests</h3>
        <div className="space-y-4">
          {bookingRequests.map(request => (
            <div key={request.id} className="border p-4 rounded">
              <p>Room: {request.roomId}</p>
              <p>Requester: {request.requesterName}</p>
              <p>Start Time: {request.startTime.toDate().toLocaleString()}</p>
              <p>End Time: {request.endTime.toDate().toLocaleString()}</p>
              <p>Purpose: {request.purpose}</p>
              <p>Status: {request.status}</p>
              
              {currentUser?.role === 'secretary' && request.status === 'PENDING' && (
                <div className="mt-2 space-x-2">
                  <button
                    className="bg-green-500 text-white px-4 py-2 rounded"
                    onClick={() => handleApproveRequest(request.id)}
                  >
                    Approve
                  </button>
                  <button
                    className="bg-red-500 text-white px-4 py-2 rounded"
                    onClick={() => handleRejectRequest(request.id)}
                  >
                    Reject
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default BookingSection; 