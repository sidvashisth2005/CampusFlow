export interface BookingRequest {
  id: string;
  roomId: string;
  requesterId: string;
  requesterName: string;
  requesterDesignation: string;
  startTime: Date;
  endTime: Date;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  purpose: string;
} 