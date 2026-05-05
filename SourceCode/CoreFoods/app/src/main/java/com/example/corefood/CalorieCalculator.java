package com.example.corefood;

public class CalorieCalculator {

    private CalorieCalculator() {
        // Utility class
    }

    public static int estimateExerciseCalories(String type, String intensity, int durationMins) {
        double baseRate;

        switch (type) {
            case "Running":
                baseRate = 10.0;
                break;
            case "Cycling":
                baseRate = 8.0;
                break;
            case "Swimming":
                baseRate = 9.0;
                break;
            case "Weight Training":
                baseRate = 6.0;
                break;
            case "HIIT":
                baseRate = 11.0;
                break;
            case "Yoga":
                baseRate = 4.0;
                break;
            case "Walking":
                baseRate = 5.0;
                break;
            default:
                baseRate = 6.0;
                break;
        }

        double intensityMultiplier;
        switch (intensity) {
            case "High":
                intensityMultiplier = 1.3;
                break;
            case "Low":
                intensityMultiplier = 0.8;
                break;
            default:
                intensityMultiplier = 1.0;
                break;
        }

        return (int) Math.round(baseRate * intensityMultiplier * durationMins);
    }
}