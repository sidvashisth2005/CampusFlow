package com.sid.campusflow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.R;
import com.sid.campusflow.models.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {
    private List<Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(Room room);
    }

    public RoomAdapter(List<Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = rooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    public void updateRooms(List<Room> newRooms) {
        this.rooms.clear();
        this.rooms.addAll(newRooms);
        notifyDataSetChanged();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private TextView roomName;
        private TextView roomType;
        private TextView capacity;
        private TextView building;
        private TextView floor;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.roomName);
            roomType = itemView.findViewById(R.id.roomType);
            capacity = itemView.findViewById(R.id.capacity);
            building = itemView.findViewById(R.id.building);
            floor = itemView.findViewById(R.id.floor);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRoomClick(rooms.get(position));
                }
            });
        }

        public void bind(Room room) {
            roomName.setText(room.getName());
            roomType.setText(room.getType());
            capacity.setText(String.format("Capacity: %d", room.getCapacity()));
            building.setText(room.getBuilding());
            floor.setText(String.format("Floor: %s", room.getFloor()));
        }
    }
} 