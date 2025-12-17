package com.example.corefoodsdatabase;

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
}
