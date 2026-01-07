package com.example.corefoodsprototype.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "COREfoods.db";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UserTable.CREATE_TABLE);
        db.execSQL(FoodLogTable.CREATE_TABLE);
        db.execSQL(ExerciseLogTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS User");
        db.execSQL("DROP TABLE IF EXISTS FoodLog");
        db.execSQL("DROP TABLE IF EXISTS ExerciseLog");
        onCreate(db);
    }

    public void createSampleUserForTesting() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Use a specific email that you can reference throughout the app for testing
        String testEmail = "test@example.com";

        // Check if the user already exists to avoid inserting duplicates
        Cursor cursor = UserTable.getByEmail(db, testEmail);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return; // User already exists
        }
        if (cursor != null) {
            cursor.close();
        }

        // Insert the sample user
        UserTable.insert(db,
                testEmail,
                "password123" // Dummy password
        );
    }
}