package com.example.corefood;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserSessionManager {

    private UserSessionManager() {
        // Utility class
    }

    public static String getCurrentUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            return null;
        }

        return user.getEmail().trim().toLowerCase();
    }

    public static boolean isUserLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty();
    }
}