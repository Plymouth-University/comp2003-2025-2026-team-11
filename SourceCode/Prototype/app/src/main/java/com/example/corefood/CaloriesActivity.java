package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CaloriesActivity extends AppCompatActivity {

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
        refreshFromData(null);
    }

    private void refreshFromData(Integer target) {
        String userEmail = requireCurrentUserEmail();
        if (userEmail == null) return;

        // Call the new Firestore asynchronous method
        healthDataManager.getTodaySummaryForUser(userEmail, new HealthDataManager.SummaryCallback() {
            @Override
            public void onSummaryLoaded(int consumed, int burned) {
                // Once data is returned from Firestore, update the UI
                CalorieSummary summary = new CalorieSummary(consumed, burned);
                renderTotals(summary, target);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(CaloriesActivity.this, "Error fetching cloud data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

        //Profile Picture Setup
        profileImage = findViewById(R.id.profile_image);
        findViewById(R.id.profile_image).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilePage.class)));

        Button btnRecalculate = findViewById(R.id.btnRecalculate);
        Button btnResetDay = findViewById(R.id.btnResetDay);

        refreshFromData(null);

        btnRecalculate.setOnClickListener(v -> recalculate());
        btnResetDay.setOnClickListener(v -> resetDay());
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

        refreshFromData(target);
        Toast.makeText(this, "Recalculated.", Toast.LENGTH_SHORT).show();
    }

    private void resetDay() {
        db.collection("FoodCollection")
                .whereEqualTo("FL_USER", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }

                    db.collection("ExerciseCollection")
                            .whereEqualTo("EL_USER", userEmail)
                            .get()
                            .addOnSuccessListener(exerciseSnapshots -> {
                                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : exerciseSnapshots) {
                                    doc.getReference().delete();
                                }

                                etDailyTarget.setText("");

                                CalorieSummary emptySummary = new CalorieSummary(0, 0);
                                renderTotals(emptySummary, null);

                                tvNotes.setText("Daily target and cloud logs have been reset.");
                                Toast.makeText(CaloriesActivity.this, "Daily logs have been reset.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reset: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
                    ? ("You are about " + delta + " kcal under your daily target today.")
                    : ("You are about " + Math.abs(delta) + " kcal over your daily target today.");
            tvNotes.setText(status);
        } else {
            tvNotes.setText("Showing today's calorie summary.");
        }
    }
}
