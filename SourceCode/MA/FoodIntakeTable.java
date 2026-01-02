package com.example.corefoodsdatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FoodIntakeTable {

    public static final String TABLE_NAME = "FoodIntake";

    public static final String COL_ID = "IntakeID";
    public static final String COL_EMAIL = "Email";
    public static final String COL_FOOD_ID = "FoodID";
    public static final String COL_CALORIC_INTAKE = "CaloricIntake";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_EMAIL + " TEXT," +
                    COL_FOOD_ID + " INTEGER," +
                    COL_CALORIC_INTAKE + " INTEGER," +
                    "FOREIGN KEY (" + COL_EMAIL + ") REFERENCES User(Email)," +
                    "FOREIGN KEY (" + COL_FOOD_ID + ") REFERENCES Food(FoodID)" +
                    ");";

    public static long insert(SQLiteDatabase db, String email, int foodId, int calories) {
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_FOOD_ID, foodId);
        values.put(COL_CALORIC_INTAKE, calories);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor getByUser(SQLiteDatabase db, String email) {
        return db.query(TABLE_NAME, null, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
    }

    public static int delete(SQLiteDatabase db, int intakeId) {
        return db.delete(TABLE_NAME, COL_ID + "=?",
                new String[]{String.valueOf(intakeId)});
    }
}

