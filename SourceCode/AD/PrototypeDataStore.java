package com.example.corefoodsprototype.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrototypeDataStore {

    private static PrototypeDataStore instance;

    private int totalCaloriesConsumed = 0;
    private int totalCaloriesBurned = 0;
    private final List<String> foodLogEntries = new ArrayList<>();
    private final List<String> exerciseLogEntries = new ArrayList<>();

    private PrototypeDataStore() {}

    public static synchronized PrototypeDataStore getInstance() {
        if (instance == null) instance = new PrototypeDataStore();
        return instance;
    }

    public int getTotalCaloriesConsumed() {
        return totalCaloriesConsumed;
    }

    public int getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void addCaloriesConsumed(int calories) {
        totalCaloriesConsumed += Math.max(0, calories);
    }

    public void addCaloriesBurned(int calories) {
        totalCaloriesBurned += Math.max(0, calories);
    }

    public void addFoodEntry(String entry) {
        foodLogEntries.add(entry);
    }

    public void addExerciseEntry(String entry) {
        exerciseLogEntries.add(entry);
    }

    public List<String> getFoodEntries() {
        return Collections.unmodifiableList(foodLogEntries);
    }

    public List<String> getExerciseEntries() {
        return Collections.unmodifiableList(exerciseLogEntries);
    }

    public void resetDay() {
        totalCaloriesConsumed = 0;
        totalCaloriesBurned = 0;
        foodLogEntries.clear();
        exerciseLogEntries.clear();
    }
}
