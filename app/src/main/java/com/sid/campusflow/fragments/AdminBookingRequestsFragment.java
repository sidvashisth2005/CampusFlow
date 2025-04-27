package com.sid.campusflow.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.sid.campusflow.R;
import com.sid.campusflow.adapters.BookingRequestAdapter;
import com.sid.campusflow.models.BookingRequest;
import com.sid.campusflow.models.Room;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminBookingRequestsFragment extends Fragment implements BookingRequestAdapter.OnRequestActionListener {
    private RecyclerView requestsRecyclerView;
    private BookingRequestAdapter requestAdapter;
    private List<BookingRequest> pendingRequests;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_requests, container, false);

        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        pendingRequests = new ArrayList<>();
        requestAdapter = new BookingRequestAdapter(pendingRequests, this);
        requestsRecyclerView.setAdapter(requestAdapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadPendingRequests();

        return view;
    }

    private void loadPendingRequests() {
        db.collection("bookingRequests")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingRequest request = document.toObject(BookingRequest.class);
                        request.setId(document.getId());
                        pendingRequests.add(request);
                    }
                    requestAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading requests: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRequestAction(BookingRequest request, String action) {
        if (action.equals("APPROVE")) {
            approveRequest(request);
        } else if (action.equals("REJECT")) {
            rejectRequest(request);
        }
    }

    private void approveRequest(BookingRequest request) {
        // Start a batch write
        WriteBatch batch = db.batch();

        // Update request status
        request.setStatus("APPROVED");
        request.setAdminId(auth.getCurrentUser().getUid());
        request.setProcessedAt(new Date());

        DocumentReference requestRef = db.collection("bookingRequests").document(request.getId());
        batch.set(requestRef, request);

        // Create booking
        com.sid.campusflow.models.Booking booking = new com.sid.campusflow.models.Booking(
                db.collection("bookings").document().getId(),
                request.getRoomId(),
                request.getUserId(),
                request.getUserName(),
                request.getUserDesignation(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPurpose(),
                request.getAdminId()
        );

        DocumentReference bookingRef = db.collection("bookings").document(booking.getId());
        batch.set(bookingRef, booking);

        // Update room availability
        DocumentReference roomRef = db.collection("rooms").document(request.getRoomId());
        roomRef.get().addOnSuccessListener(documentSnapshot -> {
            Room room = documentSnapshot.toObject(Room.class);
            if (room != null) {
                List<String> currentBookings = room.getCurrentBookings();
                if (currentBookings == null) {
                    currentBookings = new ArrayList<>();
                }
                currentBookings.add(booking.getId());
                room.setCurrentBookings(currentBookings);
                batch.set(roomRef, room);

                // Commit the batch
                batch.commit()
                        .addOnSuccessListener(aVoid -> {
                            // Schedule a task to update room availability after booking ends
                            scheduleRoomAvailabilityUpdate(roomRef, booking.getId(), request.getEndTime());
                            
                            // Reload requests
                            loadPendingRequests();
                            Toast.makeText(getContext(), "Request approved successfully",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Error approving request: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void rejectRequest(BookingRequest request) {
        request.setStatus("REJECTED");
        request.setAdminId(auth.getCurrentUser().getUid());
        request.setProcessedAt(new Date());

        db.collection("bookingRequests")
                .document(request.getId())
                .set(request)
                .addOnSuccessListener(aVoid -> {
                    loadPendingRequests();
                    Toast.makeText(getContext(), "Request rejected successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error rejecting request: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleRoomAvailabilityUpdate(DocumentReference roomRef, String bookingId, Date endTime) {
        long delay = endTime.getTime() - System.currentTimeMillis();
        if (delay > 0) {
            new android.os.Handler().postDelayed(() -> {
                roomRef.get().addOnSuccessListener(documentSnapshot -> {
                    Room room = documentSnapshot.toObject(Room.class);
                    if (room != null) {
                        List<String> currentBookings = room.getCurrentBookings();
                        if (currentBookings != null) {
                            currentBookings.remove(bookingId);
                            room.setCurrentBookings(currentBookings);
                            roomRef.set(room);
                        }
                    }
                });
            }, delay);
        }
    }
} 