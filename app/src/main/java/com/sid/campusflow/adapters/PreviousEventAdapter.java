package com.sid.campusflow.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sid.campusflow.EventDetailActivity;
import com.sid.campusflow.GlimpseUploadActivity;
import com.sid.campusflow.R;
import com.sid.campusflow.models.Event;
import com.sid.campusflow.models.EventGlimpse;
import com.sid.campusflow.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying previous events with their glimpses
 */
public class PreviousEventAdapter extends RecyclerView.Adapter<PreviousEventAdapter.PreviousEventViewHolder> {

    private final Context context;
    private final List<Event> previousEvents;
    private final int MAX_GLIMPSES = 10; // Maximum number of glimpses per event
    private FirebaseFirestore db;

    public PreviousEventAdapter(Context context, List<Event> previousEvents) {
        this.context = context;
        this.previousEvents = previousEvents;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PreviousEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_previous_event, parent, false);
        return new PreviousEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviousEventViewHolder holder, int position) {
        Event event = previousEvents.get(position);
        
        // Set event details
        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDescription.setText(event.getDescription());
        holder.tvEventDate.setText(DateUtils.formatDate(event.getEventDate()));
        
        // Load event banner
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop()
                    .into(holder.ivEventBanner);
        }
        
        // Load event glimpses
        loadEventGlimpses(holder, event.getId());
        
        // Set view event details click listener
        holder.btnViewEvent.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            context.startActivity(intent);
        });
        
        // Set add glimpses click listener (only for organizers and admins)
        holder.btnAddGlimpses.setOnClickListener(v -> {
            checkPermissionAndAddGlimpse(event);
        });
    }

    private void loadEventGlimpses(PreviousEventViewHolder holder, String eventId) {
        // Initialize glimpses list
        List<EventGlimpse> glimpses = new ArrayList<>();
        GlimpseAdapter adapter = new GlimpseAdapter(context, glimpses, eventId);
        holder.rvEventGlimpses.setAdapter(adapter);
        holder.rvEventGlimpses.setLayoutManager(
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        
        // Show loading UI
        holder.rvEventGlimpses.setVisibility(View.GONE);
        holder.llNoGlimpses.setVisibility(View.GONE);
        
        // Query for glimpses from Firestore
        db.collection("eventGlimpses")
                .whereEqualTo("eventId", eventId)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .limit(MAX_GLIMPSES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Add glimpses to the adapter
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            EventGlimpse glimpse = snapshot.toObject(EventGlimpse.class);
                            if (glimpse != null) {
                                glimpse.setId(snapshot.getId());
                                glimpses.add(glimpse);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        
                        // Show glimpses or no glimpses UI
                        if (glimpses.isEmpty()) {
                            holder.rvEventGlimpses.setVisibility(View.GONE);
                            holder.llNoGlimpses.setVisibility(View.VISIBLE);
                        } else {
                            holder.rvEventGlimpses.setVisibility(View.VISIBLE);
                            holder.llNoGlimpses.setVisibility(View.GONE);
                        }
                    } else {
                        // No glimpses found
                        holder.rvEventGlimpses.setVisibility(View.GONE);
                        holder.llNoGlimpses.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    // Error loading glimpses
                    holder.rvEventGlimpses.setVisibility(View.GONE);
                    holder.llNoGlimpses.setVisibility(View.VISIBLE);
                });
    }

    private void checkPermissionAndAddGlimpse(Event event) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showErrorDialog("You need to be logged in to add glimpses");
            return;
        }

        // Check if user is organizer or admin
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String designation = documentSnapshot.getString("designation");
                        boolean isOrganizer = event.getOrganizerId().equals(currentUser.getUid());
                        boolean isSecretary = "secretary".equalsIgnoreCase(designation);
                        boolean isAdmin = "admin".equalsIgnoreCase(designation);

                        if (isOrganizer || isSecretary || isAdmin) {
                            // User is authorized to add glimpses
                            launchGlimpseUploadActivity(event);
                        } else {
                            showErrorDialog("Only event organizers and admins can add glimpses");
                        }
                    } else {
                        showErrorDialog("Unable to verify permissions");
                    }
                })
                .addOnFailureListener(e -> showErrorDialog("Error checking permissions: " + e.getMessage()));
    }

    private void launchGlimpseUploadActivity(Event event) {
        // First check if max glimpses reached
        db.collection("eventGlimpses")
                .whereEqualTo("eventId", event.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() >= MAX_GLIMPSES) {
                        showErrorDialog(context.getString(R.string.glimpse_limit_reached));
                    } else {
                        // Launch glimpse upload activity
                        Intent intent = new Intent(context, GlimpseUploadActivity.class);
                        intent.putExtra("event_id", event.getId());
                        intent.putExtra("event_title", event.getTitle());
                        context.startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> showErrorDialog("Error: " + e.getMessage()));
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(context)
                .setTitle("Cannot Add Glimpse")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return previousEvents.size();
    }

    public void updateEvents(List<Event> newEvents) {
        previousEvents.clear();
        previousEvents.addAll(newEvents);
        notifyDataSetChanged();
    }

    static class PreviousEventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventBanner;
        TextView tvEventTitle;
        TextView tvEventDescription;
        TextView tvEventDate;
        RecyclerView rvEventGlimpses;
        LinearLayout llNoGlimpses;
        Button btnAddGlimpses;
        Button btnViewEvent;

        public PreviousEventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventBanner = itemView.findViewById(R.id.iv_event_banner);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDescription = itemView.findViewById(R.id.tv_event_description);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
            rvEventGlimpses = itemView.findViewById(R.id.rv_event_glimpses);
            llNoGlimpses = itemView.findViewById(R.id.ll_no_glimpses);
            btnAddGlimpses = itemView.findViewById(R.id.btn_add_glimpses);
            btnViewEvent = itemView.findViewById(R.id.btn_view_event);
        }
    }
} 