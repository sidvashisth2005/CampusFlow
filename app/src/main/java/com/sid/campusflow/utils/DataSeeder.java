package com.sid.campusflow.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.sid.campusflow.models.Room;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DataSeeder {
    private static final String TAG = "DataSeeder";
    private final Context context;
    private final FirebaseFirestore db;

    public DataSeeder(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void seedRoomsIfNeeded() {
        // Check if rooms already exist
        db.collection("rooms")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No rooms found in database, seeding data...");
                        seedRooms();
                    } else {
                        Log.d(TAG, "Rooms already exist in database");
                    }
                })
                .addOnFailureListener(e -> 
                    Log.e(TAG, "Error checking rooms: " + e.getMessage()));
    }

    private void seedRooms() {
        try {
            // Read the JSON file from assets
            InputStream is = context.getAssets().open("seed_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            // Parse JSON
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            Type roomListType = new TypeToken<List<Room>>(){}.getType();
            List<Room> rooms = gson.fromJson(jsonObject.get("rooms"), roomListType);

            // Add rooms to Firestore
            for (Room room : rooms) {
                db.collection("rooms")
                        .document(room.getId())
                        .set(room)
                        .addOnSuccessListener(aVoid -> 
                            Log.d(TAG, "Room added: " + room.getName()))
                        .addOnFailureListener(e -> 
                            Log.e(TAG, "Error adding room: " + e.getMessage()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading seed data: " + e.getMessage());
        }
    }
} 