package com.example.corefood;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HealthDataManager {

    //private final DatabaseHelper dbHelper;
    private final FirebaseFirestore db;

    // Callback interface to handle async Firestore results
    public interface SummaryCallback {
        void onSummaryLoaded(int consumed, int burned);
        void onError(Exception e);
    }

    public HealthDataManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
    }

    public String getCurrentUserEmail() {
        return UserSessionManager.getCurrentUserEmail();
    }

    public boolean hasLoggedInUser() {
        return !TextUtils.isEmpty(getCurrentUserEmail());
    }

    public String getTodayDatePrefix() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**public CalorieSummary getTodaySummaryForCurrentUser() {
     String userEmail = getCurrentUserEmail();
     if (TextUtils.isEmpty(userEmail)) {
     return new CalorieSummary(0, 0);
     }

     return getTodaySummaryForUser(userEmail);
     }*/

    public void getTodaySummaryForUser(String userEmail, SummaryCallback callback) {
        if (TextUtils.isEmpty(userEmail)) {
            callback.onSummaryLoaded(0, 0);
            return;
        }

        String datePrefix = getTodayDatePrefix();
        AtomicInteger totalConsumed = new AtomicInteger(0);
        AtomicInteger totalBurned = new AtomicInteger(0);
        AtomicInteger tasksCompleted = new AtomicInteger(0);

        // 1. Fetch Food Data
        db.collection("FoodCollection")
                .whereEqualTo("fl_USER", userEmail) // Changed to lowercase based on your working snippet
                .get()
                .addOnSuccessListener(foodSnapshots -> {
                    int consumed = 0;
                    for (QueryDocumentSnapshot doc : foodSnapshots) {
                        // Extract time safely
                        String time = doc.getString("fl_TIME");
                        if (time == null) time = doc.getString("FL_TIME"); // Fallback for case sensitivity

                        if (time != null && time.startsWith(datePrefix)) {
                            // FIX: Using doc.get().toString() handles both Numbers and Strings in Firestore
                            Object calObj = doc.get("fl_CALORIES");
                            if (calObj == null) calObj = doc.get("FL_CALORIES"); // Fallback

                            if (calObj != null) {
                                try {
                                    consumed += Integer.parseInt(calObj.toString());
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                    totalConsumed.set(consumed);
                    checkProgress(tasksCompleted, totalConsumed, totalBurned, callback);
                })
                .addOnFailureListener(callback::onError);

        // 2. Fetch Exercise Data
        db.collection("ExerciseCollection")
                .whereEqualTo("el_USER", userEmail) // Changed to lowercase
                .get()
                .addOnSuccessListener(exerciseSnapshots -> {
                    int burned = 0;
                    for (QueryDocumentSnapshot doc : exerciseSnapshots) {
                        String time = doc.getString("el_TIME");
                        if (time == null) time = doc.getString("EL_TIME"); // Fallback

                        if (time != null && time.startsWith(datePrefix)) {
                            // FIX: Using doc.get().toString() to prevent null on Number fields
                            Object burnObj = doc.get("el_CALORIES_BURNED");
                            if (burnObj == null) burnObj = doc.get("EL_CALORIES_BURNED"); // Fallback

                            if (burnObj != null) {
                                try {
                                    burned += Integer.parseInt(burnObj.toString());
                                } catch (NumberFormatException ignored) {}
                            }
                        }
                    }
                    totalBurned.set(burned);
                    checkProgress(tasksCompleted, totalConsumed, totalBurned, callback);
                })
                .addOnFailureListener(callback::onError);
    }

    private void checkProgress(AtomicInteger tasks, AtomicInteger consumed, AtomicInteger burned, SummaryCallback callback) {
        if (tasks.incrementAndGet() == 2) {
            callback.onSummaryLoaded(consumed.get(), burned.get());
        }
    }

    /**public boolean hasAnyLocalDataForUser(String userEmail) {
     if (TextUtils.isEmpty(userEmail)) {
     return false;
     }

     SQLiteDatabase db = dbHelper.getReadableDatabase();
     int consumed = FoodLogTable.getTotalCaloriesForUser(db, userEmail);
     int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, userEmail);

     return consumed > 0 || burned > 0;
     }*/
}