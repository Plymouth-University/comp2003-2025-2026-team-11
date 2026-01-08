package com.example.comp2003_prototype

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.comp2003_prototype.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_create)

        val registerBtn = findViewById<Button>(R.id.button_register)

        registerBtn.setOnClickListener {
            Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
            finish() // This closes the screen and takes you back to Login after creating your account
        }
    }
}