package com.example.corefoodsprototype.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;

public class MainSectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_section);

        Button btnFood = findViewById(R.id.btnFood);
        Button btnExercise = findViewById(R.id.btnExercise);
        Button btnCalories = findViewById(R.id.btnCalories);

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
