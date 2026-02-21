package com.example.firestoredatabasetest;

public class FoodLog {

    // Variables for storing data
    private String FL_MEAL_TYPE, FL_MEAL_NAME, FL_CALORIES, FL_TIME, FL_NOTES;

    public FoodLog() {
        // Empty constructor required for Firebase
    }

    // constructor for variables
    public FoodLog(String COL_MEAL_TYPE, String COL_MEAL_NAME, String COL_CALORIES, String COL_TIME, String COL_NOTES) {
        this.FL_MEAL_TYPE = COL_MEAL_TYPE;
        this.FL_MEAL_NAME = COL_MEAL_NAME;
        this.FL_CALORIES = COL_CALORIES;
        this.FL_TIME = COL_TIME;
        this.FL_NOTES = COL_NOTES;
    }

    public String getFL_MEAL_TYPE() { return FL_MEAL_TYPE; }
    public String getFL_MEAL_NAME() { return FL_MEAL_NAME; }
    public String getFL_CALORIES() { return FL_CALORIES; }
    public String getFL_TIME() { return FL_TIME; }
    public String getFL_NOTES() { return FL_NOTES; }
}

