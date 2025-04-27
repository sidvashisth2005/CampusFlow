package com.sid.campusflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.adapters.RoomAdapter;
import com.sid.campusflow.models.CampusMap;
import com.sid.campusflow.models.Room;
import com.sid.campusflow.models.User;
import com.sid.campusflow.utils.FirebaseUtils;
import com.sid.campusflow.utils.JsonUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapViewActivity extends AppCompatActivity implements RoomAdapter.OnRoomClickListener {

    private static final int REQUEST_DATA_FILE = 1001;
    
    private Toolbar toolbar;
    private Spinner spinnerMaps;
    private Spinner spinnerRoomTypes;
    private ImageView ivMap;
    private TextView tvDescription, tvEmptyMaps;
    private ProgressBar progressBar;
    private FloatingActionButton fabUpload;
    private RecyclerView rvRooms;
    private TextView tvNoRooms;
    private TextView tvRoomTypeLabel;
    private TextView tvRoomListingsLabel;

    private List<CampusMap> mapList;
    private List<String> mapTitles;
    private List<String> roomTypes;
    private List<Room> roomList;
    private RoomAdapter roomAdapter;
    private Map<String, List<Room>> roomsByType = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Campus Maps");

        // Initialize views
        spinnerMaps = findViewById(R.id.spinner_maps);
        spinnerRoomTypes = findViewById(R.id.spinner_room_types);
        ivMap = findViewById(R.id.iv_map);
        tvDescription = findViewById(R.id.tv_description);
        tvEmptyMaps = findViewById(R.id.tv_empty_maps);
        progressBar = findViewById(R.id.progress_bar);
        fabUpload = findViewById(R.id.fab_upload);
        rvRooms = findViewById(R.id.rv_rooms);
        tvNoRooms = findViewById(R.id.tv_no_rooms);
        tvRoomTypeLabel = findViewById(R.id.tv_room_type_label);
        tvRoomListingsLabel = findViewById(R.id.tv_room_listings_label);

        // Initialize lists
        mapList = new ArrayList<>();
        mapTitles = new ArrayList<>();
        roomTypes = new ArrayList<>();
        roomList = new ArrayList<>();

        // Setup map spinner
        ArrayAdapter<String> mapAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, mapTitles);
        mapAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaps.setAdapter(mapAdapter);

        // Setup room type spinner
        ArrayAdapter<String> roomTypesAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, roomTypes);
        roomTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomTypes.setAdapter(roomTypesAdapter);

        // Setup room recycler view
        roomAdapter = new RoomAdapter(roomList, this);
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(roomAdapter);

        // Setup map spinner listener
        spinnerMaps.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mapList.isEmpty() && position < mapList.size()) {
                    displayMap(mapList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup room type spinner listener
        spinnerRoomTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!roomTypes.isEmpty() && position < roomTypes.size()) {
                    String selectedType = roomTypes.get(position);
                    // Try loading from Firestore first
                    if (roomsByType.containsKey(selectedType)) {
                        displayRoomsFromMap(selectedType);
                    } else {
                        loadRoomsByType(selectedType);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup upload button - restrict to admin users only
        fabUpload.setOnClickListener(v -> {
            // Check if user is authenticated and is an admin
            FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
            if (currentUser != null) {
                // Check if user is admin
                FirebaseUtils.getCurrentUserData(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && "admin".equalsIgnoreCase(user.getDesignation())) {
                            // User is admin, show options
                            showAdminOptions();
                        } else {
                            Toast.makeText(this, "Only administrators can upload maps and room data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to verify admin status", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "You must be logged in as an administrator", Toast.LENGTH_SHORT).show();
            }
        });

        // Load maps and room types
        loadMaps();
        loadRoomTypes();
    }

    private void showAdminOptions() {
        String[] options = {"Upload Map", "Import Room Data"};
        
        new AlertDialog.Builder(this)
                .setTitle("Admin Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Upload Map
                            Intent mapIntent = new Intent(MapViewActivity.this, MapUploadActivity.class);
                            startActivity(mapIntent);
                            break;
                        case 1: // Import Room Data
                            selectRoomDataFile();
                            break;
                    }
                })
                .show();
    }

    private void selectRoomDataFile() {
        // Use a more generic approach
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");  // Accept any file type
        
        // Add storage permissions check
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
                return;
            }
        }
        
        try {
            startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_DATA_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_DATA_FILE && resultCode == RESULT_OK && data != null) {
            Uri selectedFileUri = data.getData();
            
            if (selectedFileUri == null) {
                Toast.makeText(this, "Could not get file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Log the selected URI
            Log.d("MapViewActivity", "Selected file URI: " + selectedFileUri.toString());
            
            // Take read permission for this URI
            try {
                getContentResolver().takePersistableUriPermission(
                    selectedFileUri, 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception e) {
                Log.e("MapViewActivity", "Could not take persistable URI permission: " + e.getMessage());
            }
            
            String fileName = getFileNameFromUri(selectedFileUri);
            Log.d("MapViewActivity", "Filename: " + (fileName != null ? fileName : "unknown"));
            
            if (fileName != null) {
                if (fileName.toLowerCase().endsWith(".json")) {
                    // Handle JSON file
                    importRoomDataFromJson(selectedFileUri);
                } else if (fileName.toLowerCase().endsWith(".csv")) {
                    // Handle CSV file
                    importRoomDataFromCsv(selectedFileUri);
                } else {
                    // Try to infer type from content
                    determineFileTypeAndImport(selectedFileUri);
                }
            } else {
                // If filename can't be determined, try to infer type
                determineFileTypeAndImport(selectedFileUri);
            }
        }
    }
    
    private void determineFileTypeAndImport(Uri fileUri) {
        // Read the first few bytes to try to determine file type
        try {
            InputStream is = getContentResolver().openInputStream(fileUri);
            if (is == null) {
                Toast.makeText(this, "Could not open file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String firstLine = reader.readLine();
            reader.close();
            
            if (firstLine == null) {
                Toast.makeText(this, "File is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Check for JSON
            if (firstLine.trim().startsWith("{") || firstLine.trim().startsWith("[")) {
                importRoomDataFromJson(fileUri);
            }
            // Check for CSV (has commas and no JSON markers)
            else if (firstLine.contains(",") && !firstLine.contains("{") && !firstLine.contains("[")) {
                importRoomDataFromCsv(fileUri);
            }
            // Unknown format
            else {
                Toast.makeText(this, "Unsupported file format. Please use JSON or CSV file.", 
                              Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("MapViewActivity", "Error determining file type: " + e.getMessage());
            Toast.makeText(this, "Error reading file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Helper method to get file name from URI
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        
        if (scheme != null && scheme.equals("content")) {
            try {
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (columnIndex != -1) {
                        fileName = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.e("MapViewActivity", "Error getting filename: " + e.getMessage());
            }
        }
        
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        
        return fileName;
    }
    
    private void importRoomDataFromCsv(Uri fileUri) {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("MapViewActivity", "Selected CSV URI: " + fileUri.toString());
        
        new Thread(() -> {
            List<Room> rooms = new ArrayList<>();
            
            try {
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    throw new Exception("Could not open file stream");
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                CSVReader csvReader = new CSVReader(reader);
                
                // Read header (first line)
                String[] header = null;
                try {
                    header = csvReader.readNext();
                } catch (CsvValidationException e) {
                    throw new Exception("CSV validation error: " + e.getMessage());
                }
                
                if (header == null) {
                    throw new Exception("CSV file is empty or invalid");
                }
                
                // Map column indices
                int nameIndex = -1, typeIndex = -1, locationIndex = -1, buildingIndex = -1, 
                    floorIndex = -1, descIndex = -1, latIndex = -1, longIndex = -1;
                
                for (int i = 0; i < header.length; i++) {
                    String col = header[i].toLowerCase().trim();
                    if (col.contains("name") && !col.contains("building")) nameIndex = i;
                    else if (col.contains("type")) typeIndex = i;
                    else if (col.contains("location")) locationIndex = i;
                    else if (col.contains("building")) buildingIndex = i;
                    else if (col.contains("floor")) floorIndex = i;
                    else if (col.contains("desc")) descIndex = i;
                    else if (col.contains("lat")) latIndex = i;
                    else if (col.contains("long")) longIndex = i;
                }
                
                // Read rows
                String[] line;
                try {
                    while ((line = csvReader.readNext()) != null) {
                        try {
                            Room room = new Room();
                            
                            // Set room properties based on CSV columns
                            if (nameIndex >= 0 && nameIndex < line.length) 
                                room.setName(line[nameIndex]);
                            
                            if (typeIndex >= 0 && typeIndex < line.length) 
                                room.setType(line[typeIndex]);
                            
                            if (locationIndex >= 0 && locationIndex < line.length) 
                                room.setLocation(line[locationIndex]);
                            
                            if (buildingIndex >= 0 && buildingIndex < line.length) 
                                room.setBuildingName(line[buildingIndex]);
                            
                            if (floorIndex >= 0 && floorIndex < line.length) 
                                room.setFloor(line[floorIndex]);
                            
                            if (descIndex >= 0 && descIndex < line.length) 
                                room.setDescription(line[descIndex]);
                            
                            // Parse coordinates if present
                            try {
                                if (latIndex >= 0 && latIndex < line.length && !line[latIndex].isEmpty()) 
                                    room.setLatitude(Double.parseDouble(line[latIndex]));
                                
                                if (longIndex >= 0 && longIndex < line.length && !line[longIndex].isEmpty()) 
                                    room.setLongitude(Double.parseDouble(line[longIndex]));
                            } catch (NumberFormatException e) {
                                Log.e("MapViewActivity", "Error parsing coordinates: " + e.getMessage());
                            }
                            
                            // Only add room if it has at least a name and type
                            if (room.getName() != null && !room.getName().isEmpty() &&
                                room.getType() != null && !room.getType().isEmpty()) {
                                rooms.add(room);
                            }
                        } catch (Exception e) {
                            Log.e("MapViewActivity", "Error parsing CSV row: " + e.getMessage());
                        }
                    }
                } catch (CsvValidationException e) {
                    Log.e("MapViewActivity", "CSV validation error while reading rows: " + e.getMessage());
                }
                
                csvReader.close();
                reader.close();
                inputStream.close();
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (rooms.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MapViewActivity.this, 
                                      "No valid room data found in CSV file or wrong format", 
                                      Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    Log.d("MapViewActivity", "Parsed " + rooms.size() + " rooms from CSV");
                    Toast.makeText(MapViewActivity.this, "Importing " + rooms.size() + " rooms...", 
                                  Toast.LENGTH_SHORT).show();
                    
                    // Save to Firebase
                    FirebaseUtils.importRoomsFromJson(rooms, task -> {
                        progressBar.setVisibility(View.GONE);
                        
                        if (task.isSuccessful()) {
                            Toast.makeText(MapViewActivity.this, 
                                          "Successfully imported " + rooms.size() + " rooms", 
                                          Toast.LENGTH_SHORT).show();
                            loadRoomTypes(); // Reload room types
                        } else {
                            String errorMsg = task.getException() != null ? 
                                             task.getException().getMessage() : "Unknown error";
                            Log.e("MapViewActivity", "Import failed: " + errorMsg);
                            Toast.makeText(MapViewActivity.this, 
                                          "Failed to import room data: " + errorMsg, 
                                          Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                
            } catch (Exception e) {
                Log.e("MapViewActivity", "Error parsing CSV: " + e.getMessage());
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MapViewActivity.this, 
                                  "Error reading CSV file: " + e.getMessage(), 
                                  Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if user is admin and show/hide upload button
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser != null) {
            FirebaseUtils.getCurrentUserData(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    // Only show upload button for admin users
                    if (user != null && "admin".equalsIgnoreCase(user.getDesignation())) {
                        fabUpload.setVisibility(View.VISIBLE);
                    } else {
                        fabUpload.setVisibility(View.GONE);
                    }
                } else {
                    fabUpload.setVisibility(View.GONE);
                }
            });
        } else {
            fabUpload.setVisibility(View.GONE);
        }
        
        // Reload maps and room types when returning to this activity
        loadMaps();
        loadRoomTypes();
    }

    private void loadMaps() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyMaps.setVisibility(View.GONE);
        
        // Clear current lists
        mapList.clear();
        mapTitles.clear();
        
        // Get maps from Firestore
        FirebaseUtils.getAllCampusMaps(task -> {
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    CampusMap campusMap = document.toObject(CampusMap.class);
                    campusMap.setId(document.getId());
                    
                    mapList.add(campusMap);
                    mapTitles.add(campusMap.getTitle());
                }
                
                if (mapList.isEmpty()) {
                    // Show empty view
                    tvEmptyMaps.setVisibility(View.VISIBLE);
                    ivMap.setVisibility(View.GONE);
                    tvDescription.setVisibility(View.GONE);
                    spinnerMaps.setVisibility(View.GONE);
                    
                    // Hide room sections
                    hideRoomSections();
                } else {
                    // Update spinner and show first map
                    ((ArrayAdapter) spinnerMaps.getAdapter()).notifyDataSetChanged();
                    spinnerMaps.setVisibility(View.VISIBLE);
                    spinnerMaps.setSelection(0);
                    displayMap(mapList.get(0));
                    
                    // Show room sections if we have room types
                    if (!roomTypes.isEmpty()) {
                        showRoomSections();
                    }
                }
            } else {
                Toast.makeText(this, "Failed to load maps: " + task.getException().getMessage(), 
                               Toast.LENGTH_SHORT).show();
                tvEmptyMaps.setVisibility(View.VISIBLE);
                hideRoomSections();
            }
        });
    }

    private void loadRoomTypes() {
        progressBar.setVisibility(View.VISIBLE);
        
        // Clear current list
        roomTypes.clear();
        
        // First try to load from Firestore
        FirebaseUtils.getAllRoomTypes(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                // Extract unique room types
                List<String> uniqueTypes = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    String type = document.getString("type");
                    if (type != null && !uniqueTypes.contains(type)) {
                        uniqueTypes.add(type);
                    }
                }
                
                // Update the roomTypes list
                roomTypes.addAll(uniqueTypes);
                
                if (!roomTypes.isEmpty()) {
                    // Update adapter
                    ((ArrayAdapter) spinnerRoomTypes.getAdapter()).notifyDataSetChanged();
                    
                    // Show room sections if we have maps
                    if (!mapList.isEmpty()) {
                        showRoomSections();
                        
                        // Load rooms for first type
                        spinnerRoomTypes.setSelection(0);
                        loadRoomsByType(roomTypes.get(0));
                    }
                } else {
                    // If no room types in Firestore, try loading from assets
                    loadRoomsFromAssets();
                }
            } else {
                // If Firestore fails, try loading from assets
                loadRoomsFromAssets();
            }
            
            progressBar.setVisibility(View.GONE);
        });
    }

    private void loadRoomsFromAssets() {
        try {
            // Get rooms from asset file
            List<Room> assetRooms = JsonUtils.parseRoomsFromAsset(this, "sample_rooms.csv");
            
            if (assetRooms.isEmpty()) {
                hideRoomSections();
                Toast.makeText(this, "No rooms found in asset file", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Extract unique room types
            List<String> uniqueTypes = new ArrayList<>();
            for (Room room : assetRooms) {
                String type = room.getType();
                if (type != null && !uniqueTypes.contains(type)) {
                    uniqueTypes.add(type);
                }
            }
            
            roomTypes.addAll(uniqueTypes);
            
            if (!roomTypes.isEmpty()) {
                // Update adapter
                ((ArrayAdapter) spinnerRoomTypes.getAdapter()).notifyDataSetChanged();
                
                // Show room sections if we have maps
                if (!mapList.isEmpty()) {
                    showRoomSections();
                    
                    // Store rooms by type for quick access
                    for (String type : uniqueTypes) {
                        List<Room> roomsOfType = new ArrayList<>();
                        for (Room room : assetRooms) {
                            if (type.equals(room.getType())) {
                                roomsOfType.add(room);
                            }
                        }
                        roomsByType.put(type, roomsOfType);
                    }
                    
                    // Load rooms for first type
                    spinnerRoomTypes.setSelection(0);
                    displayRoomsFromMap(roomTypes.get(0));
                }
            } else {
                hideRoomSections();
            }
        } catch (Exception e) {
            Log.e("MapViewActivity", "Error loading rooms from assets: " + e.getMessage());
            hideRoomSections();
        }
    }

    private void loadRoomsByType(String roomType) {
        progressBar.setVisibility(View.VISIBLE);
        roomList.clear();
        
        FirebaseUtils.getRoomsByType(roomType, task -> {
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    Room room = document.toObject(Room.class);
                    if (room != null) {
                        room.setId(document.getId());
                        roomList.add(room);
                    }
                }
                
                roomAdapter.updateRooms(roomList);
                
                // Show appropriate view based on results
                if (roomList.isEmpty()) {
                    tvNoRooms.setVisibility(View.VISIBLE);
                    rvRooms.setVisibility(View.GONE);
                } else {
                    tvNoRooms.setVisibility(View.GONE);
                    rvRooms.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(this, "Failed to load rooms", Toast.LENGTH_SHORT).show();
                tvNoRooms.setVisibility(View.VISIBLE);
                rvRooms.setVisibility(View.GONE);
            }
        });
    }

    private void showRoomSections() {
        tvRoomTypeLabel.setVisibility(View.VISIBLE);
        spinnerRoomTypes.setVisibility(View.VISIBLE);
        tvRoomListingsLabel.setVisibility(View.VISIBLE);
    }

    private void hideRoomSections() {
        tvRoomTypeLabel.setVisibility(View.GONE);
        spinnerRoomTypes.setVisibility(View.GONE);
        tvRoomListingsLabel.setVisibility(View.GONE);
        rvRooms.setVisibility(View.GONE);
        tvNoRooms.setVisibility(View.GONE);
    }

    private void displayMap(CampusMap campusMap) {
        if (campusMap == null) {
            return;
        }
        
        tvDescription.setText(campusMap.getDescription());
        tvDescription.setVisibility(View.VISIBLE);
        
        // Load map image
        if (campusMap.getMapImageUrl() != null && !campusMap.getMapImageUrl().isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
            ivMap.setVisibility(View.VISIBLE);
            
            Picasso.get()
                .load(campusMap.getMapImageUrl())
                .into(ivMap, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MapViewActivity.this, "Failed to load map image", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            ivMap.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRoomClick(Room room) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(room.getName())
                .setMessage("Location: " + room.getLocation() + "\n\n" +
                        "Building: " + room.getBuildingName() + "\n" +
                        "Type: " + room.getType() + "\n" +
                        "Capacity: " + room.getCapacity())
                .setPositiveButton("OK", null)
                .show();
    }

    private void importRoomDataFromJson(Uri fileUri) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Log the URI to debug
        Log.d("MapViewActivity", "Selected JSON URI: " + fileUri.toString());
        
        List<Room> rooms = JsonUtils.parseRoomsFromUri(this, fileUri);
        
        if (rooms == null || rooms.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "No valid room data found in the file. Please check the JSON format.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d("MapViewActivity", "Parsed " + rooms.size() + " rooms from JSON");
        Toast.makeText(this, "Importing " + rooms.size() + " rooms...", Toast.LENGTH_SHORT).show();
        
        FirebaseUtils.importRoomsFromJson(rooms, task -> {
            progressBar.setVisibility(View.GONE);
            
            if (task.isSuccessful()) {
                Toast.makeText(this, "Successfully imported " + rooms.size() + " rooms", Toast.LENGTH_SHORT).show();
                loadRoomTypes(); // Reload room types
            } else {
                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                Log.e("MapViewActivity", "Import failed: " + errorMsg);
                Toast.makeText(this, "Failed to import room data: " + errorMsg, 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRoomsFromMap(String roomType) {
        List<Room> roomsToDisplay = roomsByType.get(roomType);
        
        if (roomsToDisplay != null && !roomsToDisplay.isEmpty()) {
            roomList.clear();
            roomList.addAll(roomsToDisplay);
            roomAdapter.updateRooms(roomList);
            
            tvNoRooms.setVisibility(View.GONE);
            rvRooms.setVisibility(View.VISIBLE);
        } else {
            tvNoRooms.setVisibility(View.VISIBLE);
            rvRooms.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, try again
                selectRoomDataFile();
            } else {
                Toast.makeText(this, "Storage permission is required to select files", Toast.LENGTH_LONG).show();
            }
        }
    }
} 