package com.example.corefoodsprototype.ui;

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
import com.example.corefoodsprototype.R;
import com.example.corefoodsprototype.data.DatabaseHelper;
import com.example.corefoodsprototype.data.ExerciseLogTable;

public class ExerciseActivity extends AppCompatActivity {

    private Spinner spExerciseType, spIntensity;
    private EditText etDuration, etExerciseTime, etExerciseNotes;
    private TextView tvExerciseList;
    private DatabaseHelper dbHelper;

    // Hardcoded test user email. In a real app, this would come from a login session.
    private final String TEST_USER_EMAIL = "test@example.com";

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

        setupSpinners();
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

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, exerciseTypes);
        spExerciseType.setAdapter(typeAdapter);

        ArrayAdapter<String> intensityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, intensityLevels);
        spIntensity.setAdapter(intensityAdapter);
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

        int caloriesBurned = estimateCaloriesBurned(type, intensity, duration);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ExerciseLogTable.insert(db, TEST_USER_EMAIL, type, intensity, duration, caloriesBurned, time, notes);

        renderStoredExercises();
        clearInputs();
        Toast.makeText(this, "Exercise saved.", Toast.LENGTH_SHORT).show();
    }

    private void renderStoredExercises() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = ExerciseLogTable.getLogsForUser(db, TEST_USER_EMAIL);

        StringBuilder builder = new StringBuilder();
        if (cursor != null && cursor.moveToFirst()) {
            int typeIndex = cursor.getColumnIndex(ExerciseLogTable.COL_EXERCISE_TYPE);
            int intensityIndex = cursor.getColumnIndex(ExerciseLogTable.COL_INTENSITY);
            int durationIndex = cursor.getColumnIndex(ExerciseLogTable.COL_DURATION_MINS);
            int calsIndex = cursor.getColumnIndex(ExerciseLogTable.COL_CALORIES_BURNED);
            int timeIndex = cursor.getColumnIndex(ExerciseLogTable.COL_TIME);

            do {
                String type = cursor.getString(typeIndex);
                String intensity = cursor.getString(intensityIndex);
                int duration = cursor.getInt(durationIndex);
                int cals = cursor.getInt(calsIndex);
                String time = cursor.getString(timeIndex);

                builder.append("• ")
                        .append(type).append(" (").append(intensity).append(") - ")
                        .append(duration).append(" mins at ").append(time)
                        .append(" | ~").append(cals).append(" kcal\n");
            } while (cursor.moveToNext());
            cursor.close();
        }

        tvExerciseList.setText(builder.length() == 0 ? "No exercises logged yet." : builder.toString());
    }

    private void clearInputs() {
        etDuration.setText("");
        etExerciseTime.setText("");
        etExerciseNotes.setText("");
    }

    private int estimateCaloriesBurned(String type, String intensity, int durationMins) {
        // Simple prototype estimates.
        double baseRate;
        switch (type) {
            case "Running": baseRate = 10.0; break;
            case "Cycling": baseRate = 8.0; break;
            case "Swimming": baseRate = 9.0; break;
            case "Weight Training": baseRate = 6.0; break;
            case "HIIT": baseRate = 11.0; break;
            case "Yoga": baseRate = 4.0; break;
            case "Walking": baseRate = 5.0; break;
            default: baseRate = 6.0; break; // "Other"
        }

        double intensityMultiplier;
        switch (intensity) {
            case "High": intensityMultiplier = 1.3; break;
            case "Low": intensityMultiplier = 0.8; break;
            default: intensityMultiplier = 1.0; break; // Medium
        }

        return (int) Math.round(baseRate * intensityMultiplier * durationMins);
    }
}