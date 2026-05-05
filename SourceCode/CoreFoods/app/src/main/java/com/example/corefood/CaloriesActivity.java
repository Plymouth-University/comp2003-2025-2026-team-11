package com.example.corefood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class CaloriesActivity extends AppCompatActivity {

    private static final String CALORIE_PREFS = "CoreFoodsCaloriePrefs";
    private static final String DAILY_TARGET_KEY = "dailyTarget";

    private EditText etDailyTarget;
    private TextView tvConsumed, tvBurned, tvNet, tvNotes;
    private HealthDataManager healthDataManager;
    private FirebaseFirestore db;
    private String userEmail;
    private ImageView profileImage;

    private String requireCurrentUserEmail() {
        String email = UserSessionManager.getCurrentUserEmail();

        if (email == null) {
            Toast.makeText(this, "No logged-in user found. Please log in again.", Toast.LENGTH_LONG).show();
            finish();
        }

        return email;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Integer savedTarget = getSavedDailyTarget();
        refreshFromData(savedTarget);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        healthDataManager = new HealthDataManager(this);
        db = FirebaseFirestore.getInstance();

        userEmail = UserSessionManager.getCurrentUserEmail();

        if (userEmail == null) {
            Toast.makeText(this, "No logged-in user found.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        etDailyTarget = findViewById(R.id.etDailyTarget);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvBurned = findViewById(R.id.tvBurned);
        tvNet = findViewById(R.id.tvNet);
        tvNotes = findViewById(R.id.tvNotes);

        // Profile Picture Setup
        profileImage = findViewById(R.id.profile_image);
        if (profileImage != null) {
            profileImage.setOnClickListener(v ->
                    startActivity(new Intent(this, ProfilePage.class)));
        }

        setupBottomNavigation();

        Button btnRecalculate = findViewById(R.id.btnRecalculate);
        Button btnResetDay = findViewById(R.id.btnResetDay);

        Integer savedTarget = getSavedDailyTarget();

        if (savedTarget != null) {
            etDailyTarget.setText(String.valueOf(savedTarget));
        }

        refreshFromData(savedTarget);

        btnRecalculate.setOnClickListener(v -> recalculate());
        btnResetDay.setOnClickListener(v -> resetDay());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_calories);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_main) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_exercises) {
                startActivity(new Intent(this, ExerciseActivity.class));
                return true;
            } else if (itemId == R.id.nav_food) {
                startActivity(new Intent(this, FoodActivity.class));
                return true;
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(this, ForumPage.class));
                return true;
            } else if (itemId == R.id.ai_menu) {
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            } else if (itemId == R.id.nav_calories) {
                return true;
            }

            return false;
        });
    }

    private void refreshFromData(Integer target) {
        String currentEmail = requireCurrentUserEmail();

        if (currentEmail == null) {
            return;
        }

        healthDataManager.getTodaySummaryForUser(currentEmail, new HealthDataManager.SummaryCallback() {
            @Override
            public void onSummaryLoaded(int consumed, int burned) {
                CalorieSummary summary = new CalorieSummary(consumed, burned);

                runOnUiThread(() -> renderTotals(summary, target));
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(
                                CaloriesActivity.this,
                                "Error fetching cloud data: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    private void recalculate() {
        String targetStr = etDailyTarget.getText().toString().trim();

        if (TextUtils.isEmpty(targetStr)) {
            Toast.makeText(this, "Please enter a daily target first.", Toast.LENGTH_SHORT).show();
            return;
        }

        int target;

        try {
            target = Integer.parseInt(targetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Target must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (target <= 0) {
            Toast.makeText(this, "Target must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        saveDailyTarget(target);
        refreshFromData(target);

        Toast.makeText(this, "Recalculated.", Toast.LENGTH_SHORT).show();
    }

    private void resetDay() {
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    db.collection("ExerciseCollection")
                            .whereEqualTo("el_USER", userEmail)
                            .get()
                            .addOnSuccessListener(exerciseSnapshots -> {
                                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : exerciseSnapshots) {
                                    doc.getReference().delete();
                                }

                                clearSavedDailyTarget();

                                etDailyTarget.setText("");

                                CalorieSummary emptySummary = new CalorieSummary(0, 0);
                                renderTotals(emptySummary, null);

                                tvNotes.setText("Daily target and cloud logs have been reset.");

                                Toast.makeText(
                                        CaloriesActivity.this,
                                        "Daily logs have been reset.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            CaloriesActivity.this,
                                            "Failed to reset exercise logs: " + e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to reset food logs: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void renderTotals(CalorieSummary summary, Integer target) {
        int consumed = summary.getConsumed();
        int burned = summary.getBurned();
        int net = summary.getNet();

        tvConsumed.setText("Today's calories consumed: " + consumed + " kcal");
        tvBurned.setText("Today's calories burned: " + burned + " kcal");
        tvNet.setText("Today's net calories: " + net + " kcal");

        if (target != null) {
            int delta = target - net;

            String status = (delta >= 0)
                    ? "You are about " + delta + " kcal under your daily target today."
                    : "You are about " + Math.abs(delta) + " kcal over your daily target today.";

            tvNotes.setText(status);
        } else {
            tvNotes.setText("Showing today's calorie summary.");
        }
    }

    private void saveDailyTarget(int target) {
        if (TextUtils.isEmpty(userEmail)) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(CALORIE_PREFS, MODE_PRIVATE);

        prefs.edit()
                .putInt(DAILY_TARGET_KEY + "_" + userEmail, target)
                .apply();
    }

    private Integer getSavedDailyTarget() {
        if (TextUtils.isEmpty(userEmail)) {
            return null;
        }

        SharedPreferences prefs = getSharedPreferences(CALORIE_PREFS, MODE_PRIVATE);
        String key = DAILY_TARGET_KEY + "_" + userEmail;

        if (!prefs.contains(key)) {
            return null;
        }

        return prefs.getInt(key, 0);
    }

    private void clearSavedDailyTarget() {
        if (TextUtils.isEmpty(userEmail)) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(CALORIE_PREFS, MODE_PRIVATE);

        prefs.edit()
                .remove(DAILY_TARGET_KEY + "_" + userEmail)
                .apply();
    }
}