package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoodActivity extends AppCompatActivity {

    private EditText etMealName, etCalories, etTime, etNotes;
    private Spinner spMealType;
    private TextView tvMealList;
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
        setContentView(R.layout.activity_food);

        //Profile Picture Setup - Added null check to prevent crash
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

        etMealName = findViewById(R.id.etMealName);
        etCalories = findViewById(R.id.etCalories);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);
        spMealType = findViewById(R.id.spMealType);
        tvMealList = findViewById(R.id.tvMealListPlaceholder);
        Button btnSaveMeal = findViewById(R.id.btnSaveMeal);

        setupMealTypeSpinner();
        setupTimeField();

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
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(this, ForumPage.class));
                return true;
            } else if (itemId == R.id.ai_menu) {
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            }
            return false;
        });

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
        // Updated to use your custom R.layout.spinner_item for white text
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                mealTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMealType.setAdapter(adapter);
    }

    private void setupTimeField() {
        etTime.setText(getCurrentTimestamp());
        etTime.setEnabled(false);
        etTime.setFocusable(false);
        etTime.setClickable(false);
        etTime.setLongClickable(false);
        etTime.setCursorVisible(false);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(STORAGE_DATE_TIME_FORMAT);
    }

    private void saveMeal() {
        String name = etMealName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String mealType = spMealType.getSelectedItem().toString();
        String timestamp = etTime.getText().toString().trim();

        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);

        Matcher NA = p.matcher(name);
        Matcher CA = p.matcher(caloriesStr);
        Matcher NO = p.matcher(notes);

        boolean res1 = NA.find();
        boolean res2 = CA.find();
        boolean res3 = NO.find();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(caloriesStr)) {
            Toast.makeText(this, "Please fill in meal name and calories.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (res1 || res2 || res3) {
            Toast.makeText(this, "Invalid characters in input fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call Firestore instead of SQLite
        addDataToFirestore(userEmail, mealType, name, caloriesStr, timestamp, notes);
    }

    private void addDataToFirestore(String user, String mealType, String name, String calories, String time, String notes) {
        CollectionReference dbMeals = db.collection("FoodCollection");

        FoodLog foodLog = new FoodLog(user, mealType, name, calories, time, notes);

        dbMeals.add(foodLog)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FoodActivity.this, "Meal saved to Cloud", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    renderStoredMeals(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FoodActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearInputs() {
        etMealName.setText("");
        etCalories.setText("");
        etNotes.setText("");
        etTime.setText(getCurrentTimestamp());
    }

    private void renderStoredMeals() {
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder builder = new StringBuilder();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        FoodLog log = document.toObject(FoodLog.class);

                        builder.append("• ")
                                .append(log.getFL_MEAL_TYPE())
                                .append(" - ")
                                .append(log.getFL_MEAL_NAME())
                                .append(" (")
                                .append(log.getFL_CALORIES())
                                .append(" kcal at ")
                                .append(log.getFL_TIME())
                                .append(")\n");
                    }

                    if (builder.length() == 0) {
                        tvMealList.setText("No meals logged yet.");
                    } else {
                        tvMealList.setText(builder.toString());
                    }
                })
                .addOnFailureListener(e -> {
                    tvMealList.setText("Failed to load data: " + e.getMessage());
                });
    }
}