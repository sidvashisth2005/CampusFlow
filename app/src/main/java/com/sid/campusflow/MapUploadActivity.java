package com.sid.campusflow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.models.CampusMap;
import com.sid.campusflow.models.User;
import com.sid.campusflow.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUploadActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Toolbar toolbar;
    private TextInputLayout tilTitle, tilDescription;
    private EditText etTitle, etDescription;
    private Button btnSelectImage, btnUpload;
    private ImageView ivMapPreview;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddLocation;
    private RecyclerView rvLocations;

    private Uri imageUri;
    private List<CampusMap.MapLocation> locationList;
    private LocationAdapter locationAdapter;
    
    // Define the ActivityResultLauncher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is admin, if not, finish activity
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in as an administrator", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Verify admin status
        FirebaseUtils.getCurrentUserData(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().toObject(User.class);
                if (user == null || !"admin".equals(user.getDesignation())) {
                    Toast.makeText(this, "Only administrators can upload maps", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to verify admin status", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        // Initialize the ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    Picasso.get().load(imageUri).into(ivMapPreview);
                    ivMapPreview.setVisibility(View.VISIBLE);
                }
            }
        );
        
        setContentView(R.layout.activity_map_upload);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload Campus Map");

        // Initialize views
        tilTitle = findViewById(R.id.til_title);
        tilDescription = findViewById(R.id.til_description);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUpload = findViewById(R.id.btn_upload);
        ivMapPreview = findViewById(R.id.iv_map_preview);
        progressBar = findViewById(R.id.progress_bar);
        fabAddLocation = findViewById(R.id.fab_add_location);
        rvLocations = findViewById(R.id.rv_locations);

        // Initialize location list
        locationList = new ArrayList<>();
        
        // Setup RecyclerView
        rvLocations.setLayoutManager(new LinearLayoutManager(this));
        locationAdapter = new LocationAdapter(locationList);
        rvLocations.setAdapter(locationAdapter);

        // Setup click listeners
        btnSelectImage.setOnClickListener(v -> openFileChooser());
        
        fabAddLocation.setOnClickListener(v -> showAddLocationDialog());
        
        btnUpload.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadMap();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Map Image"));
    }

    private void showAddLocationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);
        
        EditText etLocationName = dialogView.findViewById(R.id.et_location_name);
        EditText etLocationDescription = dialogView.findViewById(R.id.et_location_description);
        EditText etXCoordinate = dialogView.findViewById(R.id.et_x_coordinate);
        EditText etYCoordinate = dialogView.findViewById(R.id.et_y_coordinate);
        EditText etBuildingCode = dialogView.findViewById(R.id.et_building_code);
        EditText etLocationType = dialogView.findViewById(R.id.et_location_type);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Add Location")
            .setView(dialogView)
            .setPositiveButton("Add", (d, which) -> {
                // Validate inputs
                String name = etLocationName.getText().toString().trim();
                String description = etLocationDescription.getText().toString().trim();
                String xCoord = etXCoordinate.getText().toString().trim();
                String yCoord = etYCoordinate.getText().toString().trim();
                String buildingCode = etBuildingCode.getText().toString().trim();
                String locationType = etLocationType.getText().toString().trim();
                
                if (name.isEmpty() || xCoord.isEmpty() || yCoord.isEmpty()) {
                    Toast.makeText(this, "Name and coordinates are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                float x, y;
                try {
                    x = Float.parseFloat(xCoord);
                    y = Float.parseFloat(yCoord);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Coordinates must be valid numbers", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Create and add location to list
                CampusMap.MapLocation location = new CampusMap.MapLocation(
                    name, description, x, y, buildingCode, locationType);
                
                locationList.add(location);
                locationAdapter.notifyDataSetChanged();
            })
            .setNegativeButton("Cancel", null)
            .create();
            
        dialog.show();
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            isValid = false;
        } else {
            tilTitle.setError(null);
        }
        
        if (imageUri == null) {
            Toast.makeText(this, "Please select a map image", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }

    private void uploadMap() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        // Get current user
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to upload a map", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            return;
        }

        // Upload image first
        FirebaseUtils.uploadMapImage(imageUri, task -> {
            if (task.isSuccessful()) {
                // Get download URL
                StorageReference fileRef = task.getResult().getStorage();
                FirebaseUtils.getMapImageUrl(fileRef, urlTask -> {
                    if (urlTask.isSuccessful()) {
                        // Create map object with image URL
                        String imageUrl = urlTask.getResult().toString();
                        createAndUploadMap(imageUrl, currentUser.getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnUpload.setEnabled(true);
                        Toast.makeText(MapUploadActivity.this, 
                                      "Failed to get image URL: " + urlTask.getException().getMessage(), 
                                      Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                btnUpload.setEnabled(true);
                Toast.makeText(this, 
                              "Failed to upload image: " + task.getException().getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createAndUploadMap(String imageUrl, String userId) {
        // Create map object
        CampusMap campusMap = new CampusMap();
        campusMap.setTitle(etTitle.getText().toString().trim());
        campusMap.setDescription(etDescription.getText().toString().trim());
        campusMap.setMapImageUrl(imageUrl);
        campusMap.setLastUpdated(new Date());
        campusMap.setUploadedBy(userId);
        
        // Create locations map
        Map<String, CampusMap.MapLocation> locationsMap = new HashMap<>();
        for (int i = 0; i < locationList.size(); i++) {
            CampusMap.MapLocation location = locationList.get(i);
            locationsMap.put("location_" + i, location);
        }
        campusMap.setLocations(locationsMap);

        // Upload to Firestore
        FirebaseUtils.uploadCampusMap(campusMap, task -> {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            
            if (task.isSuccessful()) {
                DocumentReference documentReference = task.getResult();
                Toast.makeText(this, "Map uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, 
                              "Failed to upload map: " + task.getException().getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Adapter for locations
    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
        private List<CampusMap.MapLocation> locations;
        
        public LocationAdapter(List<CampusMap.MapLocation> locations) {
            this.locations = locations;
        }
        
        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
            CampusMap.MapLocation location = locations.get(position);
            holder.tvLocationName.setText(location.getName());
            holder.tvLocationDetails.setText(String.format("Type: %s, Building: %s", 
                    location.getLocationType(), location.getBuildingCode()));
            
            holder.btnDelete.setOnClickListener(v -> {
                locations.remove(position);
                notifyDataSetChanged();
            });
        }
        
        @Override
        public int getItemCount() {
            return locations.size();
        }
        
        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView tvLocationName, tvLocationDetails;
            ImageButton btnDelete;
            
            public LocationViewHolder(@NonNull View itemView) {
                super(itemView);
                tvLocationName = itemView.findViewById(R.id.tv_location_name);
                tvLocationDetails = itemView.findViewById(R.id.tv_location_details);
                btnDelete = itemView.findViewById(R.id.btn_delete_location);
            }
        }
    }
} 