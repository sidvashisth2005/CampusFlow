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
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventHighlightAdapter extends RecyclerView.Adapter<EventHighlightAdapter.EventHighlightViewHolder> {

    private final List<Event> eventList;
    private final Context context;
    private final OnEventHighlightClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnEventHighlightClickListener {
        void onEventHighlightClick(Event event, int position);
    }

    public EventHighlightAdapter(Context context, List<Event> eventList, OnEventHighlightClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventHighlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_highlight, parent, false);
        return new EventHighlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventHighlightViewHolder holder, int position) {
        Event event = eventList.get(position);
        
        holder.tvHighlightTitle.setText(event.getTitle());
        holder.tvHighlightDescription.setText(event.getDescription());
        
        // Format and set date
        if (event.getStartDate() != null) {
            holder.tvHighlightDate.setText(dateFormat.format(event.getStartDate()));
        } else {
            holder.tvHighlightDate.setVisibility(View.GONE);
        }
        
        // Load image using Picasso
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_event)
                    .error(R.drawable.placeholder_event)
                    .into(holder.ivHighlightImage);
        } else {
            holder.ivHighlightImage.setImageResource(R.drawable.placeholder_event);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventHighlightViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHighlightImage;
        TextView tvHighlightTitle, tvHighlightDescription, tvHighlightDate;

        public EventHighlightViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivHighlightImage = itemView.findViewById(R.id.iv_highlight_image);
            tvHighlightTitle = itemView.findViewById(R.id.tv_highlight_title);
            tvHighlightDescription = itemView.findViewById(R.id.tv_highlight_description);
            tvHighlightDate = itemView.findViewById(R.id.tv_highlight_date);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEventHighlightClick(eventList.get(position), position);
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
