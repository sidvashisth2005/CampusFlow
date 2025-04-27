export interface Room {
  id: string;
  name: string;
  buildingName: string;
  capacity: number;
  type: string;
  currentBookings: BookingRequest[];
} 