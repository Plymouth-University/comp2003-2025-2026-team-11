package com.example.corefoodsdatabase;

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
}
