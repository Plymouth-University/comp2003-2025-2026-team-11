package com.example.corefoodsprototype.ui;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;
import com.example.corefoodsprototype.data.DatabaseHelper;
import com.example.corefoodsprototype.data.ExerciseLogTable;
import com.example.corefoodsprototype.data.FoodLogTable;
import com.example.corefoodsprototype.data.UserTable;

public class SettingsActivity extends AppCompatActivity {


    private DatabaseHelper dbHelper;
    // Hardcoded test user email. In a real app, this would come from a login session.
    private final String TEST_USER_EMAIL = "test@example.com";

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
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            // Delete all food and exercise logs for the current user
            FoodLogTable.deleteAllLogsForUser(db, TEST_USER_EMAIL);
            ExerciseLogTable.deleteAllLogsForUser(db, TEST_USER_EMAIL);
            Toast.makeText(this, "Today’s data cleared.", Toast.LENGTH_SHORT).show();
        });

        btnDeleteAccount.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete account")
                        .setMessage("This will remove all local prototype data. Are you sure?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            // Delete the user from the UserTable. Cascading delete will handle the rest.
                            UserTable.delete(db, TEST_USER_EMAIL);
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }
}
