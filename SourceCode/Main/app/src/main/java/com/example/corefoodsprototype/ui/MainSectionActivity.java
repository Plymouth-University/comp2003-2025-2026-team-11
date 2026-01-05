package com.example.corefoodsprototype.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;
import com.example.corefoodsprototype.data.DatabaseHelper;

public class MainSectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_section);

        Button btnFood = findViewById(R.id.btnFood);
        Button btnExercise = findViewById(R.id.btnExercise);
        Button btnCalories = findViewById(R.id.btnCalories);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.createSampleUserForTesting();

        btnFood.setOnClickListener(v ->
                startActivity(new Intent(this, FoodActivity.class))
        );

        btnExercise.setOnClickListener(v ->
                startActivity(new Intent(this, ExerciseActivity.class))
        );

        btnCalories.setOnClickListener(v ->
                startActivity(new Intent(this, CaloriesActivity.class))
        );
    }
}
