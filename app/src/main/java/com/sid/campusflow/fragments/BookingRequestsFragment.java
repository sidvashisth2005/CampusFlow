package com.sid.campusflow.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sid.campusflow.R;
import com.sid.campusflow.adapters.BookingRequestAdapter;
import com.sid.campusflow.models.BookingRequest;
import com.sid.campusflow.models.Room;

import java.util.ArrayList;
import java.util.List;

public class BookingRequestsFragment extends Fragment {
    private RecyclerView requestsRecyclerView;
    private BookingRequestAdapter requestAdapter;
    private List<BookingRequest> bookingRequests;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView noRequestsText;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking_requests, container, false);
        
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        requestsRecyclerView = view.findViewById(R.id.requestsRecyclerView);
        noRequestsText = view.findViewById(R.id.noRequestsText);
        
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        bookingRequests = new ArrayList<>();
        requestAdapter = new BookingRequestAdapter(bookingRequests, this::handleRequestAction);
        requestsRecyclerView.setAdapter(requestAdapter);
        
        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadBookingRequests);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Load booking requests
        loadBookingRequests();
    }

    private void loadBookingRequests() {
        swipeRefreshLayout.setRefreshing(true);
        db.collection("bookingRequests")
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingRequests.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        BookingRequest request = document.toObject(BookingRequest.class);
                        request.setId(document.getId());
                        bookingRequests.add(request);
                    }
                    requestAdapter.notifyDataSetChanged();
                    updateUI();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingRequestsFragment", "Error loading requests: " + e.getMessage());
                    Toast.makeText(getContext(), "Error loading requests", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
    }

    private void updateUI() {
        if (bookingRequests.isEmpty()) {
            noRequestsText.setVisibility(View.VISIBLE);
            requestsRecyclerView.setVisibility(View.GONE);
        } else {
            noRequestsText.setVisibility(View.GONE);
            requestsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void handleRequestAction(BookingRequest request, String action) {
        if (action.equals("APPROVE")) {
            approveRequest(request);
        } else if (action.equals("REJECT")) {
            rejectRequest(request);
        }
    }

    private void approveRequest(BookingRequest request) {
        // Check if the room is still available
        db.collection("rooms")
                .document(request.getRoomId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Room room = documentSnapshot.toObject(Room.class);
                    if (room != null) {
                        // Update booking request status
                        request.setStatus("APPROVED");
                        request.setAdminId(auth.getCurrentUser().getUid());
                        request.setAdminName(auth.getCurrentUser().getDisplayName());
                        request.setProcessedAt(new java.util.Date());

                        // Update the request in Firestore
                        db.collection("bookingRequests")
                                .document(request.getId())
                                .set(request)
                                .addOnSuccessListener(aVoid -> {
                                    // Add booking to room's current bookings
                                    List<String> currentBookings = room.getCurrentBookings();
                                    if (currentBookings == null) {
                                        currentBookings = new ArrayList<>();
                                    }
                                    currentBookings.add(request.getId());
                                    room.setCurrentBookings(currentBookings);

                                    // Update room in Firestore
                                    db.collection("rooms")
                                            .document(request.getRoomId())
                                            .set(room)
                                            .addOnSuccessListener(aVoid1 -> {
                                                Toast.makeText(getContext(), 
                                                             "Booking approved successfully", 
                                                             Toast.LENGTH_SHORT).show();
                                                loadBookingRequests();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("BookingRequestsFragment", 
                                                      "Error updating room: " + e.getMessage());
                                                Toast.makeText(getContext(), 
                                                             "Error updating room", 
                                                             Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("BookingRequestsFragment", 
                                          "Error approving request: " + e.getMessage());
                                    Toast.makeText(getContext(), 
                                                 "Error approving request", 
                                                 Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingRequestsFragment", "Error checking room: " + e.getMessage());
                    Toast.makeText(getContext(), "Error checking room", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectRequest(BookingRequest request) {
        // Show dialog to get rejection reason
        RejectionDialogFragment dialog = new RejectionDialogFragment(reason -> {
            request.setStatus("REJECTED");
            request.setAdminId(auth.getCurrentUser().getUid());
            request.setAdminName(auth.getCurrentUser().getDisplayName());
            request.setProcessedAt(new java.util.Date());
            request.setRejectionReason(reason);

            // Update the request in Firestore
            db.collection("bookingRequests")
                    .document(request.getId())
                    .set(request)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Booking rejected successfully", 
                                     Toast.LENGTH_SHORT).show();
                        loadBookingRequests();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookingRequestsFragment", 
                              "Error rejecting request: " + e.getMessage());
                        Toast.makeText(getContext(), "Error rejecting request", 
                                     Toast.LENGTH_SHORT).show();
                    });
        });
        dialog.show(getChildFragmentManager(), "RejectionDialog");
    }
} 