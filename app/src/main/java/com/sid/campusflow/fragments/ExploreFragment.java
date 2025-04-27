package com.sid.campusflow.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sid.campusflow.R;
import com.sid.campusflow.MapViewActivity;
import com.sid.campusflow.adapters.NoticeAdapter;
import com.sid.campusflow.models.Notice;
import com.sid.campusflow.TimeTableUploadActivity;
import com.sid.campusflow.models.User;
import com.sid.campusflow.utils.FirebaseUtils;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExploreFragment extends Fragment {

    private Button btnViewMap, btnViewTimetable, btnViewNotices;
    private ImageButton btnUploadTimetable;
    private RecyclerView recyclerNotices;
    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeList;
    private FirebaseFirestore db;
    private View loadingView, emptyView;
    private Spinner spinnerYearFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        btnViewMap = view.findViewById(R.id.btn_view_map);
        btnViewTimetable = view.findViewById(R.id.btn_view_timetable);
        btnViewNotices = view.findViewById(R.id.btn_view_notices);
        btnUploadTimetable = view.findViewById(R.id.btn_upload_timetable);
        recyclerNotices = view.findViewById(R.id.recycler_notices);
        loadingView = view.findViewById(R.id.loading_view);
        emptyView = view.findViewById(R.id.empty_view);
        spinnerYearFilter = view.findViewById(R.id.spinner_year_filter);
        
        // Setup year filter spinner
        setupYearFilterSpinner();
        
        // Setup recycler view
        setupRecyclerView();
        
        // Load notices
        loadNotices();
        
        // Setup click listeners
        setupClickListeners();
        
        // Check if user is admin to show/hide admin controls
        checkAdminStatus();
    }

    private void setupRecyclerView() {
        noticeList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(noticeList);
        recyclerNotices.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerNotices.setAdapter(noticeAdapter);
    }
    
    private void loadNotices() {
        if (loadingView != null) loadingView.setVisibility(View.VISIBLE);
        if (recyclerNotices != null) recyclerNotices.setVisibility(View.GONE);
        if (emptyView != null) emptyView.setVisibility(View.GONE);
        
        db.collection("notices")
            .orderBy("publishDate")
            .get()
            .addOnCompleteListener(task -> {
                if (loadingView != null) loadingView.setVisibility(View.GONE);
                
                if (task.isSuccessful()) {
                    noticeList.clear();
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Notice notice = document.toObject(Notice.class);
                        noticeList.add(notice);
                    }
                    
                    noticeAdapter.notifyDataSetChanged();
                    
                    if (noticeList.isEmpty()) {
                        if (emptyView != null) {
                            TextView tvTitle = emptyView.findViewById(R.id.empty_state_title);
                            TextView tvMessage = emptyView.findViewById(R.id.empty_state_message);
                            if (tvTitle != null) {
                                tvTitle.setText(R.string.no_notices_available);
                            }
                            if (tvMessage != null) {
                                tvMessage.setText(R.string.no_notices_available);
                            }
                            emptyView.setVisibility(View.VISIBLE);
                        }
                        if (recyclerNotices != null) recyclerNotices.setVisibility(View.GONE);
                    } else {
                        if (emptyView != null) emptyView.setVisibility(View.GONE);
                        if (recyclerNotices != null) recyclerNotices.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error loading notices: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    if (emptyView != null) {
                        TextView tvTitle = emptyView.findViewById(R.id.empty_state_title);
                        TextView tvMessage = emptyView.findViewById(R.id.empty_state_message);
                        if (tvTitle != null) {
                            tvTitle.setText(R.string.no_notices_available);
                        }
                        if (tvMessage != null) {
                            tvMessage.setText(R.string.no_notices_available);
                        }
                        emptyView.setVisibility(View.VISIBLE);
                    }
                    if (recyclerNotices != null) recyclerNotices.setVisibility(View.GONE);
                }
            });
    }

    private void setupYearFilterSpinner() {
        // Create array of year options
        String[] years = new String[]{"All Years", "First Year", "Second Year", "Third Year", "Fourth Year"};
        
        // Create and set adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, years);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYearFilter.setAdapter(adapter);
        
        // Hide initially
        spinnerYearFilter.setVisibility(View.GONE);
        
        // Set listener
        spinnerYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (recyclerNotices.getVisibility() == View.VISIBLE) {
                    loadTimetable(spinnerYearFilter.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupClickListeners() {
        btnViewMap.setOnClickListener(v -> {
            // Open campus map viewer activity instead of Google Maps
            Intent intent = new Intent(requireContext(), MapViewActivity.class);
            startActivity(intent);
        });
        
        btnViewTimetable.setOnClickListener(v -> {
            // Toggle timetable visibility
            if (recyclerNotices.getVisibility() == View.VISIBLE && 
                spinnerYearFilter.getVisibility() == View.VISIBLE) {
                // Hide timetable
                recyclerNotices.setVisibility(View.GONE);
                spinnerYearFilter.setVisibility(View.GONE);
                btnViewTimetable.setText(R.string.view_timetable);
                btnViewNotices.setEnabled(true);
            } else {
                // Show timetable
                spinnerYearFilter.setVisibility(View.VISIBLE);
                loadTimetable(spinnerYearFilter.getSelectedItem().toString());
                recyclerNotices.setVisibility(View.VISIBLE);
                btnViewTimetable.setText(R.string.hide_timetable);
                btnViewNotices.setEnabled(false);
            }
        });
        
        btnViewNotices.setOnClickListener(v -> {
            // Reset to notices view
            if (spinnerYearFilter.getVisibility() == View.VISIBLE) {
                spinnerYearFilter.setVisibility(View.GONE);
                btnViewTimetable.setText(R.string.view_timetable);
            }
            // Refresh notices
            loadNotices();
        });

        btnUploadTimetable.setOnClickListener(v -> {
            // Launch timetable upload activity (for admins only)
            Intent intent = new Intent(requireContext(), TimeTableUploadActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadTimetable(String yearFilter) {
        // Show loading view
        loadingView.setVisibility(View.VISIBLE);
        recyclerNotices.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        
        // Clear current list
        noticeList.clear();
        
        if ("All Years".equals(yearFilter)) {
            // Load all timetables
            FirebaseUtils.getAllTimeTables(task -> {
                handleTimetableQueryResult(task);
            });
        } else {
            // Load timetables for specific year
            FirebaseUtils.getTimeTablesByYear(yearFilter, task -> {
                handleTimetableQueryResult(task);
            });
        }
    }
    
    private void handleTimetableQueryResult(Task<QuerySnapshot> task) {
        loadingView.setVisibility(View.GONE);
        
        if (task.isSuccessful()) {
            // Convert timetables to notices for display
            for (QueryDocumentSnapshot document : task.getResult()) {
                com.sid.campusflow.models.TimeTable timeTable = document.toObject(com.sid.campusflow.models.TimeTable.class);
                timeTable.setId(document.getId());
                
                // Create a notice from the timetable
                Notice timeTableNotice = new Notice();
                timeTableNotice.setId(timeTable.getId());
                timeTableNotice.setTitle(timeTable.getTitle() + " (" + timeTable.getYear() + ")");
                
                // Format schedule into content
                StringBuilder content = new StringBuilder();
                Map<String, Map<String, String>> schedule = timeTable.getSchedule();
                
                for (String day : schedule.keySet()) {
                    content.append(day).append(":\n");
                    Map<String, String> daySchedule = schedule.get(day);
                    
                    for (String timeSlot : daySchedule.keySet()) {
                        content.append("  ").append(timeSlot).append(" - ").append(daySchedule.get(timeSlot)).append("\n");
                    }
                    content.append("\n");
                }
                
                timeTableNotice.setContent(content.toString().trim());
                timeTableNotice.setPublishDate(timeTable.getLastUpdated());
                
                noticeList.add(timeTableNotice);
            }
            
            if (noticeList.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerNotices.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerNotices.setVisibility(View.VISIBLE);
                noticeAdapter.notifyDataSetChanged();
            }
        } else {
            // Show error and fallback to sample data
            Toast.makeText(getContext(), "Failed to load timetables: " + task.getException().getMessage(), 
                          Toast.LENGTH_SHORT).show();
            loadSampleTimetable();
        }
    }
    
    private void loadSampleTimetable() {
        // Fallback method with sample data
        noticeList.clear();
        
        Notice mondayTimetable = new Notice();
        mondayTimetable.setTitle("Monday Classes");
        mondayTimetable.setContent("9:00 AM - Mathematics\n11:00 AM - Computer Science\n2:00 PM - Physics");
        mondayTimetable.setPublishDate(new Date());
        
        Notice tuesdayTimetable = new Notice();
        tuesdayTimetable.setTitle("Tuesday Classes");
        tuesdayTimetable.setContent("10:00 AM - Chemistry\n1:00 PM - English Literature\n3:00 PM - Programming Lab");
        tuesdayTimetable.setPublishDate(new Date());
        
        Notice wednesdayTimetable = new Notice();
        wednesdayTimetable.setTitle("Wednesday Classes");
        wednesdayTimetable.setContent("9:00 AM - Biology\n12:00 PM - History\n2:00 PM - Physical Education");
        wednesdayTimetable.setPublishDate(new Date());
        
        noticeList.add(mondayTimetable);
        noticeList.add(tuesdayTimetable);
        noticeList.add(wednesdayTimetable);
        
        emptyView.setVisibility(View.GONE);
        recyclerNotices.setVisibility(View.VISIBLE);
        noticeAdapter.notifyDataSetChanged();
    }

    private void checkAdminStatus() {
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser != null) {
            FirebaseUtils.getCurrentUserData(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User user = task.getResult().toObject(User.class);
                    // Only show upload buttons for admin users
                    if (user != null && "admin".equals(user.getDesignation())) {
                        btnUploadTimetable.setVisibility(View.VISIBLE);
                    } else {
                        btnUploadTimetable.setVisibility(View.GONE);
                    }
                } else {
                    btnUploadTimetable.setVisibility(View.GONE);
                }
            });
        } else {
            btnUploadTimetable.setVisibility(View.GONE);
        }
    }
} 
