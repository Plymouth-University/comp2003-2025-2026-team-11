package com.example.corefoodsdatabase;

public class FoodTable {

    public static final String TABLE_NAME = "Food";

    public static final String COL_ID = "FoodID";
    public static final String COL_NAME = "FoodName";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_NAME + " TEXT" +
                    ");";
}
