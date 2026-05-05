package com.example.firebaseproject;

public class User {
    public String firstName, lastName, dob, profileImageBase64; // Changed to Base64 string
    public double weight, height;
    public boolean isTrainer;

    public User() {}

    public User(String firstName, String lastName, String dob, double weight, double height, boolean isTrainer, String profileImageBase64) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.weight = weight;
        this.height = height;
        this.isTrainer = isTrainer;
        this.profileImageBase64 = profileImageBase64;
    }
}