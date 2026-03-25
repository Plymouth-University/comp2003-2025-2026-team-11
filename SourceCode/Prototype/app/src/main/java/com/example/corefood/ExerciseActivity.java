package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class ExerciseActivity extends AppCompatActivity {

    private Spinner spExerciseType, spIntensity;
    private EditText etDuration, etExerciseTime, etExerciseNotes;
    private TextView tvExerciseList;
    private FirebaseFirestore db;
    private String userEmail;

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
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_SETTINGS", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
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

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                exerciseTypes
        );
        spExerciseType.setAdapter(typeAdapter);

        ArrayAdapter<String> intensityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                intensityLevels
        );
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

    private void saveExercise() {
        String type = spExerciseType.getSelectedItem().toString();
        String intensity = spIntensity.getSelectedItem().toString();
        String durationStr = etDuration.getText().toString().trim();
        String time = etExerciseTime.getText().toString().trim();
        String notes = etExerciseNotes.getText().toString().trim();

        if (TextUtils.isEmpty(durationStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in duration and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationStr);
        int caloriesBurned = CalorieCalculator.estimateExerciseCalories(type, intensity, duration);

        addDataToFirestore(userEmail, type, intensity, durationStr,
                String.valueOf(caloriesBurned), time, notes);
    }

    private void addDataToFirestore(String user, String type, String intensity, String duration, String calories, String time, String notes) {
        CollectionReference dbExercises = db.collection("ExerciseCollection");

        ExerciseLog exerciseLog = new ExerciseLog(user, type, intensity, duration, calories, time, notes);

        dbExercises.add(exerciseLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ExerciseActivity.this, "Exercise saved to Cloud", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    renderStoredExercises(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ExerciseActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void renderStoredExercises() {
        db.collection("ExerciseCollection")
                .whereEqualTo("el_USER", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder builder = new StringBuilder();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ExerciseLog log = document.toObject(ExerciseLog.class);

                        builder.append("• ")
                                .append(log.getEL_EXERCISE_TYPE()).append(" (")
                                .append(log.getEl_INTENSITY()).append(") - ")
                                .append(log.getEL_DURATION_MINS()).append(" mins at ")
                                .append(log.getEL_TIME())
                                .append(" | ~").append(log.getEL_CALORIES_BURNED()).append(" kcal\n");
                    }

                    if (builder.length() == 0) {
                        tvExerciseList.setText("No exercises logged yet.");
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