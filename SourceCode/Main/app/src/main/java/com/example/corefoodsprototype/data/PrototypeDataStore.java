package com.example.corefoodsprototype.data;

public class PrototypeDataStore {

    private static PrototypeDataStore instance;

    private int totalCaloriesConsumed = 0;
    private int totalCaloriesBurned = 0;

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

    public void resetDay() {
        totalCaloriesConsumed = 0;
        totalCaloriesBurned = 0;
    }
}
