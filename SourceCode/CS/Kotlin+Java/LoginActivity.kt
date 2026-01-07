package com.example.comp2003_prototype

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.appcompat.app.AppCompatActivity
import com.example.comp2003_prototype.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen_layout)

        val emailField = findViewById<TextInputEditText>(R.id.edit_email)
        val passwordField = findViewById<TextInputEditText>(R.id.edit_password)
        val loginBtn = findViewById<Button>(R.id.button_login)
        val forgotPasswordBtn = findViewById<Button>(R.id.button_forgot_password)

        // Main Login Logic
        loginBtn.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            // At the minute this login is HARD CODED into the system so I can verify all of my screens work
            if (email == "test@test.com" && password == "password123") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // This is my popup for the Incorrect Password Indicator
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
            }
        }

        // Forgot Password Logic
        forgotPasswordBtn.setOnClickListener {
            // This is my popup for the Forgot Password indicator
            Toast.makeText(this, "Not Implemented", Toast.LENGTH_SHORT).show()
        }
    }
}