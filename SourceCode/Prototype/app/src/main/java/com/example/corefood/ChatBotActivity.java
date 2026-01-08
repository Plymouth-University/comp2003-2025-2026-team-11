package com.example.corefood;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefood.R;
import com.example.corefood.DatabaseHelper;
import com.example.corefood.ExerciseLogTable;
import com.example.corefood.FoodLogTable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ChatBotActivity extends AppCompatActivity {

    private TextView tvContext;
    private TextView tvTranscript;
    private EditText etMessage;
    private DatabaseHelper dbHelper;

    // Hardcoded test user email. In a real app, this would come from a login session.
    private final String TEST_USER_EMAIL = "test@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        dbHelper = new DatabaseHelper(this);

        tvContext = findViewById(R.id.tvContext);
        tvTranscript = findViewById(R.id.tvChatTranscript);
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        //Dashboard Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.ai_menu);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.ai_menu) {
                return true;
            } else if (itemId == R.id.nav_main) {
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
            } else if (itemId == R.id.nav_calories) {
                startActivity(new Intent(this, CaloriesActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("OPEN_SETTINGS", true);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });

        refreshContext();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshContext();
    }

    private void refreshContext() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, TEST_USER_EMAIL);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, TEST_USER_EMAIL);
        int net = consumed - burned;

        tvContext.setText("Today: Consumed " + consumed + " kcal • Burned " + burned + " kcal • Net " + net + " kcal");
    }

    private void sendMessage() {
        String userMsg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(userMsg)) return;

        appendLine("You: " + userMsg);
        etMessage.setText("");

        String aiReply = generatePrototypeReply(userMsg);
        appendLine("AI: " + aiReply);
    }

    private void appendLine(String line) {
        tvTranscript.append(line + "\n\n");
    }

    private String generatePrototypeReply(String userMsg) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, TEST_USER_EMAIL);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, TEST_USER_EMAIL);
        int net = consumed - burned;

        String lower = userMsg.toLowerCase();

        // Basic topic detection
        if (lower.contains("diet") || lower.contains("food") || lower.contains("eat")) {
            if (net > 2000) {
                return "Your net intake looks quite high today. If your goal is fat loss, you could reduce portion sizes or swap one meal for something lighter (e.g., lean protein + vegetables).";
            } else if (net > 0) {
                return "You’re currently in a net surplus today. If you want to maintain, try balancing with a lighter dinner or a higher-protein snack.";
            } else {
                return "You’re currently in a net deficit today. Make sure you’re still eating enough protein and staying hydrated.";
            }
        }

        if (lower.contains("exercise") || lower.contains("workout") || lower.contains("burn")) {
            if (burned < 200) {
                return "If you have time, a short walk or a light workout could help increase your burned calories for today.";
            } else {
                return "Nice work staying active today. If you’re training again, consider stretching or a lighter session for recovery.";
            }
        }

        if (lower.contains("calorie") || lower.contains("deficit") || lower.contains("surplus")) {
            if (net > 0) {
                return "Based on today’s logs, you’re in a net surplus of about " + net + " kcal (consumed minus burned).";
            } else if (net < 0) {
                return "Based on today’s logs, you’re in a net deficit of about " + Math.abs(net) + " kcal (burned more than you consumed).";
            } else {
                return "Right now your net balance is roughly even (consumed and burned are similar).";
            }
        }

        // Default helpful reply
        return "I can help with food, exercise, and calorie balance. Try asking: “How can I reduce calories today?” or “What workout should I do next?”";
    }
}