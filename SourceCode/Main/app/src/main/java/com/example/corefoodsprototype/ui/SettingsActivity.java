package com.example.corefoodsprototype.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;
import com.example.corefoodsprototype.data.PrototypeDataStore;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Switch switchTheme = findViewById(R.id.switchTheme);

        Button btnChangeEmail = findViewById(R.id.btnChangeEmail);
        Button btnChangePassword = findViewById(R.id.btnChangePassword);
        Button btnClearData = findViewById(R.id.btnClearData);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnChangeEmail.setOnClickListener(v ->
                Toast.makeText(this, "Email updated.", Toast.LENGTH_SHORT).show()
        );

        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(this, "Password updated.", Toast.LENGTH_SHORT).show()
        );

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this,
                        isChecked ? "Dark mode enabled." : "Light mode enabled.",
                        Toast.LENGTH_SHORT).show()
        );

        btnClearData.setOnClickListener(v -> {
            PrototypeDataStore.getInstance().resetDay();
            Toast.makeText(this, "Today’s data cleared.", Toast.LENGTH_SHORT).show();
        });

        btnDeleteAccount.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete account")
                        .setMessage("This will remove all local prototype data. Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            PrototypeDataStore.getInstance().resetDay();
                            Toast.makeText(this, "Account deleted.", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }
}
