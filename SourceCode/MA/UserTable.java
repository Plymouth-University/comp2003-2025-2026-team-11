package com.example.corefoodsdatabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserTable {

    public static final String TABLE_NAME = "User";

    public static final String COL_EMAIL = "Email";
    public static final String COL_PASSWORD = "Password";
    public static final String COL_NAME = "Name";
    public static final String COL_HEIGHT = "Height";
    public static final String COL_WEIGHT = "Weight";
    public static final String COL_DOB = "DateOfBirth";
    public static final String COL_GENDER = "Gender";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_EMAIL + " TEXT PRIMARY KEY," +
                    COL_PASSWORD + " TEXT," +
                    COL_NAME + " TEXT," +
                    COL_HEIGHT + " REAL," +
                    COL_WEIGHT + " REAL," +
                    COL_DOB + " TEXT," +
                    COL_GENDER + " TEXT" +
                    ");";

    // CREATE
    public static long insert(SQLiteDatabase db, String email, String password, String name,
                              double height, double weight, String dob, String gender) {
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_NAME, name);
        values.put(COL_HEIGHT, height);
        values.put(COL_WEIGHT, weight);
        values.put(COL_DOB, dob);
        values.put(COL_GENDER, gender);
        return db.insert(TABLE_NAME, null, values);
    }

    // READ
    public static Cursor getByEmail(SQLiteDatabase db, String email) {
        return db.query(TABLE_NAME, null, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
    }

    // UPDATE
    public static int update(SQLiteDatabase db, String email, ContentValues values) {
        return db.update(TABLE_NAME, values, COL_EMAIL + "=?",
                new String[]{email});
    }

    // DELETE
    public static int delete(SQLiteDatabase db, String email) {
        return db.delete(TABLE_NAME, COL_EMAIL + "=?",
                new String[]{email});
    }
}
