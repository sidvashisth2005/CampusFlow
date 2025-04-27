package com.sid.campusflow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.EventDetailActivity;
import com.sid.campusflow.R;
import com.sid.campusflow.adapters.EventAdapter;
import com.sid.campusflow.adapters.EventHighlightAdapter;
import com.sid.campusflow.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment implements EventAdapter.OnEventClickListener, EventHighlightAdapter.OnEventHighlightClickListener {

    private RecyclerView rvUpcomingEvents;
    private RecyclerView rvEventHighlights;
    private FloatingActionButton fabRegisterEvent;
    private View progressBar;
    private TextView tvWelcomeUser;
    private Button btnQuickAction;
    private ImageButton btnViewAllEvents, btnRefreshHighlights;
    
    private EventAdapter upcomingEventsAdapter;
    private EventHighlightAdapter eventHighlightsAdapter;
    
    private List<Event> upcomingEventsList;
    private List<Event> highlightEventsList;
    
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        
        // Initialize lists
        upcomingEventsList = new ArrayList<>();
        highlightEventsList = new ArrayList<>();
        
        // Initialize views
        rvUpcomingEvents = view.findViewById(R.id.rv_upcoming_events);
        rvEventHighlights = view.findViewById(R.id.rv_event_highlights);
        fabRegisterEvent = view.findViewById(R.id.fab_register_event);
        progressBar = view.findViewById(R.id.progress_bar);
        tvWelcomeUser = view.findViewById(R.id.tv_welcome_user);
        btnQuickAction = view.findViewById(R.id.btn_quick_action);
        btnViewAllEvents = view.findViewById(R.id.btn_view_all_events);
        btnRefreshHighlights = view.findViewById(R.id.btn_refresh_highlights);
        
        // Set welcome message
        setWelcomeMessage();
        
        // Setup upcoming events recycler view
        setupUpcomingEventsRecyclerView();
        
        // Setup event highlights recycler view
        setupEventHighlightsRecyclerView();
        
        // Setup click listeners
        setupClickListeners();
        
        // Load events data
        loadEvents();
    }

    private void setWelcomeMessage() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            DatabaseReference userRef = mDatabase.child("users").child(auth.getCurrentUser().getUid());
            userRef.child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            String firstName = name.split(" ")[0];
                            tvWelcomeUser.setText(getString(R.string.home_greeting, firstName));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Use default greeting
                }
            });
        }
    }

    private void setupClickListeners() {
        fabRegisterEvent.setOnClickListener(v -> {
            // TODO: Open event registration activity
            Toast.makeText(getContext(), "Create Event feature coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnQuickAction.setOnClickListener(v -> {
            // TODO: Implement quick actions menu
            Toast.makeText(getContext(), "Quick Actions coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnViewAllEvents.setOnClickListener(v -> {
            // TODO: Open all events page
            Toast.makeText(getContext(), "View All Events coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnRefreshHighlights.setOnClickListener(v -> {
            // Refresh event highlights
            loadEvents();
        });
    }

    private void setupUpcomingEventsRecyclerView() {
        // Set horizontal layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvUpcomingEvents.setLayoutManager(layoutManager);
        
        // Create and set adapter
        upcomingEventsAdapter = new EventAdapter(getContext(), upcomingEventsList, this);
        rvUpcomingEvents.setAdapter(upcomingEventsAdapter);
    }

    private void setupEventHighlightsRecyclerView() {
        // Set vertical layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvEventHighlights.setLayoutManager(layoutManager);
        
        // Create and set adapter
        eventHighlightsAdapter = new EventHighlightAdapter(getContext(), highlightEventsList, this);
        rvEventHighlights.setAdapter(eventHighlightsAdapter);
    }
    
    private void loadEvents() {
        // Show progress
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        
        // Get current time
        final long currentTime = new Date().getTime();
        
        // Query for upcoming events (events that have not yet started)
        Query upcomingEventsQuery = mDatabase.child("events")
                .orderByChild("startDate")
                .startAt(currentTime)
                .limitToFirst(10);
        
        upcomingEventsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                upcomingEventsList.clear();
                
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        // Set the ID from the key
                        event.setId(eventSnapshot.getKey());
                        upcomingEventsList.add(event);
                    }
                }
                
                // Notify adapter
                upcomingEventsAdapter.notifyDataSetChanged();
                
                // Hide progress
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error
                // Hide progress
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Query for event highlights (popular or featured events)
        Query highlightsQuery = mDatabase.child("events")
                .orderByChild("featured")
                .equalTo(true)
                .limitToFirst(5);
        
        highlightsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                highlightEventsList.clear();
                
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        // Set the ID from the key
                        event.setId(eventSnapshot.getKey());
                        highlightEventsList.add(event);
                    }
                }
                
                // If no featured events, get most recent events instead
                if (highlightEventsList.isEmpty()) {
                    loadRecentEvents();
                } else {
                    // Notify adapter
                    eventHighlightsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error
                loadRecentEvents(); // Fallback to recent events
            }
        });
    }
    
    private void loadRecentEvents() {
        // Query for recent events as a fallback
        Query recentEventsQuery = mDatabase.child("events")
                .orderByChild("createdAt")
                .limitToLast(5);
        
        recentEventsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                highlightEventsList.clear();
                
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event event = eventSnapshot.getValue(Event.class);
                    if (event != null) {
                        // Set the ID from the key
                        event.setId(eventSnapshot.getKey());
                        highlightEventsList.add(event);
                    }
                }
                
                // Reverse to get newest first
                Collections.reverse(highlightEventsList);
                
                // Notify adapter
                eventHighlightsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error
                Toast.makeText(getContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEventClick(Event event, int position) {
        // Navigate to event details
        Intent intent = new Intent(getActivity(), EventDetailActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }

    @Override
    public void onEventHighlightClick(Event event, int position) {
        // Navigate to event details
        Intent intent = new Intent(getActivity(), EventDetailActivity.class);
        intent.putExtra("eventId", event.getId());
        startActivity(intent);
    }
} 
