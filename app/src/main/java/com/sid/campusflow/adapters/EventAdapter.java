package com.sid.campusflow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.R;
import com.sid.campusflow.models.Event;
import com.sid.campusflow.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final Context context;
    private final OnEventClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public interface OnEventClickListener {
        void onEventClick(Event event, int position);
    }

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        
        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDescription.setText(event.getDescription());
        holder.tvEventLocation.setText(event.getLocation());
        
        // Format start and end times and combine them
        String startTime = DateUtils.formatTime(event.getStartDate());
        String endTime = DateUtils.formatTime(event.getEndDate());
        String date = DateUtils.formatDate(event.getStartDate()); // Assuming start and end date are the same day
        String dateTimeRange = date + " • " + startTime + " - " + endTime;
        holder.tvEventTime.setText(dateTimeRange);
        
        // Load image using Picasso if image URL exists
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .into(holder.ivEventImage);
        } else {
            holder.ivEventImage.setImageResource(R.drawable.placeholder_event);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDescription, tvEventLocation;
        TextView tvEventTime;

        public EventViewHolder(@NonNull View itemView, OnEventClickListener listener) {
            super(itemView);
            
            ivEventImage = itemView.findViewById(R.id.iv_event_image);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDescription = itemView.findViewById(R.id.tv_event_description);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventClick(eventList.get(position), position);
                }
            });
        }
    }
    
    public void updateEventList(List<Event> newEventList) {
        this.eventList.clear();
        this.eventList.addAll(newEventList);
        notifyDataSetChanged();
    }
} 
