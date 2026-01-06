package com.example.corefoodsprototype.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;
import com.example.corefoodsprototype.data.DatabaseHelper;
import com.example.corefoodsprototype.data.ExerciseLogTable;
import com.example.corefoodsprototype.data.FoodLogTable;

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
