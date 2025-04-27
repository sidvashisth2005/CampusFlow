package com.sid.campusflow.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sid.campusflow.R;
import com.sid.campusflow.adapters.RoomAdapter;
import com.sid.campusflow.models.Room;
import com.sid.campusflow.models.BookingRequest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BookingFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private Spinner roomTypeSpinner;
    private Spinner capacitySpinner;
    private DatePicker datePicker;
    private Button searchButton;
    private Button requestBookingButton;
    private RecyclerView roomsRecyclerView;
    private TextView noRoomsText;
    private RoomAdapter roomAdapter;
    private List<Room> availableRooms;
    private Room selectedRoom;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);

        // Initialize views
        roomTypeSpinner = view.findViewById(R.id.roomTypeSpinner);
        capacitySpinner = view.findViewById(R.id.capacitySpinner);
        datePicker = view.findViewById(R.id.datePicker);
        searchButton = view.findViewById(R.id.searchButton);
        requestBookingButton = view.findViewById(R.id.requestBookingButton);
        roomsRecyclerView = view.findViewById(R.id.roomsRecyclerView);
        noRoomsText = view.findViewById(R.id.noRoomsText);
        progressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        try {
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error initializing Firebase: " + e.getMessage(), 
                         Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup spinners
        setupSpinners();

        // Setup RecyclerView
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        availableRooms = new ArrayList<>();
        roomAdapter = new RoomAdapter(availableRooms, room -> {
            selectedRoom = room;
            requestBookingButton.setEnabled(true);
        });
        roomsRecyclerView.setAdapter(roomAdapter);

        // Setup search button
        searchButton.setOnClickListener(v -> {
            if (checkAndRequestPermissions()) {
                if (db != null && auth != null) {
                    searchRooms();
                } else {
                    Toast.makeText(getContext(), "Firebase not initialized properly", 
                                 Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Setup request booking button
        requestBookingButton.setOnClickListener(v -> {
            if (db != null && auth != null) {
                requestBooking();
            } else {
                Toast.makeText(getContext(), "Firebase not initialized properly", 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinners() {
        // Room types
        String[] roomTypes = {"Classroom", "Laboratory", "Conference Room", "Auditorium"};
        ArrayAdapter<String> roomTypeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, roomTypes);
        roomTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomTypeSpinner.setAdapter(roomTypeAdapter);

        // Capacities
        String[] capacities = {"10-20", "21-50", "51-100", "100+"};
        ArrayAdapter<String> capacityAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, capacities);
        capacityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitySpinner.setAdapter(capacityAdapter);
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsNeeded = new ArrayList<>();
            
            // Check if we have internet permission
            if (getContext().checkSelfPermission(Manifest.permission.INTERNET) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.INTERNET);
            }
            
            // Check if we have network state permission
            if (getContext().checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
            }

            if (!permissionsNeeded.isEmpty()) {
                requestPermissions(permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                searchRooms();
            } else {
                Toast.makeText(getContext(), "Permissions are required to search rooms", 
                             Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchRooms() {
        if (db == null) {
            Log.e("BookingFragment", "Firebase database is null");
            Toast.makeText(getContext(), "Database not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        final Date selectedDate = calendar.getTime();

        String selectedType = roomTypeSpinner.getSelectedItem().toString();
        String selectedCapacity = capacitySpinner.getSelectedItem().toString();

        Log.d("BookingFragment", "Searching rooms with type: " + selectedType + 
              ", capacity: " + selectedCapacity + ", date: " + selectedDate);

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        availableRooms.clear();
        roomAdapter.notifyDataSetChanged();

        // Query Firestore for rooms of the selected type
        db.collection("rooms")
                .whereEqualTo("type", selectedType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("BookingFragment", "Found " + queryDocumentSnapshots.size() + " rooms of type " + selectedType);
                    
                    List<Room> matchingRooms = new ArrayList<>();
                    int totalRooms = queryDocumentSnapshots.size();
                    AtomicInteger processedRooms = new AtomicInteger(0);

                    if (totalRooms == 0) {
                        Log.d("BookingFragment", "No rooms found for type: " + selectedType);
                        progressBar.setVisibility(View.GONE);
                        updateUI();
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Room room = document.toObject(Room.class);
                        room.setId(document.getId());
                        
                        Log.d("BookingFragment", "Processing room: " + room.getName() + 
                              " (Capacity: " + room.getCapacity() + ")");
                        
                        // Check if room has the required capacity
                        if (!isCapacityMatch(room.getCapacity(), selectedCapacity)) {
                            Log.d("BookingFragment", "Room " + room.getName() + " capacity mismatch");
                            processedRooms.incrementAndGet();
                            if (processedRooms.get() == totalRooms) {
                                updateRoomsList(matchingRooms);
                            }
                            continue;
                        }

                        // Check if room has any current bookings
                        List<String> currentBookings = room.getCurrentBookings();
                        if (currentBookings != null && !currentBookings.isEmpty()) {
                            Log.d("BookingFragment", "Room " + room.getName() + " has " + 
                                  currentBookings.size() + " bookings to check");
                            
                            AtomicInteger processedBookings = new AtomicInteger(0);
                            boolean[] isAvailable = {true};

                            for (String bookingId : currentBookings) {
                                db.collection("bookings").document(bookingId).get()
                                        .addOnSuccessListener(bookingDoc -> {
                                            com.sid.campusflow.models.Booking booking = bookingDoc.toObject(com.sid.campusflow.models.Booking.class);
                                            if (booking != null) {
                                                Calendar bookingStart = Calendar.getInstance();
                                                bookingStart.setTime(booking.getStartTime());
                                                Calendar bookingEnd = Calendar.getInstance();
                                                bookingEnd.setTime(booking.getEndTime());

                                                Log.d("BookingFragment", "Checking booking: " + bookingId + 
                                                      " (Start: " + bookingStart.getTime() + 
                                                      ", End: " + bookingEnd.getTime() + ")");

                                                // Check if selected date falls within any booking period
                                                if (selectedDate.after(bookingStart.getTime()) && 
                                                    selectedDate.before(bookingEnd.getTime())) {
                                                    Log.d("BookingFragment", "Room " + room.getName() + 
                                                          " is booked for selected date");
                                                    isAvailable[0] = false;
                                                }
                                            }
                                            
                                            processedBookings.incrementAndGet();
                                            if (processedBookings.get() == currentBookings.size()) {
                                                if (isAvailable[0]) {
                                                    Log.d("BookingFragment", "Room " + room.getName() + 
                                                          " is available");
                                                    matchingRooms.add(room);
                                                }
                                                processedRooms.incrementAndGet();
                                                if (processedRooms.get() == totalRooms) {
                                                    updateRoomsList(matchingRooms);
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("BookingFragment", "Error checking booking " + bookingId + 
                                                  ": " + e.getMessage());
                                            processedBookings.incrementAndGet();
                                            if (processedBookings.get() == currentBookings.size()) {
                                                processedRooms.incrementAndGet();
                                                if (processedRooms.get() == totalRooms) {
                                                    updateRoomsList(matchingRooms);
                                                }
                                            }
                                        });
                            }
                        } else {
                            // Room has no bookings, so it's available
                            Log.d("BookingFragment", "Room " + room.getName() + " has no bookings");
                            matchingRooms.add(room);
                            processedRooms.incrementAndGet();
                            if (processedRooms.get() == totalRooms) {
                                updateRoomsList(matchingRooms);
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingFragment", "Error searching rooms: " + e.getMessage(), e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error searching rooms: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRoomsList(List<Room> rooms) {
        Log.d("BookingFragment", "Updating UI with " + rooms.size() + " available rooms");
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                availableRooms.clear();
                availableRooms.addAll(rooms);
                roomAdapter.notifyDataSetChanged();
                updateUI();
            });
        }
    }

    private boolean isCapacityMatch(int roomCapacity, String selectedCapacity) {
        switch (selectedCapacity) {
            case "10-20":
                return roomCapacity >= 10 && roomCapacity <= 20;
            case "21-50":
                return roomCapacity >= 21 && roomCapacity <= 50;
            case "51-100":
                return roomCapacity >= 51 && roomCapacity <= 100;
            case "100+":
                return roomCapacity > 100;
            default:
                return false;
        }
    }

    private void updateUI() {
        if (availableRooms.isEmpty()) {
            noRoomsText.setVisibility(View.VISIBLE);
            roomsRecyclerView.setVisibility(View.GONE);
        } else {
            noRoomsText.setVisibility(View.GONE);
            roomsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void requestBooking() {
        if (selectedRoom == null) {
            Toast.makeText(getContext(), "Please select a room first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user's information
        String userId = auth.getCurrentUser().getUid();
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String userName = documentSnapshot.getString("name");
                    String userDesignation = documentSnapshot.getString("designation");

                    if (userName == null || userDesignation == null) {
                        Toast.makeText(getContext(), "User information not found", 
                                     Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create booking request
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(datePicker.getYear(), datePicker.getMonth(), 
                               datePicker.getDayOfMonth(), 9, 0); // Default start time 9 AM
                    Date startTime = calendar.getTime();
                    calendar.add(Calendar.HOUR, 2); // Default 2-hour booking
                    Date endTime = calendar.getTime();

                    String requestId = db.collection("bookingRequests").document().getId();
                    BookingRequest request = new BookingRequest(
                            requestId,
                            selectedRoom.getId(),
                            userId,
                            userName,
                            userDesignation,
                            startTime,
                            endTime,
                            "Meeting" // Default purpose
                    );

                    // Save booking request
                    db.collection("bookingRequests")
                            .document(requestId)
                            .set(request)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), 
                                             "Booking request submitted successfully", 
                                             Toast.LENGTH_SHORT).show();
                                selectedRoom = null;
                                requestBookingButton.setEnabled(false);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("BookingFragment", 
                                      "Error submitting request: " + e.getMessage());
                                Toast.makeText(getContext(), 
                                             "Error submitting request: " + e.getMessage(), 
                                             Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("BookingFragment", "Error getting user info: " + e.getMessage());
                    Toast.makeText(getContext(), 
                                 "Error getting user information", 
                                 Toast.LENGTH_SHORT).show();
                });
    }
} 
