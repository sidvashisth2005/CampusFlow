package com.sid.campusflow.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BookingFragment extends Fragment {

    private TextInputEditText etDate;
    private AutoCompleteTextView dropdownRoomType;
    private Button btnCheckAvailability;
    private RecyclerView rvAvailableRooms;
    private RecyclerView rvYourBookings;
    private FloatingActionButton fabCreateBooking;
    
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize date formatter
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        
        // Initialize views
        etDate = view.findViewById(R.id.et_date);
        dropdownRoomType = view.findViewById(R.id.dropdown_room_type);
        btnCheckAvailability = view.findViewById(R.id.btn_check_availability);
        rvAvailableRooms = view.findViewById(R.id.rv_available_rooms);
        rvYourBookings = view.findViewById(R.id.rv_your_bookings);
        fabCreateBooking = view.findViewById(R.id.fab_create_booking);
        
        // Setup date picker
        setupDatePicker();
        
        // Setup room type dropdown
        setupRoomTypeDropdown();
        
        // Setup recycler views
        setupAvailableRoomsRecyclerView();
        setupYourBookingsRecyclerView();
        
        // Setup click listeners
        btnCheckAvailability.setOnClickListener(v -> checkAvailability());
        fabCreateBooking.setOnClickListener(v -> createNewBooking());
    }

    private void setupDatePicker() {
        // Set current date as default
        etDate.setText(dateFormatter.format(calendar.getTime()));
        
        // Show date picker when clicked
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        etDate.setText(dateFormatter.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date as today
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            
            datePickerDialog.show();
        });
    }

    private void setupRoomTypeDropdown() {
        String[] roomTypes = new String[]{"Lecture Hall", "Classroom", "Lab", "Conference Room", "Auditorium"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, roomTypes);
        dropdownRoomType.setAdapter(adapter);
    }

    private void setupAvailableRoomsRecyclerView() {
        rvAvailableRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // TODO: Create adapter for available rooms
        // AvailableRoomsAdapter adapter = new AvailableRoomsAdapter(availableRoomsList);
        // rvAvailableRooms.setAdapter(adapter);
    }

    private void setupYourBookingsRecyclerView() {
        rvYourBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // TODO: Create adapter for bookings and fetch user's bookings
        // BookingsAdapter adapter = new BookingsAdapter(bookingsList);
        // rvYourBookings.setAdapter(adapter);
    }

    private void checkAvailability() {
        String date = etDate.getText().toString();
        String roomType = dropdownRoomType.getText().toString();
        
        if (roomType.isEmpty()) {
            dropdownRoomType.setError("Please select a room type");
            return;
        }
        
        // TODO: Fetch available rooms from database
        Toast.makeText(getContext(), "Checking availability for " + roomType + " on " + date, Toast.LENGTH_SHORT).show();
    }

    private void createNewBooking() {
        // TODO: Open booking creation dialog or navigate to booking creation activity
        Toast.makeText(getContext(), "Create new booking", Toast.LENGTH_SHORT).show();
    }
} 
