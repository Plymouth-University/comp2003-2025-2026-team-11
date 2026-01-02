package com.example.corefoodsdatabase;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ActivityTable {

    public static final String TABLE_NAME = "Activity";

    public static final String COL_ID = "ActivityID";
    public static final String COL_EMAIL = "Email";
    public static final String COL_STEPS = "DailySteps";
    public static final String COL_TYPE = "ExerciseType";
    public static final String COL_INTENSITY = "ExerciseIntensity";
    public static final String COL_LENGTH = "ExerciseLength";
    public static final String COL_CALORIES = "CaloriesBurned";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_EMAIL + " TEXT," +
                    COL_STEPS + " INTEGER," +
                    COL_TYPE + " TEXT," +
                    COL_INTENSITY + " TEXT," +
                    COL_LENGTH + " INTEGER," +
                    COL_CALORIES + " INTEGER," +
                    "FOREIGN KEY (" + COL_EMAIL + ") REFERENCES User(Email)" +
                    ");";

    public static long insert(SQLiteDatabase db, String email, int steps,
                              String type, String intensity, int length, int calories) {
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_STEPS, steps);
        values.put(COL_TYPE, type);
        values.put(COL_INTENSITY, intensity);
        values.put(COL_LENGTH, length);
        values.put(COL_CALORIES, calories);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor getByUser(SQLiteDatabase db, String email) {
        return db.query(TABLE_NAME, null, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
    }

    public static int delete(SQLiteDatabase db, int activityId) {
        return db.delete(TABLE_NAME, COL_ID + "=?",
                new String[]{String.valueOf(activityId)});
    }
}

