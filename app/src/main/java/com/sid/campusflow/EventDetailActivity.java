package com.sid.campusflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.sid.campusflow.models.Event;
import com.sid.campusflow.models.User;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    // UI elements
    private ImageView ivEventImage;
    private TextView tvEventTitle;
    private TextView tvOrganizerName;
    private TextView tvEventDate;
    private TextView tvEventTime;
    private TextView tvEventLocation;
    private TextView tvEventAttendees;
    private TextView tvEventDescription;
    private TextView tvRegistrationStatus;
    private TextView tvApprovalStatus;
    private Button btnRegister;
    private TextView tvRegistrationClosed;
    private FloatingActionButton fabShare;
    
    // Data
    private Event event;
    private String eventId;
    private User organizer;
    private boolean isRegistered = false;
    private boolean isPending = false;
    private int attendeeCount = 0;
    
    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    
    // Date formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        
        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        
        // Get event ID from intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize UI elements
        initializeViews();
        
        // Setup toolbar
        setupToolbar();
        
        // Load event data
        loadEventData();
        
        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        ivEventImage = findViewById(R.id.iv_event_image);
        tvEventTitle = findViewById(R.id.tv_event_title);
        tvOrganizerName = findViewById(R.id.tv_organizer_name);
        tvEventDate = findViewById(R.id.tv_event_date);
        tvEventTime = findViewById(R.id.tv_event_time);
        tvEventLocation = findViewById(R.id.tv_event_location);
        tvEventAttendees = findViewById(R.id.tv_event_attendees);
        tvEventDescription = findViewById(R.id.tv_event_description);
        tvRegistrationStatus = findViewById(R.id.tv_registration_status);
        tvApprovalStatus = findViewById(R.id.tv_approval_status);
        btnRegister = findViewById(R.id.btn_register);
        tvRegistrationClosed = findViewById(R.id.tv_registration_closed);
        fabShare = findViewById(R.id.fab_share);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this, android.R.color.transparent));
    }

    private void loadEventData() {
        // Show loading state
        // TODO: Add a progress indicator
        
        // Fetch event data from Firebase
        mDatabase.child("events").child(eventId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                if (event != null) {
                    // Populate UI with event data
                    displayEventData();
                    // Fetch organizer data
                    loadOrganizerData(event.getOrganizerId());
                    // Check registration status for current user
                    checkRegistrationStatus();
                    // Get attendee count
                    countAttendees();
                } else {
                    Toast.makeText(EventDetailActivity.this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EventDetailActivity.this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void loadOrganizerData(String organizerId) {
        mDatabase.child("users").child(organizerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                organizer = dataSnapshot.getValue(User.class);
                if (organizer != null) {
                    tvOrganizerName.setText(organizer.getFullName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Continue without organizer data
            }
        });
    }
    
    private void displayEventData() {
        // Set event details
        tvEventTitle.setText(event.getTitle());
        tvEventDescription.setText(event.getDescription());
        tvEventLocation.setText(event.getLocation());
        
        // Set dates and times
        Date startDate = event.getStartDate();
        Date endDate = event.getEndDate();
        
        if (startDate != null) {
            tvEventDate.setText(dateFormat.format(startDate));
            
            if (endDate != null && !isSameDay(startDate, endDate)) {
                // Multi-day event
                String dateRange = dateFormat.format(startDate) + " - " + dateFormat.format(endDate);
                tvEventDate.setText(dateRange);
                
                String timeRange = timeFormat.format(startDate) + " - " + timeFormat.format(endDate);
                tvEventTime.setText(timeRange);
            } else {
                // Single day event
                String timeRange;
                if (endDate != null) {
                    timeRange = timeFormat.format(startDate) + " - " + timeFormat.format(endDate);
                } else {
                    timeRange = timeFormat.format(startDate);
                }
                tvEventTime.setText(timeRange);
            }
        }
        
        // Load image with Picasso
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .into(ivEventImage);
        } else {
            ivEventImage.setImageResource(R.drawable.placeholder_event);
        }
        
        // Show or hide registration required status
        if (event.isRequiresRegistration()) {
            tvRegistrationStatus.setVisibility(View.VISIBLE);
            tvRegistrationStatus.setText(R.string.registration_required);
        } else {
            tvRegistrationStatus.setVisibility(View.GONE);
        }
        
        // Show or hide approval required status
        if (event.isRequiresApproval()) {
            tvApprovalStatus.setVisibility(View.VISIBLE);
            tvApprovalStatus.setText(R.string.approval_required);
        } else {
            tvApprovalStatus.setVisibility(View.GONE);
        }
        
        // Check if event is expired
        boolean isEventExpired = false;
        if (startDate != null) {
            Date now = new Date();
            isEventExpired = now.after(startDate);
        }
        
        // Update UI based on expired state
        if (isEventExpired) {
            btnRegister.setVisibility(View.GONE);
            tvRegistrationClosed.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setVisibility(View.VISIBLE);
            tvRegistrationClosed.setVisibility(View.GONE);
        }
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(date1).equals(fmt.format(date2));
    }
    
    private void checkRegistrationStatus() {
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        mDatabase.child("event_registrations").child(eventId).child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User is registered
                            isRegistered = true;
                            
                            // Check approval status if necessary
                            if (event.isRequiresApproval()) {
                                Boolean isApproved = dataSnapshot.child("approved").getValue(Boolean.class);
                                if (isApproved != null && isApproved) {
                                    // Registration is approved
                                    btnRegister.setText(R.string.unregister_from_event);
                                    isPending = false;
                                } else {
                                    // Registration is pending approval
                                    btnRegister.setText(R.string.unregister_from_event);
                                    isPending = true;
                                }
                            } else {
                                // No approval needed
                                btnRegister.setText(R.string.unregister_from_event);
                                isPending = false;
                            }
                        } else {
                            // User is not registered
                            isRegistered = false;
                            isPending = false;
                            btnRegister.setText(R.string.register_now);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Continue with default state
                    }
                });
    }
    
    private void countAttendees() {
        mDatabase.child("event_registrations").child(eventId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        attendeeCount = (int) dataSnapshot.getChildrenCount();
                        int maxAttendees = event.getMaxAttendees();
                        
                        // Update attendee count display
                        String attendeeDisplay;
                        if (maxAttendees > 0) {
                            attendeeDisplay = attendeeCount + " / " + maxAttendees + " registered";
                        } else {
                            attendeeDisplay = attendeeCount + " registered";
                        }
                        tvEventAttendees.setText(attendeeDisplay);
                        
                        // Check if event is full
                        if (maxAttendees > 0 && attendeeCount >= maxAttendees && !isRegistered) {
                            btnRegister.setEnabled(false);
                            btnRegister.setText(R.string.registration_closed);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Continue with default state
                    }
                });
    }
    
    private void setupClickListeners() {
        // Register/unregister button click listener
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    // Redirect to login
                    Intent loginIntent = new Intent(EventDetailActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    return;
                }
                
                if (isRegistered) {
                    unregisterFromEvent();
                } else {
                    registerForEvent();
                }
            }
        });
        
        // Share FAB click listener
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareEvent();
            }
        });
    }
    
    private void registerForEvent() {
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        // Check if event is full
        if (event.getMaxAttendees() > 0 && attendeeCount >= event.getMaxAttendees()) {
            Toast.makeText(this, R.string.error_max_attendees_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create registration data
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("userId", userId);
        registrationData.put("timestamp", new Date().getTime());
        
        // Set approval status if required
        if (event.isRequiresApproval()) {
            registrationData.put("approved", false);
        } else {
            registrationData.put("approved", true);
        }
        
        // Save to Firebase
        mDatabase.child("event_registrations").child(eventId).child(userId)
                .setValue(registrationData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventDetailActivity.this, R.string.success_event_registered, Toast.LENGTH_SHORT).show();
                    
                    // Update button state (will be handled by the listener)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailActivity.this, R.string.error_event_registration_failed, Toast.LENGTH_SHORT).show();
                });
    }
    
    private void unregisterFromEvent() {
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        // Remove from Firebase
        mDatabase.child("event_registrations").child(eventId).child(userId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventDetailActivity.this, R.string.success_event_unregistered, Toast.LENGTH_SHORT).show();
                    
                    // Update button state (will be handled by the listener)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailActivity.this, R.string.error_unknown, Toast.LENGTH_SHORT).show();
                });
    }
    
    private void shareEvent() {
        if (event == null) return;
        
        String shareMessage = getString(R.string.app_name) + ": " + event.getTitle() + 
                "\n\n" + event.getDescription() +
                "\n\nLocation: " + event.getLocation() +
                "\nDate: " + tvEventDate.getText() +
                "\nTime: " + tvEventTime.getText();
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, event.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_event)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 
