package com.example.corefoodsdatabase;

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
}
