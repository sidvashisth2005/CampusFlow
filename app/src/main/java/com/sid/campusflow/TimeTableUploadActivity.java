package com.sid.campusflow;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sid.campusflow.models.TimeTable;
import com.sid.campusflow.models.User;
import com.sid.campusflow.utils.FirebaseUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TimeTableUploadActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout tilTitle;
    private EditText etTitle;
    private Spinner spinnerYear;
    private Spinner spinnerDepartment;
    private Spinner spinnerSemester;
    private EditText etMondayClasses;
    private EditText etTuesdayClasses;
    private EditText etWednesdayClasses;
    private EditText etThursdayClasses;
    private EditText etFridayClasses;
    private Button btnUpload;
    private ProgressBar progressBar;

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
                    Toast.makeText(this, "Only administrators can upload timetables", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Failed to verify admin status", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        setContentView(R.layout.activity_timetable_upload);

        // Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Upload Timetable");

        // Initialize views
        tilTitle = findViewById(R.id.til_title);
        etTitle = findViewById(R.id.et_title);
        spinnerYear = findViewById(R.id.spinner_year);
        spinnerDepartment = findViewById(R.id.spinner_department);
        spinnerSemester = findViewById(R.id.spinner_semester);
        etMondayClasses = findViewById(R.id.et_monday_classes);
        etTuesdayClasses = findViewById(R.id.et_tuesday_classes);
        etWednesdayClasses = findViewById(R.id.et_wednesday_classes);
        etThursdayClasses = findViewById(R.id.et_thursday_classes);
        etFridayClasses = findViewById(R.id.et_friday_classes);
        btnUpload = findViewById(R.id.btn_upload);
        progressBar = findViewById(R.id.progress_bar);

        // Setup spinners
        setupSpinners();

        // Setup click listeners
        setupClickListeners();
    }

    private void setupSpinners() {
        // Year spinner
        String[] years = new String[]{"First Year", "Second Year", "Third Year", "Fourth Year"};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);

        // Department spinner
        String[] departments = new String[]{"Computer Science", "Electrical Engineering", "Mechanical Engineering", 
                                           "Civil Engineering", "Mathematics", "Physics", "Chemistry"};
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, departments);
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartment.setAdapter(departmentAdapter);

        // Semester spinner
        String[] semesters = new String[]{"Semester 1", "Semester 2", "Semester 3", "Semester 4", 
                                         "Semester 5", "Semester 6", "Semester 7", "Semester 8"};
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, semesters);
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);
    }

    private void setupClickListeners() {
        btnUpload.setOnClickListener(v -> {
            if (validateInputs()) {
                uploadTimetable();
            }
        });
    }

    private boolean validateInputs() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            return false;
        }
        
        tilTitle.setError(null);
        return true;
    }

    private void uploadTimetable() {
        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        btnUpload.setEnabled(false);

        // Get current user
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to upload a timetable", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            return;
        }

        // Create timetable object
        TimeTable timeTable = new TimeTable();
        timeTable.setTitle(etTitle.getText().toString().trim());
        timeTable.setYear(spinnerYear.getSelectedItem().toString());
        timeTable.setDepartment(spinnerDepartment.getSelectedItem().toString());
        timeTable.setSemester(spinnerSemester.getSelectedItem().toString());
        timeTable.setLastUpdated(new Date());
        timeTable.setUploadedBy(currentUser.getUid());

        // Create schedule map
        Map<String, Map<String, String>> schedule = new HashMap<>();
        
        // Monday
        Map<String, String> mondaySchedule = parseClassSchedule(etMondayClasses.getText().toString());
        if (!mondaySchedule.isEmpty()) {
            schedule.put("Monday", mondaySchedule);
        }
        
        // Tuesday
        Map<String, String> tuesdaySchedule = parseClassSchedule(etTuesdayClasses.getText().toString());
        if (!tuesdaySchedule.isEmpty()) {
            schedule.put("Tuesday", tuesdaySchedule);
        }
        
        // Wednesday
        Map<String, String> wednesdaySchedule = parseClassSchedule(etWednesdayClasses.getText().toString());
        if (!wednesdaySchedule.isEmpty()) {
            schedule.put("Wednesday", wednesdaySchedule);
        }
        
        // Thursday
        Map<String, String> thursdaySchedule = parseClassSchedule(etThursdayClasses.getText().toString());
        if (!thursdaySchedule.isEmpty()) {
            schedule.put("Thursday", thursdaySchedule);
        }
        
        // Friday
        Map<String, String> fridaySchedule = parseClassSchedule(etFridayClasses.getText().toString());
        if (!fridaySchedule.isEmpty()) {
            schedule.put("Friday", fridaySchedule);
        }
        
        timeTable.setSchedule(schedule);

        // Upload to Firestore
        FirebaseUtils.uploadTimeTable(timeTable, task -> {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            
            if (task.isSuccessful()) {
                DocumentReference documentReference = task.getResult();
                Toast.makeText(this, "Timetable uploaded successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to upload timetable: " + task.getException().getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to parse class schedule text into a map
    private Map<String, String> parseClassSchedule(String scheduleText) {
        Map<String, String> schedule = new HashMap<>();
        if (scheduleText == null || scheduleText.trim().isEmpty()) {
            return schedule;
        }
        
        String[] lines = scheduleText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                String[] parts = line.split("-", 2);
                if (parts.length == 2) {
                    String timeSlot = parts[0].trim();
                    String subject = parts[1].trim();
                    schedule.put(timeSlot, subject);
                }
            }
        }
        
        return schedule;
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