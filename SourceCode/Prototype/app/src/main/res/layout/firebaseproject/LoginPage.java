package com.example.firebaseproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    private EditText emailField, passwordField;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        emailField = findViewById(R.id.login_email);
        passwordField = findViewById(R.id.login_password);
        Button loginBtn = findViewById(R.id.btn_login);
        TextView registerLink = findViewById(R.id.txt_register);
        TextView forgotPassword = findViewById(R.id.txt_forgot_password);

        // Standard Login
        loginBtn.setOnClickListener(v -> loginUser());

        // Quick Register (for testing)
        registerLink.setOnClickListener(v -> registerUser());

        // Forgot Password Logic
        forgotPassword.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginPage.this, "Enter your email to reset password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(LoginPage.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(LoginPage.this, "Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    startActivity(new Intent(LoginPage.this, ForumPage.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Email required & Password must be 6+ chars", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginPage.this, ForumPage.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Registration Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }
}