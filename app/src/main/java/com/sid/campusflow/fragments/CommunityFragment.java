package com.sid.campusflow.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sid.campusflow.utils.CloudinaryConfig;
import com.sid.campusflow.utils.CloudinaryConfig.CloudinaryUploadCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class CommunityFragment extends Fragment {

    private static final String TAG = "CommunityFragment";
    private EditText etSearch;
    private RecyclerView rvYourCommunities;
    private RecyclerView rvPopularCommunities;
    private RecyclerView rvRecentActivity;
    private FloatingActionButton fabCreateCommunity;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        // Initialize views
        etSearch = view.findViewById(R.id.et_search);
        rvYourCommunities = view.findViewById(R.id.rv_your_communities);
        rvPopularCommunities = view.findViewById(R.id.rv_popular_communities);
        rvRecentActivity = view.findViewById(R.id.rv_recent_activity);
        fabCreateCommunity = view.findViewById(R.id.fab_create_community);
        
        // Hide FAB by default until we check permissions
        fabCreateCommunity.setVisibility(View.GONE);
        
        // Setup recycler views
        setupYourCommunitiesRecyclerView();
        setupPopularCommunitiesRecyclerView();
        setupRecentActivityRecyclerView();
        
        // Setup search functionality
        setupSearch();
        
        // Check if user is authorized to create communities
        checkUserAuthorization();
        
        // Setup click listeners
        fabCreateCommunity.setOnClickListener(v -> createCommunity());
    }

    private void checkUserAuthorization() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String designation = document.getString("designation");
                            Log.d(TAG, "User designation: " + designation);
                            
                            // Show FAB only for secretaries and admins
                            if (designation != null && 
                                (designation.equalsIgnoreCase("secretary") || 
                                 designation.equalsIgnoreCase("admin"))) {
                                fabCreateCommunity.setVisibility(View.VISIBLE);
                            } else {
                                fabCreateCommunity.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to get user data", task.getException());
                        fabCreateCommunity.setVisibility(View.GONE);
                    }
                });
        } else {
            fabCreateCommunity.setVisibility(View.GONE);
        }
    }

    private void createCommunity() {
        // Create a dialog for community creation
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_community, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.create_community_title);
        
        // Initialize dialog views
        TextInputEditText etCommunityName = dialogView.findViewById(R.id.et_community_name);
        TextInputEditText etCommunityDescription = dialogView.findViewById(R.id.et_community_description);
        ImageView ivCommunityImage = dialogView.findViewById(R.id.iv_community_image);
        Button btnSelectImage = dialogView.findViewById(R.id.btn_select_image);
        Button btnCreate = dialogView.findViewById(R.id.btn_create_community);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);
        
        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Image selection intent result launcher
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                requireContext().getContentResolver(), selectedImageUri);
                            ivCommunityImage.setImageBitmap(bitmap);
                            ivCommunityImage.setTag(selectedImageUri);
                            ivCommunityImage.setVisibility(View.VISIBLE);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
        
        // Select image button click
        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
        
        // Create community button click
        btnCreate.setOnClickListener(v -> {
            String name = etCommunityName.getText().toString().trim();
            String description = etCommunityDescription.getText().toString().trim();
            Uri imageUri = (Uri) ivCommunityImage.getTag();
            
            // Validate inputs
            if (name.isEmpty()) {
                etCommunityName.setError(getString(R.string.error_name_required));
                return;
            }
            
            if (description.isEmpty() || description.split("\\s+").length < 20) {
                etCommunityDescription.setError(getString(R.string.error_description_minimum));
                return;
            }
            
            if (imageUri == null) {
                Toast.makeText(getContext(), R.string.error_image_required, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Show loading state
            progressBar.setVisibility(View.VISIBLE);
            btnCreate.setEnabled(false);
            
            // Upload image to Cloudinary instead of Firebase Storage
            uploadImageToCloudinary(imageUri, name.toLowerCase().replace(" ", "_"), new CloudinaryUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    // Create community with the Cloudinary image URL
                    createCommunityInFirestore(name, description, imageUrl, dialog, progressBar);
                }
                
                @Override
                public void onFailure(String error) {
                    progressBar.setVisibility(View.GONE);
                    btnCreate.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload image: " + error, Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onProgress(int progress) {
                    // Update progress if needed
                    Log.d(TAG, "Upload progress: " + progress + "%");
                }
            });
        });
    }
    
    private void uploadImageToCloudinary(Uri imageUri, String communityId, CloudinaryUploadCallback listener) {
        // Use the CloudinaryConfig utility to upload the image
        CloudinaryConfig.uploadImage(requireContext(), imageUri, "community_images", listener);
    }
    
    private void createCommunityInFirestore(String name, String description, String imageUrl, AlertDialog dialog, ProgressBar progressBar) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }
        
        // Create a community object
        Map<String, Object> community = new HashMap<>();
        community.put("name", name);
        community.put("description", description);
        community.put("imageUrl", imageUrl);
        community.put("createdBy", currentUser.getUid());
        community.put("createdAt", System.currentTimeMillis());
        community.put("memberCount", 1); // Creator is the first member
        
        // Add to Firestore
        db.collection("communities")
            .add(community)
            .addOnSuccessListener(documentReference -> {
                String communityId = documentReference.getId();
                
                // Add creator as a member
                Map<String, Object> membership = new HashMap<>();
                membership.put("userId", currentUser.getUid());
                membership.put("communityId", communityId);
                membership.put("role", "creator");
                membership.put("joinedAt", System.currentTimeMillis());
                
                db.collection("userCommunities")
                    .add(membership)
                    .addOnSuccessListener(memberRef -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), R.string.community_created_success, Toast.LENGTH_SHORT).show();
                        // TODO: Refresh the communities list
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.community_creation_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.community_creation_failed) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupYourCommunitiesRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        rvYourCommunities.setLayoutManager(layoutManager);
        
        // TODO: Create adapter for user's communities and fetch data
        // CommunitiesAdapter adapter = new CommunitiesAdapter(communitiesList, true);
        // rvYourCommunities.setAdapter(adapter);
    }

    private void setupPopularCommunitiesRecyclerView() {
        rvPopularCommunities.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // TODO: Create adapter for popular communities and fetch data
        // CommunitiesAdapter adapter = new CommunitiesAdapter(popularCommunitiesList, false);
        // rvPopularCommunities.setAdapter(adapter);
    }

    private void setupRecentActivityRecyclerView() {
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // TODO: Create adapter for community activity and fetch data
        // CommunityActivityAdapter adapter = new CommunityActivityAdapter(activityList);
        // rvRecentActivity.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Filter communities based on search text
                String searchText = s.toString().trim().toLowerCase();
                
                // TODO: Implement search functionality
                if (!searchText.isEmpty()) {
                    Toast.makeText(getContext(), "Searching for: " + searchText, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
} 
