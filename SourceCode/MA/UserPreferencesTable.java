package com.example.corefoodsdatabase;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserPreferencesTable {

    public static final String TABLE_NAME = "UserPreferences";

    public static final String COL_EMAIL = "Email";
    public static final String COL_TEXT_SIZE = "TextSize";
    public static final String COL_THEME = "Theme";
    public static final String COL_LIGHT_DARK = "LightDarkMode";
    public static final String COL_COLOR_BLIND = "ColourBlindMode";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_EMAIL + " TEXT PRIMARY KEY," +
                    COL_TEXT_SIZE + " TEXT," +
                    COL_THEME + " TEXT," +
                    COL_LIGHT_DARK + " TEXT," +
                    COL_COLOR_BLIND + " TEXT," +
                    "FOREIGN KEY (" + COL_EMAIL + ") REFERENCES User(Email)" +
                    ");";

    public static long insert(SQLiteDatabase db, String email, String textSize,
                              String theme, String mode, String colourBlind) {
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_TEXT_SIZE, textSize);
        values.put(COL_THEME, theme);
        values.put(COL_LIGHT_DARK, mode);
        values.put(COL_COLOR_BLIND, colourBlind);
        return db.insert(TABLE_NAME, null, values);
    }

    public static Cursor get(SQLiteDatabase db, String email) {
        return db.query(TABLE_NAME, null, COL_EMAIL + "=?",
                new String[]{email}, null, null, null);
    }

    public static int update(SQLiteDatabase db, String email, ContentValues values) {
        return db.update(TABLE_NAME, values, COL_EMAIL + "=?",
                new String[]{email});
    }

    public static int delete(SQLiteDatabase db, String email) {
        return db.delete(TABLE_NAME, COL_EMAIL + "=?",
                new String[]{email});
    }
}

