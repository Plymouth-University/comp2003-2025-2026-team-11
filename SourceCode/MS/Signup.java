package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button signupButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialise views
        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailEditText);
        passwordInput = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.progressBar);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    //Create account
    private void createAccount() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        //Input requirements
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        if (!isPasswordValid(password)) {
            return;
        }

        //Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        signupButton.setEnabled(false);

        //Create the user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //Send verification email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            sendVerification(user);
                        }
                    } else {
                        //Sign up fail
                        progressBar.setVisibility(View.GONE);
                        signupButton.setEnabled(true);
                        Toast.makeText(Signup.this, "Sign up failed " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Password requirements
    private boolean isPasswordValid(String password) {
        //Password Length
        if (password.length() < 8) {
            passwordInput.setError("Password must be at least 8 characters");
            return false;
        }

        //Uppercase letter
        if(!Pattern.compile("[A-Z]").matcher(password).find()) {
            passwordInput.setError("Password must contain at least one uppercase letter");
            return false;
        }

        //Lowercase letter
        if(!Pattern.compile("[a-z]").matcher(password).find()) {
            passwordInput.setError("Password must contain at least one lowercase letter");
            return false;
        }

        //Number
        if(!Pattern.compile("[0-9]").matcher(password).find()) {
            passwordInput.setError("Password must contain at least one number");
            return false;
        }

        //Special character
        if(!Pattern.compile("[!@#$%^&*?_=]").matcher(password).find()) {
            passwordInput.setError("Password must contain at least one special character");
            return false;
        }
        return true;
    }

    //Send Email
    private void sendVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    signupButton.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(Signup.this, "Verification email sent", Toast.LENGTH_LONG).show();

                        //Sign out until verification
                        mAuth.signOut();

                        //Go to the login page
                        Intent intent = new Intent(Signup.this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Signup.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}