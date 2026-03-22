package com.example.corefood;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HealthDataManager {

    private final DatabaseHelper dbHelper;

    public HealthDataManager(Context context) {
        this.dbHelper = new DatabaseHelper(context.getApplicationContext());
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

    public CalorieSummary getTodaySummaryForCurrentUser() {
        String userEmail = getCurrentUserEmail();
        if (TextUtils.isEmpty(userEmail)) {
            return new CalorieSummary(0, 0);
        }

        return getTodaySummaryForUser(userEmail);
    }

    public CalorieSummary getTodaySummaryForUser(String userEmail) {
        if (TextUtils.isEmpty(userEmail)) {
            return new CalorieSummary(0, 0);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String datePrefix = getTodayDatePrefix();

        int consumed = FoodLogTable.getTotalCaloriesForUserOnDate(db, userEmail, datePrefix);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUserOnDate(db, userEmail, datePrefix);

        return new CalorieSummary(consumed, burned);
    }

    public boolean hasAnyLocalDataForUser(String userEmail) {
        if (TextUtils.isEmpty(userEmail)) {
            return false;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int consumed = FoodLogTable.getTotalCaloriesForUser(db, userEmail);
        int burned = ExerciseLogTable.getTotalCaloriesBurnedForUser(db, userEmail);

        return consumed > 0 || burned > 0;
    }
}