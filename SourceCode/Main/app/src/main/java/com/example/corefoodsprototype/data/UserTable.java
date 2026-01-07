package com.example.corefoodsprototype.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserTable {

    public static final String TABLE_NAME = "User";
    public static final String COL_EMAIL = "Email";
    public static final String COL_PASSWORD = "Password";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_EMAIL + " TEXT PRIMARY KEY," +
                    COL_PASSWORD + " TEXT" +
                    ");";

    // CREATE
    public static long insert(SQLiteDatabase db, String email, String password) {
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
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