package com.example.corefood;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatBotActivity extends AppCompatActivity {

    private static final String FALLBACK_TEST_EMAIL = "test@example.com";

    private TextView tvContext;
    private TextView tvTranscript;
    private EditText etMessage;
    private Button btnSend;
    private DatabaseHelper dbHelper;

    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        dbHelper = new DatabaseHelper(this);

        tvContext = findViewById(R.id.tvContext);
        tvTranscript = findViewById(R.id.tvChatTranscript);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        tvTranscript.setMovementMethod(new ScrollingMovementMethod());

        currentUserEmail = resolveBestUserEmail();
        setupBottomNavigation();
        refreshContext();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUserEmail = resolveBestUserEmail();
        refreshContext();
    }

    private void setupBottomNavigation() {
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
    }

    private String resolveBestUserEmail() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String firebaseEmail = firebaseUser != null ? firebaseUser.getEmail() : null;

        if (!TextUtils.isEmpty(firebaseEmail) && hasAnyLocalData(firebaseEmail)) {
            return firebaseEmail;
        }

        if (hasAnyLocalData(FALLBACK_TEST_EMAIL)) {
            Toast.makeText(this, "Using local test data for chatbot context", Toast.LENGTH_SHORT).show();
            return FALLBACK_TEST_EMAIL;
        }

        if (!TextUtils.isEmpty(firebaseEmail)) {
            return firebaseEmail;
        }

        return FALLBACK_TEST_EMAIL;
    }

    private boolean hasAnyLocalData(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, email);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, email);
        return consumed > 0 || burned > 0;
    }

    private void refreshContext() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, currentUserEmail);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, currentUserEmail);
        int net = consumed - burned;

        tvContext.setText("Today: Consumed " + consumed + " kcal • Burned " + burned + " kcal • Net " + net + " kcal");
    }

    private void sendMessage() {
        String userMsg = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(userMsg)) {
            return;
        }

        appendLine("You: " + userMsg);
        etMessage.setText("");
        setSendingState(true);

        String prompt = buildPrompt(userMsg);

        GeminiApiHelper.generateReply(prompt, new GeminiApiHelper.GeminiCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    appendLine("AI: " + reply);
                    setSendingState(false);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    String fallbackReply = generatePrototypeReply(userMsg);
                    appendLine("AI: " + fallbackReply);
                    setSendingState(false);

                    Toast.makeText(
                            ChatBotActivity.this,
                            "AI unavailable. Showing prototype response.",
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }
        });
    }

    private void setSendingState(boolean isSending) {
        btnSend.setEnabled(!isSending);
        btnSend.setText(isSending ? "Sending..." : "Send");
    }

    private void appendLine(String line) {
        tvTranscript.append(line + "\n\n");

        tvTranscript.post(() -> {
            if (tvTranscript.getLayout() == null) {
                return;
            }

            int scrollAmount = tvTranscript.getLayout().getLineTop(tvTranscript.getLineCount()) - tvTranscript.getHeight();

            if (scrollAmount > 0) {
                tvTranscript.scrollTo(0, scrollAmount);
            } else {
                tvTranscript.scrollTo(0, 0);
            }
        });
    }

    private String buildPrompt(String userMsg) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, currentUserEmail);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, currentUserEmail);
        int net = consumed - burned;

        String transcriptSnapshot = tvTranscript.getText().toString();
        if (transcriptSnapshot.length() > 800) {
            transcriptSnapshot = transcriptSnapshot.substring(transcriptSnapshot.length() - 800);
        }

        return "You are the CoreFoods chatbot, an AI assistant for a student fitness app prototype. "
                + "Help users with food, calorie balance, exercise, motivation, and general fitness guidance. "
                + "Use the provided user data when relevant. "
                + "Do not invent user facts that are not available. "
                + "Do not provide medical diagnosis or treatment advice. "
                + "If the topic becomes medical or risky, briefly say the user should consult a qualified professional. "
                + "Keep replies natural, useful, and reasonably concise for a mobile app.\n\n"

                + "User context:\n"
                + "- Calories consumed today: " + consumed + "\n"
                + "- Calories burned today: " + burned + "\n"
                + "- Net calories today: " + net + "\n\n"

                + "Recent chat:\n"
                + transcriptSnapshot + "\n\n"

                + "User question:\n"
                + userMsg;
    }

    private String generatePrototypeReply(String userMsg) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, currentUserEmail);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, currentUserEmail);
        int net = consumed - burned;

        String lower = userMsg.toLowerCase();

        if (lower.contains("diet") || lower.contains("food") || lower.contains("eat")) {
            if (net > 2000) {
                return "Your net intake looks quite high today. If your goal is fat loss, you could reduce portion sizes or choose a lighter meal with lean protein and vegetables.";
            } else if (net > 0) {
                return "You’re currently in a net surplus today. If your goal is maintenance, a lighter meal or higher-protein option could help balance the day.";
            } else {
                return "You’re currently in a net deficit today. Make sure you still get enough protein, fluids, and overall nutrition.";
            }
        }

        if (lower.contains("exercise") || lower.contains("workout") || lower.contains("burn")) {
            if (burned < 200) {
                return "If you have time, a short walk or light workout could help increase your calories burned today.";
            } else {
                return "Nice work staying active today. If you train again, consider recovery, mobility work, or a lighter session.";
            }
        }

        if (lower.contains("calorie") || lower.contains("deficit") || lower.contains("surplus")) {
            if (net > 0) {
                return "Based on today’s logs, you’re in a net surplus of about " + net + " kcal.";
            } else if (net < 0) {
                return "Based on today’s logs, you’re in a net deficit of about " + Math.abs(net) + " kcal.";
            } else {
                return "Right now your calorie balance is roughly even.";
            }
        }

        return "I can help with food, exercise, calorie balance, and general fitness guidance. This is informational support only and not medical advice.";
    }
}