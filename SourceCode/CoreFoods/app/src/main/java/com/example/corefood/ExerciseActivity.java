package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExerciseActivity extends AppCompatActivity {

    private Spinner spExerciseType, spIntensity;
    private EditText etDuration, etExerciseTime, etExerciseNotes;
    private TextView tvExerciseList;
    private FirebaseFirestore db;
    private String userEmail;
    private ImageView profileImage;

    private static final DateTimeFormatter STORAGE_DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String requireCurrentUserEmail() {
        String email = UserSessionManager.getCurrentUserEmail();
        if (email == null) {
            Toast.makeText(this, "No logged-in user found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
        }
        return email;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        //Profile Picture Setup
        profileImage = findViewById(R.id.profile_image);
        if (profileImage != null) {
            profileImage.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfilePage.class)));
        }


        db = FirebaseFirestore.getInstance();

        userEmail = UserSessionManager.getCurrentUserEmail();
        if (userEmail == null) {
            Toast.makeText(this, "No logged-in user found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        spExerciseType = findViewById(R.id.spExerciseType);
        spIntensity = findViewById(R.id.spIntensity);
        etDuration = findViewById(R.id.etDuration);
        etExerciseTime = findViewById(R.id.etExerciseTime);
        etExerciseNotes = findViewById(R.id.etExerciseNotes);
        tvExerciseList = findViewById(R.id.tvExerciseListPlaceholder);
        Button btnSaveExercise = findViewById(R.id.btnSaveExercise);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_exercises);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_exercises) {
                return true;
            } else if (itemId == R.id.nav_main) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_food) {
                startActivity(new Intent(this, FoodActivity.class));
                return true;
            } else if (itemId == R.id.nav_calories) {
                startActivity(new Intent(this, CaloriesActivity.class));
                return true;
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(this, ForumPage.class));
                return true;
            } else if (itemId == R.id.ai_menu) {
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            }

            return false;
        });

        setupSpinners();
        setupTimeField();
        btnSaveExercise.setOnClickListener(v -> saveExercise());
        renderStoredExercises();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderStoredExercises();
    }

    private void setupSpinners() {
        String[] exerciseTypes = {"Walking", "Running", "Cycling", "Weight Training", "Swimming", "Yoga", "HIIT", "Other"};
        String[] intensityLevels = {"Low", "Medium", "High"};

        // Updated to use your custom layout for white text
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // Use your custom XML here
                exerciseTypes
        );
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spExerciseType.setAdapter(typeAdapter);

        ArrayAdapter<String> intensityAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item, // Use your custom XML here
                intensityLevels
        );
        intensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIntensity.setAdapter(intensityAdapter);
    }

    private void setupTimeField() {
        etExerciseTime.setText(getCurrentTimestamp());
        etExerciseTime.setEnabled(false);
        etExerciseTime.setFocusable(false);
        etExerciseTime.setClickable(false);
        etExerciseTime.setLongClickable(false);
        etExerciseTime.setCursorVisible(false);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME_FORMAT);
    }

    private String getTodayDatePrefix() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void saveExercise() {
        String type = spExerciseType.getSelectedItem().toString();
        String intensity = spIntensity.getSelectedItem().toString();
        String durationStr = etDuration.getText().toString().trim();
        String time = etExerciseTime.getText().toString().trim();
        String notes = etExerciseNotes.getText().toString().trim();

        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

        Matcher DU = p.matcher(durationStr);

        boolean res1 = DU.find();

        if (TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in duration and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (res1) {
            Toast.makeText(this, "Invalid characters in input fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            int caloriesBurned = CalorieCalculator.estimateExerciseCalories(type, intensity, duration);

            addDataToFirestore(userEmail, type, intensity, durationStr,
                    String.valueOf(caloriesBurned), time, notes);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number for duration.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addDataToFirestore(String user, String type, String intensity, String duration, String calories, String time, String notes) {
        CollectionReference dbExercises = db.collection("ExerciseCollection");

        ExerciseLog exerciseLog = new ExerciseLog(user, type, intensity, duration, calories, time, notes);

        dbExercises.add(exerciseLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ExerciseActivity.this, "Exercise saved to Cloud", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    renderStoredExercises();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExerciseActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderStoredExercises() {
        String todayDatePrefix = getTodayDatePrefix();

        db.collection("ExerciseCollection")
                .whereEqualTo("el_USER", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder builder = new StringBuilder();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ExerciseLog log = document.toObject(ExerciseLog.class);

                        String logTime = log.getEL_TIME();

                        if (logTime == null || !logTime.startsWith(todayDatePrefix)) {
                            continue;
                        }

                        builder.append("• ")
                                .append(log.getEL_EXERCISE_TYPE()).append(" (")
                                .append(log.getEl_INTENSITY()).append(") - ")
                                .append(log.getEL_DURATION_MINS()).append(" mins at ")
                                .append(log.getEL_TIME())
                                .append(" | ~").append(log.getEL_CALORIES_BURNED()).append(" kcal");

                        if (log.getEL_NOTES() != null && !log.getEL_NOTES().isEmpty()) {
                            builder.append("\n  Notes: ").append(log.getEL_NOTES());
                        }
                        builder.append("\n");
                    }

                    if (builder.length() == 0) {
                        tvExerciseList.setText("No exercises logged today.");
                    } else {
                        tvExerciseList.setText(builder.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    tvExerciseList.setText("Failed to load data: " + e.getMessage());
                });
    }

    private void clearInputs() {
        etDuration.setText("");
        etExerciseNotes.setText("");
        etExerciseTime.setText(getCurrentTimestamp());
    }
}