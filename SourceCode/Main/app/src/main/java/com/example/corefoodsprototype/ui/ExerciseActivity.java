package com.example.corefoodsprototype.ui;

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

import java.util.ArrayList;
import java.util.List;

public class ExerciseActivity extends AppCompatActivity {

    private Spinner spExerciseType, spIntensity;
    private EditText etDuration, etExerciseTime, etExerciseNotes;
    private TextView tvExerciseList;

    private final List<String> exercisesLogged = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        spExerciseType = findViewById(R.id.spExerciseType);
        spIntensity = findViewById(R.id.spIntensity);
        etDuration = findViewById(R.id.etDuration);
        etExerciseTime = findViewById(R.id.etExerciseTime);
        etExerciseNotes = findViewById(R.id.etExerciseNotes);
        tvExerciseList = findViewById(R.id.tvExerciseListPlaceholder);
        Button btnSaveExercise = findViewById(R.id.btnSaveExercise);

        setupSpinners();

        btnSaveExercise.setOnClickListener(v -> saveExercise());
    }

    private void setupSpinners() {
        String[] exerciseTypes = {
                "Walking", "Running", "Cycling", "Weight Training",
                "Swimming", "Yoga", "HIIT", "Other"
        };

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

    private void saveExercise() {
        String type = spExerciseType.getSelectedItem().toString();
        String intensity = spIntensity.getSelectedItem().toString();
        String durationStr = etDuration.getText().toString().trim();
        String time = etExerciseTime.getText().toString().trim();

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

        String entry = type + " (" + intensity + ") - " + duration + " mins at " + time;
        exercisesLogged.add(entry);

        updateExerciseList();
        clearInputs();

        Toast.makeText(this, "Exercise saved.", Toast.LENGTH_SHORT).show();
    }

    private void updateExerciseList() {
        StringBuilder builder = new StringBuilder();
        for (String ex : exercisesLogged) {
            builder.append("• ").append(ex).append("\n");
        }
        tvExerciseList.setText(builder.toString());
    }

    private void clearInputs() {
        etDuration.setText("");
        etExerciseTime.setText("");
        etExerciseNotes.setText("");
    }
}