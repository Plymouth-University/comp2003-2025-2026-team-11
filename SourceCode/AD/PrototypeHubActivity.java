package com.example.corefoodsprototype.ui;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;

public class PrototypeHubActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prototype_hub);

        Button btnMainSection = findViewById(R.id.btnMainSection);
        Button btnChatBot = findViewById(R.id.btnChatBot);
        Button btnSettings = findViewById(R.id.btnSettings);

        btnMainSection.setOnClickListener(v ->
                startActivity(new Intent(this, MainSectionActivity.class)));

        btnChatBot.setOnClickListener(v ->
                startActivity(new Intent(this, ChatBotActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
