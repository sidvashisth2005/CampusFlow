package com.sid.campusflow;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sid.campusflow.models.CampusMap;
import com.sid.campusflow.models.User;
import com.sid.campusflow.utils.FirebaseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MapViewActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner spinnerMaps;
    private ImageView ivMap;
    private TextView tvDescription, tvEmptyMaps;
    private ProgressBar progressBar;
    private FloatingActionButton fabUpload;

    private List<CampusMap> mapList;
    private List<String> mapTitles;

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
        ivMap = findViewById(R.id.iv_map);
        tvDescription = findViewById(R.id.tv_description);
        tvEmptyMaps = findViewById(R.id.tv_empty_maps);
        progressBar = findViewById(R.id.progress_bar);
        fabUpload = findViewById(R.id.fab_upload);

        // Initialize lists
        mapList = new ArrayList<>();
        mapTitles = new ArrayList<>();

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, mapTitles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaps.setAdapter(adapter);

        // Setup spinner listener
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

        // Setup upload button - restrict to admin users only
        fabUpload.setOnClickListener(v -> {
            // Check if user is authenticated and is an admin
            FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
            if (currentUser != null) {
                // Check if user is admin
                FirebaseUtils.getCurrentUserData(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && "admin".equals(user.getDesignation())) {
                            // User is admin, allow upload
                            Intent intent = new Intent(MapViewActivity.this, MapUploadActivity.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Only administrators can upload maps", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to verify admin status", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "You must be logged in as an administrator", Toast.LENGTH_SHORT).show();
            }
        });

        // Load maps
        loadMaps();
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
                    if (user != null && "admin".equals(user.getDesignation())) {
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
        
        // Reload maps when returning to this activity
        loadMaps();
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
                } else {
                    // Update spinner and show first map
                    ((ArrayAdapter) spinnerMaps.getAdapter()).notifyDataSetChanged();
                    spinnerMaps.setVisibility(View.VISIBLE);
                    spinnerMaps.setSelection(0);
                    displayMap(mapList.get(0));
                }
            } else {
                Toast.makeText(this, "Failed to load maps: " + task.getException().getMessage(), 
                              Toast.LENGTH_SHORT).show();
                tvEmptyMaps.setVisibility(View.VISIBLE);
            }
        });
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
                        Toast.makeText(MapViewActivity.this, 
                                      "Failed to load map image", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            ivMap.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 