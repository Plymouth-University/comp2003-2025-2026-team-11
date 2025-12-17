package com.example.corefoodsdatabase;

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
}