package com.example.firestoredatabasetest;

public class ExerciseLog {

    // Variables for storing data
    private String EL_EXERCISE_TYPE, El_INTENSITY, EL_DURATION_MINS, EL_CALORIES_BURNED, EL_TIME, EL_NOTES;

    public ExerciseLog() {
        // Empty constructor required for Firebase
    }

    // constructor for variables
    public ExerciseLog(String COL_EXERCISE_TYPE, String COL_INTENSITY, String COL_DURATION_MINS, String COL_CALORIES_BURNED, String COL_TIME, String COL_NOTES) {
        this.EL_EXERCISE_TYPE = COL_EXERCISE_TYPE;
        this.El_INTENSITY = COL_INTENSITY;
        this.EL_DURATION_MINS = COL_DURATION_MINS;
        this.EL_CALORIES_BURNED = COL_CALORIES_BURNED;
        this.EL_TIME = COL_TIME;
        this.EL_NOTES = COL_NOTES;
    }

    public String getEL_EXERCISE_TYPE() { return EL_EXERCISE_TYPE; }
    public String getEl_INTENSITY() { return El_INTENSITY; }
    public String getEL_DURATION_MINS() { return EL_DURATION_MINS; }
    public String getEL_CALORIES_BURNED() { return EL_CALORIES_BURNED; }
    public String getEL_TIME() { return EL_TIME; }
    public String getEL_NOTES() { return EL_NOTES; }

    public void setEL_EXERCISE_TYPE(String EL_EXERCISE_TYPE) { this.EL_EXERCISE_TYPE = EL_EXERCISE_TYPE; }
    public void setEl_INTENSITY(String El_INTENSITY) { this.El_INTENSITY = El_INTENSITY; }
    public void setEL_DURATION_MINS(String EL_DURATION_MINS) { this.EL_DURATION_MINS = EL_DURATION_MINS; }
    public void setEL_CALORIES_BURNED(String EL_CALORIES_BURNED) { this.EL_CALORIES_BURNED = EL_CALORIES_BURNED; }
    public void setEL_TIME(String EL_TIME) { this.EL_TIME = EL_TIME; }
    public void setEL_NOTES(String EL_NOTES) { this.EL_NOTES = EL_NOTES; }
}


