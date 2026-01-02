package com.example.corefoodsdatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NutritionTable {

    public static final String TABLE_NAME = "Nutrition";

    public static final String COL_FOOD_ID = "FoodID";
    public static final String COL_CALORIES = "CalorieInfo";
    public static final String COL_PROTEIN = "Protein";
    public static final String COL_CARBS = "Carbs";
    public static final String COL_FAT = "Fat";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_FOOD_ID + " INTEGER PRIMARY KEY," +
                    COL_CALORIES + " INTEGER," +
                    COL_PROTEIN + " REAL," +
                    COL_CARBS + " REAL," +
                    COL_FAT + " REAL," +
                    "FOREIGN KEY (" + COL_FOOD_ID + ") REFERENCES Food(FoodID)" +
                    ");";

    public static long insert(SQLiteDatabase db, int foodId, int calories,
                              double protein, double carbs, double fat) {
        ContentValues values = new ContentValues();
        values.put(COL_FOOD_ID, foodId);
        values.put(COL_CALORIES, calories);
        values.put(COL_PROTEIN, protein);
        values.put(COL_CARBS, carbs);
        values.put(COL_FAT, fat);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor get(SQLiteDatabase db, int foodId) {
        return db.query(TABLE_NAME, null, COL_FOOD_ID + "=?",
                new String[]{String.valueOf(foodId)}, null, null, null);
    }
}

