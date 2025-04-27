package com.sid.campusflow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.sid.campusflow.R;
import com.sid.campusflow.models.BookingRequest;
import com.sid.campusflow.models.Room;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.RequestViewHolder> {
    private List<BookingRequest> requests;
    private OnRequestActionListener actionListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private FirebaseFirestore db;

    public interface OnRequestActionListener {
        void onRequestAction(BookingRequest request, String action);
    }

    public BookingRequestAdapter(List<BookingRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.actionListener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        BookingRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView roomNameView;
        private TextView requesterNameView;
        private TextView requesterDesignationView;
        private TextView timeRangeView;
        private TextView purposeView;
        private Button approveButton;
        private Button rejectButton;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameView = itemView.findViewById(R.id.roomName);
            requesterNameView = itemView.findViewById(R.id.requesterName);
            requesterDesignationView = itemView.findViewById(R.id.requesterDesignation);
            timeRangeView = itemView.findViewById(R.id.timeRange);
            purposeView = itemView.findViewById(R.id.purpose);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        void bind(BookingRequest request) {
            // Fetch and display room nam e
            db.collection("rooms")
                    .document(request.getRoomId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Room room = documentSnapshot.toObject(Room.class);
                        if (room != null) {
                            roomNameView.setText(room.getName());
                        }
                    });

            requesterNameView.setText(request.getUserName());
            requesterDesignationView.setText(request.getUserDesignation());
            timeRangeView.setText(String.format("%s - %s", 
                    dateFormat.format(request.getStartTime()),
                    dateFormat.format(request.getEndTime())));
            purposeView.setText(request.getPurpose());

            approveButton.setOnClickListener(v -> 
                actionListener.onRequestAction(request, "APPROVE"));
            rejectButton.setOnClickListener(v -> 
                actionListener.onRequestAction(request, "REJECT"));
        }
    }
} 