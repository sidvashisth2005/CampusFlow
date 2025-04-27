package com.sid.campusflow.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sid.campusflow.LoginActivity;
import com.sid.campusflow.R;
import com.sid.campusflow.adapters.ApprovalAdapter;
import com.sid.campusflow.models.Approval;
import com.sid.campusflow.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements ApprovalAdapter.OnApprovalActionListener {

    // UI components
    private CircleImageView ivProfileImage;
    private TextView tvFullName, tvDesignation, tvEmail;
    private TextView tvEventsCount, tvBookingsCount, tvCommunitiesCount;
    private RecyclerView rvPendingRequests, rvApprovals;
    private TextView tvNoPendingRequests, tvNoApprovals;
    private Button btnEditProfile, btnViewAllRequests, btnViewAllApprovals;
    private Toolbar toolbar;
    
    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    // Adapters
    private ApprovalAdapter pendingRequestsAdapter;
    private ApprovalAdapter approvalsAdapter;
    
    // Data
    private List<Approval> pendingRequests = new ArrayList<>();
    private List<Approval> approvals = new ArrayList<>();
    private User userProfile;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        Log.d("ProfileFragment", "Firebase Auth initialized in onCreate: " + (auth != null));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firebase
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
            Log.d("ProfileFragment", "Firebase Auth initialized in onViewCreated");
        }
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        Log.d("ProfileFragment", "Current user in onViewCreated: " + (currentUser != null ? currentUser.getEmail() : "null"));
        
        // Initialize UI components
        initializeViews(view);
        
        // Setup toolbar
        setupToolbar();
        
        // Setup adapters
        setupAdapters();
        
        // Load user data
        loadUserProfile();
        
        // Load requests and approvals
        loadPendingRequests();
        loadApprovals();
        
        // Setup click listeners
        setupClickListeners();
    }
    
    private void setupToolbar() {
        toolbar = getView().findViewById(R.id.profile_toolbar);
        // Set up toolbar with activity's action bar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("Profile");
    }
    
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Log.d("ProfileFragment", "Menu item clicked: " + itemId);
        
        if (itemId == R.id.action_edit_profile) {
            Log.d("ProfileFragment", "Edit profile menu option selected");
            // Handle edit profile
            return true;
        } else if (itemId == R.id.action_settings) {
            Log.d("ProfileFragment", "Settings menu option selected");
            // Handle settings
            return true;
        } else if (itemId == R.id.action_logout) {
            Log.d("ProfileFragment", "Logout menu option selected");
            showLogoutConfirmationDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void showLogoutConfirmationDialog() {
        Log.d("ProfileFragment", "Showing logout confirmation dialog");
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.dialog_confirm_logout)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                    Log.d("ProfileFragment", "User confirmed logout");
                    performLogout();
                })
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> {
                    Log.d("ProfileFragment", "User cancelled logout");
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
    
    private void performLogout() {
        // Add logging before logout
        Log.d("ProfileFragment", "Attempting to log out user: " + (auth != null ? "Auth initialized" : "Auth is NULL"));
        if (auth != null && auth.getCurrentUser() != null) {
            Log.d("ProfileFragment", "Current user before logout: " + auth.getCurrentUser().getEmail());
        }
        
        try {
            // Sign out from Firebase Auth
            auth.signOut();
            Log.d("ProfileFragment", "signOut() method called");
            
            // Show success message
            Toast.makeText(requireContext(), "Successfully logged out", Toast.LENGTH_SHORT).show();
            
            // Navigate to login screen
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Log.d("ProfileFragment", "Started LoginActivity");
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error during logout: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Logout failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void initializeViews(View view) {
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvDesignation = view.findViewById(R.id.tv_designation);
        tvEmail = view.findViewById(R.id.tv_email);
        
        tvEventsCount = view.findViewById(R.id.tv_events_count);
        tvBookingsCount = view.findViewById(R.id.tv_bookings_count);
        tvCommunitiesCount = view.findViewById(R.id.tv_communities_count);
        
        rvPendingRequests = view.findViewById(R.id.rv_pending_requests);
        rvApprovals = view.findViewById(R.id.rv_approvals);
        
        tvNoPendingRequests = view.findViewById(R.id.tv_no_pending_requests);
        tvNoApprovals = view.findViewById(R.id.tv_no_approvals);
        
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnViewAllRequests = view.findViewById(R.id.btn_view_all_requests);
        btnViewAllApprovals = view.findViewById(R.id.btn_view_all_approvals);
    }
    
    private void setupAdapters() {
        // Setup pending requests adapter
        pendingRequestsAdapter = new ApprovalAdapter(getContext(), pendingRequests, this);
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPendingRequests.setAdapter(pendingRequestsAdapter);
        
        // Setup approvals adapter
        approvalsAdapter = new ApprovalAdapter(getContext(), approvals, this);
        rvApprovals.setLayoutManager(new LinearLayoutManager(getContext()));
        rvApprovals.setAdapter(approvalsAdapter);
    }
    
    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to edit profile screen
        });
        
        btnViewAllRequests.setOnClickListener(v -> {
            // TODO: Navigate to all requests screen
        });
        
        btnViewAllApprovals.setOnClickListener(v -> {
            // TODO: Navigate to all approvals screen
        });
    }
    
    private void loadUserProfile() {
        if (currentUser == null) {
            return;
        }
        
        // Add this debug toast to show UID
        Toast.makeText(getContext(), "Current user UID: " + currentUser.getUid(), Toast.LENGTH_LONG).show();
        
        db.collection("users").document(currentUser.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    userProfile = documentSnapshot.toObject(User.class);
                    
                    if (userProfile != null) {
                        // Add this debug toast to show designation
                        Toast.makeText(getContext(), "User designation: " + userProfile.getDesignation(), Toast.LENGTH_LONG).show();
                        
                        tvFullName.setText(userProfile.getFullName());
                        tvDesignation.setText(userProfile.getDesignation());
                        tvEmail.setText(userProfile.getEmail());
                        
                        if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
                            Glide.with(this)
                                .load(userProfile.getProfileImageUrl())
                                .placeholder(R.drawable.ic_profile)
                                .into(ivProfileImage);
                        }
                        
                        // Load stats
                        loadUserStats();
                    }
                }
            });
    }
    
    private void loadUserStats() {
        // Load events count
        db.collection("events")
            .whereEqualTo("organizerId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                tvEventsCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
        
        // Load bookings count
        db.collection("bookings")
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                tvBookingsCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
        
        // Load communities count
        db.collection("userCommunities")
            .whereEqualTo("userId", currentUser.getUid())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                tvCommunitiesCount.setText(String.valueOf(queryDocumentSnapshots.size()));
            });
    }
    
    private void loadPendingRequests() {
        if (currentUser != null) {
            db.collection("approvals")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("status", "PENDING")
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .limit(2)  // Only show 2 most recent requests
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingRequests.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoPendingRequests.setVisibility(View.VISIBLE);
                        rvPendingRequests.setVisibility(View.GONE);
                    } else {
                        tvNoPendingRequests.setVisibility(View.GONE);
                        rvPendingRequests.setVisibility(View.VISIBLE);
                        
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Approval approval = doc.toObject(Approval.class);
                            if (approval != null) {
                                pendingRequests.add(approval);
                            }
                        }
                        
                        pendingRequestsAdapter.notifyDataSetChanged();
                    }
                });
        }
    }
    
    private void loadApprovals() {
        if (currentUser != null && userProfile != null && userProfile.getDesignation() != null && 
                (userProfile.getDesignation().equals("Faculty") || userProfile.getDesignation().equals("Admin"))) {
            
            db.collection("approvals")
                .whereEqualTo("status", "PENDING")
                .orderBy("requestDate", Query.Direction.DESCENDING)
                .limit(2)  // Only show 2 most recent approvals
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    approvals.clear();
                    
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoApprovals.setVisibility(View.VISIBLE);
                        rvApprovals.setVisibility(View.GONE);
                    } else {
                        tvNoApprovals.setVisibility(View.GONE);
                        rvApprovals.setVisibility(View.VISIBLE);
                        
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Approval approval = doc.toObject(Approval.class);
                            if (approval != null) {
                                approvals.add(approval);
                            }
                        }
                        
                        approvalsAdapter.notifyDataSetChanged();
                    }
                });
        } else {
            tvNoApprovals.setVisibility(View.VISIBLE);
            rvApprovals.setVisibility(View.GONE);
        }
    }

    @Override
    public void onApprove(Approval approval, int position) {
        approval.approve(currentUser.getUid());
        
        db.collection("approvals")
            .document(approval.getId())
            .set(approval)
            .addOnSuccessListener(aVoid -> {
                // Update the local list and notify adapter
                approvals.remove(position);
                approvalsAdapter.notifyItemRemoved(position);
                
                if (approvals.isEmpty()) {
                    tvNoApprovals.setVisibility(View.VISIBLE);
                    rvApprovals.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public void onReject(Approval approval, int position) {
        approval.reject(currentUser.getUid());
        
        db.collection("approvals")
            .document(approval.getId())
            .set(approval)
            .addOnSuccessListener(aVoid -> {
                // Update the local list and notify adapter
                approvals.remove(position);
                approvalsAdapter.notifyItemRemoved(position);
                
                if (approvals.isEmpty()) {
                    tvNoApprovals.setVisibility(View.VISIBLE);
                    rvApprovals.setVisibility(View.GONE);
                }
            });
    }
    
    @Override
    public void onItemClick(Approval approval, int position) {
        // TODO: Navigate to approval details screen
    }
} 
