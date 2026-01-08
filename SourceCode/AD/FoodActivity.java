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

import com.example.corefoodsprototype.data.PrototypeDataStore;


public class FoodActivity extends AppCompatActivity {

    private EditText etMealName, etCalories, etTime, etNotes;
    private Spinner spMealType;
    private TextView tvMealList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        spMealType = findViewById(R.id.spMealType);
        tvMealList = findViewById(R.id.tvMealListPlaceholder);
        Button btnSaveMeal = findViewById(R.id.btnSaveMeal);

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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                mealTypes
        );

        spMealType.setAdapter(adapter);
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String calories = etCalories.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String mealType = spMealType.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(calories) || TextUtils.isEmpty(time)) {
            Toast.makeText(this, "Please fill in meal name, calories and time.", Toast.LENGTH_SHORT).show();
            return;
        }

        int calValue;
        try {
            calValue = Integer.parseInt(calories);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Calories must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (calValue <= 0) {
            Toast.makeText(this, "Calories must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        String entry = mealType + " - " + name + " (" + calValue + " kcal at " + time + ")";
        PrototypeDataStore.getInstance().addFoodEntry(entry);
        renderStoredMeals();

        PrototypeDataStore.getInstance().addCaloriesConsumed(calValue);

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
        StringBuilder builder = new StringBuilder();
        for (String meal : PrototypeDataStore.getInstance().getFoodEntries()) {
            builder.append("• ").append(meal).append("\n");
        }
        tvMealList.setText(builder.length() == 0 ? "No meals logged yet." : builder.toString());
    }

}
