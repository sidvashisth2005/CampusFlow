package com.sid.campusflow.utils;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sid.campusflow.models.Approval;
import com.sid.campusflow.models.Event;
import com.sid.campusflow.models.Notice;
import com.sid.campusflow.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

public class FirebaseUtils {
    private static final String TAG = "FirebaseUtils";
    
    // Collection names
    private static final String USERS_COLLECTION = "users";
    private static final String EVENTS_COLLECTION = "events";
    private static final String NOTICES_COLLECTION = "notices";
    private static final String APPROVALS_COLLECTION = "approvals";
    private static final String ATTENDEES_COLLECTION = "attendees";
    private static final String TIMETABLES_COLLECTION = "timetables";
    private static final String MAPS_COLLECTION = "maps";
    private static final String ROOMS_COLLECTION = "rooms";
    
    // Storage paths
    private static final String PROFILE_IMAGES_PATH = "profile_images";
    private static final String EVENT_IMAGES_PATH = "event_images";
    private static final String NOTICE_ATTACHMENTS_PATH = "notice_attachments";
    private static final String MAP_IMAGES_PATH = "map_images";
    
    // Firebase instances
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    
    // User methods
    
    /**
     * Get current user from Firebase Auth
     */
    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    
    /**
     * Get current user data from Firestore
     */
    public static void getCurrentUserData(OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        db.collection(USERS_COLLECTION)
            .document(currentUser.getUid())
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Create or update user in Firestore
     */
    public static void saveUserData(User user, OnCompleteListener<Void> listener) {
        db.collection(USERS_COLLECTION)
            .document(user.getUserId())
            .set(user)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Upload user profile image
     */
    public static void uploadProfileImage(Uri imageUri, OnCompleteListener<UploadTask.TaskSnapshot> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        String filename = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference()
                .child(PROFILE_IMAGES_PATH)
                .child(currentUser.getUid())
                .child(filename);
                
        fileRef.putFile(imageUri).addOnCompleteListener(listener);
    }
    
    // Event methods
    
    /**
     * Get all events
     */
    public static void getAllEvents(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(EVENTS_COLLECTION)
            .orderBy("startDate", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get events organized by current user
     */
    public static void getUserEvents(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        db.collection(EVENTS_COLLECTION)
            .whereEqualTo("organizerId", currentUser.getUid())
            .orderBy("startDate", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Create new event
     */
    public static void createEvent(Event event, OnCompleteListener<DocumentReference> listener) {
        db.collection(EVENTS_COLLECTION)
            .add(event)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Update existing event
     */
    public static void updateEvent(Event event, OnCompleteListener<Void> listener) {
        db.collection(EVENTS_COLLECTION)
            .document(event.getId())
            .set(event)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Register user for event
     */
    public static void registerForEvent(String eventId, OnCompleteListener<Void> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        Map<String, Object> attendeeData = new HashMap<>();
        attendeeData.put("userId", currentUser.getUid());
        attendeeData.put("registrationDate", new Date());
        attendeeData.put("status", "registered");
        
        db.collection(EVENTS_COLLECTION)
            .document(eventId)
            .collection(ATTENDEES_COLLECTION)
            .document(currentUser.getUid())
            .set(attendeeData)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Unregister user from event
     */
    public static void unregisterFromEvent(String eventId, OnCompleteListener<Void> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        db.collection(EVENTS_COLLECTION)
            .document(eventId)
            .collection(ATTENDEES_COLLECTION)
            .document(currentUser.getUid())
            .delete()
            .addOnCompleteListener(listener);
    }
    
    // Notice methods
    
    /**
     * Get all notices
     */
    public static void getAllNotices(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(NOTICES_COLLECTION)
            .orderBy("publishDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Create new notice
     */
    public static void createNotice(Notice notice, OnCompleteListener<DocumentReference> listener) {
        db.collection(NOTICES_COLLECTION)
            .add(notice)
            .addOnCompleteListener(listener);
    }
    
    // Approval methods
    
    /**
     * Submit approval request
     */
    public static void submitApproval(Approval approval, OnCompleteListener<DocumentReference> listener) {
        db.collection(APPROVALS_COLLECTION)
            .add(approval)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get user approvals
     */
    public static void getUserApprovals(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            return;
        }
        
        db.collection(APPROVALS_COLLECTION)
            .whereEqualTo("userId", currentUser.getUid())
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Update approval status
     */
    public static void updateApprovalStatus(String approvalId, String status, 
                                           String responderId, OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("responderId", responderId);
        updates.put("responseDate", new Date());
        
        db.collection(APPROVALS_COLLECTION)
            .document(approvalId)
            .update(updates)
            .addOnCompleteListener(listener);
    }
    
    // TimeTable methods
    
    /**
     * Upload a new timetable to Firestore
     */
    public static void uploadTimeTable(com.sid.campusflow.models.TimeTable timeTable, OnCompleteListener<DocumentReference> listener) {
        db.collection(TIMETABLES_COLLECTION)
            .add(timeTable)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get all timetables
     */
    public static void getAllTimeTables(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(TIMETABLES_COLLECTION)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get timetables filtered by year
     */
    public static void getTimeTablesByYear(String year, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(TIMETABLES_COLLECTION)
            .whereEqualTo("year", year)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Update an existing timetable
     */
    public static void updateTimeTable(com.sid.campusflow.models.TimeTable timeTable, OnCompleteListener<Void> listener) {
        db.collection(TIMETABLES_COLLECTION)
            .document(timeTable.getId())
            .set(timeTable)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Delete a timetable
     */
    public static void deleteTimeTable(String timeTableId, OnCompleteListener<Void> listener) {
        db.collection(TIMETABLES_COLLECTION)
            .document(timeTableId)
            .delete()
            .addOnCompleteListener(listener);
    }
    
    // Map methods
    
    /**
     * Upload a new campus map to Firestore
     */
    public static void uploadCampusMap(com.sid.campusflow.models.CampusMap campusMap, OnCompleteListener<DocumentReference> listener) {
        db.collection(MAPS_COLLECTION)
            .add(campusMap)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Upload a map image to Firebase Storage
     */
    public static void uploadMapImage(Uri imageUri, OnCompleteListener<UploadTask.TaskSnapshot> listener) {
        String filename = UUID.randomUUID().toString();
        StorageReference fileRef = storage.getReference()
                .child(MAP_IMAGES_PATH)
                .child(filename);
                
        fileRef.putFile(imageUri).addOnCompleteListener(listener);
    }
    
    /**
     * Get map image download URL after upload
     */
    public static void getMapImageUrl(StorageReference fileRef, OnCompleteListener<Uri> listener) {
        fileRef.getDownloadUrl().addOnCompleteListener(listener);
    }
    
    /**
     * Get all campus maps
     */
    public static void getAllCampusMaps(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(MAPS_COLLECTION)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Update an existing campus map
     */
    public static void updateCampusMap(com.sid.campusflow.models.CampusMap campusMap, OnCompleteListener<Void> listener) {
        db.collection(MAPS_COLLECTION)
            .document(campusMap.getId())
            .set(campusMap)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Delete a campus map
     */
    public static void deleteCampusMap(String mapId, OnCompleteListener<Void> listener) {
        db.collection(MAPS_COLLECTION)
            .document(mapId)
            .delete()
            .addOnCompleteListener(listener);
    }
    
    // Map and room methods
    
    /**
     * Get all rooms
     */
    public static void getAllRooms(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(ROOMS_COLLECTION)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get rooms by type
     */
    public static void getRoomsByType(String roomType, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(ROOMS_COLLECTION)
            .whereEqualTo("type", roomType)
            .orderBy("name", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Get all room types (unique)
     */
    public static void getAllRoomTypes(OnCompleteListener<QuerySnapshot> listener) {
        db.collection(ROOMS_COLLECTION)
            .get()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Add a room
     */
    public static void addRoom(com.sid.campusflow.models.Room room, OnCompleteListener<DocumentReference> listener) {
        db.collection(ROOMS_COLLECTION)
            .add(room)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Update a room
     */
    public static void updateRoom(com.sid.campusflow.models.Room room, OnCompleteListener<Void> listener) {
        if (room.getId() == null) {
            return;
        }
        db.collection(ROOMS_COLLECTION)
            .document(room.getId())
            .set(room)
            .addOnCompleteListener(listener);
    }
    
    /**
     * Delete a room
     */
    public static void deleteRoom(String roomId, OnCompleteListener<Void> listener) {
        db.collection(ROOMS_COLLECTION)
            .document(roomId)
            .delete()
            .addOnCompleteListener(listener);
    }
    
    /**
     * Import rooms from JSON string
     */
    public static void importRoomsFromJson(List<com.sid.campusflow.models.Room> rooms, OnCompleteListener<Void> listener) {
        // Use a batch to add all rooms at once
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        
        for (com.sid.campusflow.models.Room room : rooms) {
            DocumentReference docRef = db.collection(ROOMS_COLLECTION).document();
            room.setId(docRef.getId());
            batch.set(docRef, room);
        }
        
        batch.commit().addOnCompleteListener(listener);
    }
} 