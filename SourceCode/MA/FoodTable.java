package com.example.corefoodsdatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FoodTable {

    public static final String TABLE_NAME = "Food";

    public static final String COL_ID = "FoodID";
    public static final String COL_NAME = "FoodName";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_NAME + " TEXT" +
                    ");";


    public static long insert(SQLiteDatabase db, String foodName) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, foodName);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor getAll(SQLiteDatabase db) {
        return db.query(TABLE_NAME, null, null, null, null, null, COL_NAME);
    }

    public static int delete(SQLiteDatabase db, int foodId) {
        return db.delete(TABLE_NAME, COL_ID + "=?",
                new String[]{String.valueOf(foodId)});
    }
}


