package com.example.corefoodsprototype.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ExerciseLogTable {

    public static final String TABLE_NAME = "ExerciseLog";
    public static final String COL_ID = "ID";
    public static final String COL_USER_EMAIL = "UserEmail";
    public static final String COL_EXERCISE_TYPE = "ExerciseType";
    public static final String COL_INTENSITY = "Intensity";
    public static final String COL_DURATION_MINS = "DurationMins";
    public static final String COL_CALORIES_BURNED = "CaloriesBurned";
    public static final String COL_TIME = "Time";
    public static final String COL_NOTES = "Notes";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_EMAIL + " TEXT," +
                    COL_EXERCISE_TYPE + " TEXT NOT NULL," +
                    COL_INTENSITY + " TEXT," +
                    COL_DURATION_MINS + " INTEGER NOT NULL," +
                    COL_CALORIES_BURNED + " INTEGER," +
                    COL_TIME + " TEXT NOT NULL," +
                    COL_NOTES + " TEXT," +
                    "FOREIGN KEY (" + COL_USER_EMAIL + ") REFERENCES " +
                    UserTable.TABLE_NAME + "(" + UserTable.COL_EMAIL + ")" +
                    ");";

    public static long insert(SQLiteDatabase db, String userEmail, String exerciseType, String intensity,
                              int duration, int caloriesBurned, String time, String notes) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_EMAIL, userEmail);
        values.put(COL_EXERCISE_TYPE, exerciseType);
        values.put(COL_INTENSITY, intensity);
        values.put(COL_DURATION_MINS, duration);
        values.put(COL_CALORIES_BURNED, caloriesBurned);
        values.put(COL_TIME, time);
        values.put(COL_NOTES, notes);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor getLogsForUser(SQLiteDatabase db, String userEmail) {
        return db.query(TABLE_NAME, null, COL_USER_EMAIL + "=?",
                new String[]{userEmail}, null, null, COL_TIME + " DESC");
    }

    // New method to get total calories burned for a user
    public static int getTotalCaloriesBurnedForUser(SQLiteDatabase db, String userEmail) {
        int totalCalories = 0;
        String query = "SELECT SUM(" + COL_CALORIES_BURNED + ") FROM " + TABLE_NAME +
                " WHERE " + COL_USER_EMAIL + "=?";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});

        if (cursor != null && cursor.moveToFirst()) {
            totalCalories = cursor.getInt(0); // SUM() is the first column
            cursor.close();
        }
        return totalCalories;
    }

    // New method to delete all logs for a user
    public static void deleteAllLogsForUser(SQLiteDatabase db, String userEmail) {
        db.delete(TABLE_NAME, COL_USER_EMAIL + "=?", new String[]{userEmail});
    }
}