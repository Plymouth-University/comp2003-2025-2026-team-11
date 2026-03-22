package com.example.corefood;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class ExerciseActivity extends AppCompatActivity {

    private Spinner spExerciseType, spIntensity;
    private EditText etDuration, etExerciseTime, etExerciseNotes;
    private TextView tvExerciseList;
    private DatabaseHelper dbHelper;

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

        dbHelper = new DatabaseHelper(this);

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
        String notes = etExerciseNotes.getText().toString().trim();
        String timestamp = getCurrentTimestamp();

        if (TextUtils.isEmpty(durationStr)) {
            Toast.makeText(this, "Please fill in duration.", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Duration must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duration <= 0) {
            Toast.makeText(this, "Duration must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        int caloriesBurned = CalorieCalculator.estimateExerciseCalories(type, intensity, duration);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String userEmail = requireCurrentUserEmail();
        if (userEmail == null) return;

        dbHelper.ensureUserExists(userEmail);

        long result = ExerciseLogTable.insert(
                db,
                userEmail,
                type,
                intensity,
                duration,
                caloriesBurned,
                timestamp,
                notes
        );

        if (result == -1) {
            Toast.makeText(this, "Failed to save exercise.", Toast.LENGTH_SHORT).show();
            return;
        }

        renderStoredExercises();
        clearInputs();
        Toast.makeText(this, "Exercise saved.", Toast.LENGTH_SHORT).show();
    }

    private void renderStoredExercises() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String userEmail = requireCurrentUserEmail();
        if (userEmail == null) return;

        Cursor cursor = ExerciseLogTable.getLogsForUser(db, userEmail);

        StringBuilder builder = new StringBuilder();
        if (cursor != null) {
            int typeIndex = cursor.getColumnIndex(ExerciseLogTable.COL_EXERCISE_TYPE);
            int intensityIndex = cursor.getColumnIndex(ExerciseLogTable.COL_INTENSITY);
            int durationIndex = cursor.getColumnIndex(ExerciseLogTable.COL_DURATION_MINS);
            int calsIndex = cursor.getColumnIndex(ExerciseLogTable.COL_CALORIES_BURNED);
            int timeIndex = cursor.getColumnIndex(ExerciseLogTable.COL_TIME);

            while (cursor.moveToNext()) {
                String type = cursor.getString(typeIndex);
                String intensity = cursor.getString(intensityIndex);
                int duration = cursor.getInt(durationIndex);
                int cals = cursor.getInt(calsIndex);
                String time = cursor.getString(timeIndex);

                builder.append("• ")
                        .append(type)
                        .append(" (")
                        .append(intensity)
                        .append(") - ")
                        .append(duration)
                        .append(" mins at ")
                        .append(time)
                        .append(" | ~")
                        .append(cals)
                        .append(" kcal\n");
            }
            cursor.close();
        }

        tvExerciseList.setText(builder.length() == 0 ? "No exercises logged yet." : builder.toString());
    }

    private void clearInputs() {
        etDuration.setText("");
        etExerciseNotes.setText("");
        etExerciseTime.setText(getCurrentTimestamp());
    }
}