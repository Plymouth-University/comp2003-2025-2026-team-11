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

import com.example.corefood.FoodLogTable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FoodActivity extends AppCompatActivity {

    private EditText etMealName, etCalories, etTime, etNotes;
    private Spinner spMealType;
    private TextView tvMealList;
    private DatabaseHelper dbHelper;

    // Hardcoded test user email. In a real app, this would come from a login session.
    private final String TEST_USER_EMAIL = "test@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        dbHelper = new DatabaseHelper(this);

        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        spMealType = findViewById(R.id.spMealType);
        tvMealList = findViewById(R.id.tvMealListPlaceholder);
        Button btnSaveMeal = findViewById(R.id.btnSaveMeal);

        //Dashboard Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_food);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_food) {
                return true;
            } else if (itemId == R.id.nav_main) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_exercises) {
                startActivity(new Intent(this, ExerciseActivity.class));
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

        setupMealTypeSpinner();
        btnSaveMeal.setOnClickListener(v -> saveMeal());
        renderStoredMeals();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderStoredMeals();
    }

    private void setupMealTypeSpinner() {
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mealTypes);
        spMealType.setAdapter(adapter);
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String mealType = spMealType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in meal name, calories and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        int calValue;
        try {
            calValue = Integer.parseInt(caloriesStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Calories must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (calValue <= 0) {
            Toast.makeText(this, "Calories must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        FoodLogTable.insert(db, TEST_USER_EMAIL, mealType, name, calValue, time, notes);

        renderStoredMeals();
        clearInputs();
        Toast.makeText(this, "Meal saved.", Toast.LENGTH_SHORT).show();
    }

    private void clearInputs() {
        etMealName.setText("");
        etCalories.setText("");
        etTime.setText("");
        etNotes.setText("");
    }

    private void renderStoredMeals() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = FoodLogTable.getLogsForUser(db, TEST_USER_EMAIL);

        StringBuilder builder = new StringBuilder();
        if (cursor != null && cursor.moveToFirst()) {
            int mealTypeIndex = cursor.getColumnIndex(FoodLogTable.COL_MEAL_TYPE);
            int mealNameIndex = cursor.getColumnIndex(FoodLogTable.COL_MEAL_NAME);
            int caloriesIndex = cursor.getColumnIndex(FoodLogTable.COL_CALORIES);
            int timeIndex = cursor.getColumnIndex(FoodLogTable.COL_TIME);

            do {
                String mealType = cursor.getString(mealTypeIndex);
                String mealName = cursor.getString(mealNameIndex);
                int calories = cursor.getInt(caloriesIndex);
                String time = cursor.getString(timeIndex);

                builder.append("• ")
                        .append(mealType).append(" - ")
                        .append(mealName).append(" (")
                        .append(calories).append(" kcal at ")
                        .append(time).append(")\n");
            } while (cursor.moveToNext());
            cursor.close();
        }

        tvMealList.setText(builder.length() == 0 ? "No meals logged yet." : builder.toString());
    }
}