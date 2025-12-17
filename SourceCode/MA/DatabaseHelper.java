package com.example.corefoodsdatabase;

import android.content.Context;
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
        db.execSQL(UserPreferencesTable.CREATE_TABLE);
        db.execSQL(ActivityTable.CREATE_TABLE);
        db.execSQL(FoodTable.CREATE_TABLE);
        db.execSQL(NutritionTable.CREATE_TABLE);
        db.execSQL(FoodIntakeTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS FoodIntake");
        db.execSQL("DROP TABLE IF EXISTS Nutrition");
        db.execSQL("DROP TABLE IF EXISTS Activity");
        db.execSQL("DROP TABLE IF EXISTS UserPreferences");
        db.execSQL("DROP TABLE IF EXISTS Food");
        db.execSQL("DROP TABLE IF EXISTS User");
        onCreate(db);
    }
}