package com.example.corefoodsdatabase;

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
}
