package com.example.corefood;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefood.ExerciseLogTable;
import com.example.corefood.FoodLogTable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CaloriesActivity extends AppCompatActivity {

    private EditText etDailyTarget;
    private TextView tvConsumed, tvBurned, tvNet, tvNotes;
    private DatabaseHelper dbHelper;

    // Hardcoded test user email. In a real app, this would come from a login session.
    private final String TEST_USER_EMAIL = "test@example.com";

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromDatabase(null);
    }

    private void refreshFromDatabase(Integer target) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, TEST_USER_EMAIL);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, TEST_USER_EMAIL);
        renderTotals(consumed, burned, target);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        dbHelper = new DatabaseHelper(this);

        etDailyTarget = findViewById(R.id.etDailyTarget);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvBurned = findViewById(R.id.tvBurned);
        tvNet = findViewById(R.id.tvNet);
        tvNotes = findViewById(R.id.tvNotes);

        //Dashboard Navigation
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
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_SETTINGS", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.ai_menu) {
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            } else if (itemId == R.id.nav_calories) {
                return true;
            }
            return false;
        });

        Button btnRecalculate = findViewById(R.id.btnRecalculate);
        Button btnResetDay = findViewById(R.id.btnResetDay);

        refreshFromDatabase(null);

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

        refreshFromDatabase(target);
        Toast.makeText(this, "Recalculated.", Toast.LENGTH_SHORT).show();
    }

    private void resetDay() {
        etDailyTarget.setText("");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        FoodLogTable.deleteAllLogsForUser(db, TEST_USER_EMAIL);
        ExerciseLogTable.deleteAllLogsForUser(db, TEST_USER_EMAIL);

        refreshFromDatabase(null);

        tvNotes.setText("Day reset. Log meals and exercises again to rebuild totals.");
        Toast.makeText(this, "Day reset.", Toast.LENGTH_SHORT).show();
    }

    private void renderTotals(int consumed, int burned, Integer target) {
        tvConsumed.setText("Calories consumed: " + consumed + " kcal");
        tvBurned.setText("Calories burned (estimated): " + burned + " kcal");

        int net = consumed - burned;
        tvNet.setText("Net balance: " + net + " kcal");

        if (target != null) {
            int delta = target - net;
            String status = (delta >= 0)
                    ? ("You are about " + delta + " kcal under your target.")
                    : ("You are about " + Math.abs(delta) + " kcal over your target.");
            tvNotes.setText(status);
        } else {
            // Clear the notes if there's no target
            tvNotes.setText("");
        }
    }
}