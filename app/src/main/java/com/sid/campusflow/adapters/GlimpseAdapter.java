package com.sid.campusflow.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sid.campusflow.MediaViewerActivity;
import com.sid.campusflow.R;
import com.sid.campusflow.models.EventGlimpse;
import com.sid.campusflow.utils.MediaUtils;

import java.util.List;

/**
 * Adapter for displaying event glimpses
 */
public class GlimpseAdapter extends RecyclerView.Adapter<GlimpseAdapter.GlimpseViewHolder> {

    private final Context context;
    private final List<EventGlimpse> glimpses;
    private final String eventId;

    public GlimpseAdapter(Context context, List<EventGlimpse> glimpses, String eventId) {
        this.context = context;
        this.glimpses = glimpses;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public GlimpseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_glimpse, parent, false);
        return new GlimpseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GlimpseViewHolder holder, int position) {
        EventGlimpse glimpse = glimpses.get(position);
        
        // Load the thumbnail (for both images and videos)
        Glide.with(context)
                .load(glimpse.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.ivGlimpse);
        
        // Show/hide play indicator based on media type
        holder.ivPlayIndicator.setVisibility(glimpse.isVideo() ? View.VISIBLE : View.GONE);
        
        // Set the media type icon
        holder.ivMediaType.setImageResource(
                glimpse.isVideo() ? R.drawable.ic_video : R.drawable.ic_image
        );
        
        // Set click listener to open media viewer
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MediaViewerActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("glimpse_position", position);
            intent.putExtra("media_url", glimpse.getMediaUrl());
            intent.putExtra("media_type", glimpse.getMediaType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return glimpses.size();
    }

    public void addGlimpse(EventGlimpse glimpse) {
        glimpses.add(glimpse);
        notifyItemInserted(glimpses.size() - 1);
    }

    static class GlimpseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGlimpse;
        ImageView ivPlayIndicator;
        ImageView ivMediaType;

        public GlimpseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGlimpse = itemView.findViewById(R.id.iv_glimpse);
            ivPlayIndicator = itemView.findViewById(R.id.iv_play_indicator);
            ivMediaType = itemView.findViewById(R.id.iv_media_type);
        }
    }
} 