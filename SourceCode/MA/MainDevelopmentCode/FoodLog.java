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

}
