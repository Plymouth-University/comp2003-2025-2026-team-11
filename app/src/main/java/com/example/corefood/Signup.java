package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private Button signupButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Initialise views
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        emailInput = findViewById(R.id.edit_email);
        passwordInput = findViewById(R.id.edit_password);
        firstNameInput = findViewById(R.id.edit_first_name);
        lastNameInput = findViewById(R.id.edit_last_name);
        signupButton = findViewById(R.id.button_register);

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
        String fName = firstNameInput.getText().toString().trim();
        String lName = lastNameInput.getText().toString().trim();

        //Input requirements
        if (TextUtils.isEmpty(fName)) {
            firstNameInput.setError("First name is required");
            return;
        }

        if (TextUtils.isEmpty(lName)) {
            lastNameInput.setError("Last name is required");
            return;
        }

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

        signupButton.setEnabled(false);

        //Create the user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //Send verification email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user, fName, lName);
                        }
                    } else {
                        //Sign up fail
                        signupButton.setEnabled(true);
                        Toast.makeText(Signup.this, "Sign up failed " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    //Save user data to Firestore
    private void saveUserToFirestore(FirebaseUser user, String fName, String lName) {
        String uid = user.getUid();

        //Initial data
        Map<String, Object> userData = new HashMap<>();
        userData.put("firstName", fName);
        userData.put("lastName", lName);
        userData.put("email", user.getEmail());
        userData.put("isTrainer", false);
        userData.put("weight", 0.0);
        userData.put("height", 0.0);
        userData.put("dob", "1 Jan 2026");
        userData.put("profileImageBase64", "");

        //Create the users collection and the document
        db.collection("users").document(uid)
                .set(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendVerification(user);
                    } else {
                        signupButton.setEnabled(true);
                        Toast.makeText(Signup.this, "Firestore Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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