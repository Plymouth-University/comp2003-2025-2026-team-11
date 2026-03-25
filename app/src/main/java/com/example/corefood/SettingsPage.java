package com.example.corefood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SettingsPage extends AppCompatActivity {

    private EditText FirstName, LastName, Weight, Height;
    private Spinner spinnerDay, spinnerMonth, spinnerYear;
    private ImageView Profile;
    private ImageButton backbtn;
    private TextView BecomeTrainer;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String userId;
    private String base64ImageString = "";
    private boolean isTrainerLocal = false;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Bottom Nav Bar
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        Menu menu = bottomNav.getMenu();
        menu.setGroupCheckable(0, true, false);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_main) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_exercises) {
                startActivity(new Intent(this, ExerciseActivity.class));
                return true;
            } else if (itemId == R.id.nav_food) {
                startActivity(new Intent(this, FoodActivity.class));
                return true;
            } else if (itemId == R.id.nav_forum) {
                startActivity(new Intent(this, ForumPage.class));
                return true;
            } else if (itemId == R.id.ai_menu) {
                startActivity(new Intent(this, ChatBotActivity.class));
                return true;
            } else if (itemId == R.id.nav_calories) {
                startActivity(new Intent(this, CaloriesActivity.class));
                return true;
            }
            return false;
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }
        userId = currentUser.getUid();

        initViews();
        setupDateSpinners();
        loadUserData();

        //Back Button
        backbtn.setOnClickListener(v -> onBackPressed());

        //Save Button
        findViewById(R.id.btn_save_all).setOnClickListener(v -> saveSettings());

        //Profile Image Selection
        Profile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE);
        });

        //Logout Button
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        //Password Reset
        findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            String email = currentUser.getEmail();
            if (email != null) {
                mAuth.sendPasswordResetEmail(email).addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Reset email sent to " + email, Toast.LENGTH_SHORT).show());
            }
        });

        //Trainer Chooser
        BecomeTrainer.setOnClickListener(v -> {
            TrainerApplicationPage dialog = new TrainerApplicationPage();
            dialog.show(getSupportFragmentManager(), "TrainerDialog");
        });

        listenToTrainerStatus();
    }

    private void initViews() {
        FirstName = findViewById(R.id.et_first_name);
        LastName = findViewById(R.id.et_last_name);
        Weight = findViewById(R.id.et_weight);
        Height = findViewById(R.id.et_height);
        Profile = findViewById(R.id.iv_profile_setup);
        spinnerDay = findViewById(R.id.spinner_day);
        spinnerMonth = findViewById(R.id.spinner_month);
        spinnerYear = findViewById(R.id.spinner_year);
        BecomeTrainer = findViewById(R.id.tv_become_trainer);
        backbtn = findViewById(R.id.back_button);
    }

    private void listenToTrainerStatus() {
        db.collection("users").document(userId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                //Trainer Output
                Boolean isTrainer = snapshot.getBoolean("isTrainer");
                String type = snapshot.getString("trainerType");

                if (Boolean.TRUE.equals(isTrainer) && type != null) {
                    isTrainerLocal = true;
                    BecomeTrainer.setText("Specialty: " + type);
                    BecomeTrainer.setTextColor(Color.parseColor("#03DAC5"));
                }
            }
        });
    }

    private void loadUserData() {
        db.collection("users").document(userId).get().addOnSuccessListener(doc -> {
            //Show the data from the Firebase
            if (doc.exists()) {
                FirstName.setText(doc.getString("firstName"));
                LastName.setText(doc.getString("lastName"));

                if (doc.contains("weight")) Weight.setText(String.valueOf(doc.get("weight")));
                if (doc.contains("height")) Height.setText(String.valueOf(doc.get("height")));

                String encodedImage = doc.getString("profileImageBase64");
                if (encodedImage != null && !encodedImage.isEmpty()) {
                    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Profile.setImageBitmap(decodedByte);
                    base64ImageString = encodedImage;
                }

                String dob = doc.getString("dob");
                if (dob != null && dob.contains(" ")) {
                    String[] parts = dob.split(" ");
                    if (parts.length == 3) {
                        setSpinnerValue(spinnerDay, parts[0]);
                        setSpinnerValue(spinnerMonth, parts[1]);
                        setSpinnerValue(spinnerYear, parts[2]);
                    }
                }
            }
        });
    }

    private void saveSettings() {
        //Save the inputted data
        String wStr = Weight.getText().toString().trim();
        String hStr = Height.getText().toString().trim();

        String dobString = spinnerDay.getSelectedItem().toString() + " " +
                spinnerMonth.getSelectedItem().toString() + " " +
                spinnerYear.getSelectedItem().toString();

        User user = new User(
                FirstName.getText().toString().trim(),
                LastName.getText().toString().trim(),
                dobString,
                Double.parseDouble(wStr),
                Double.parseDouble(hStr),
                isTrainerLocal,
                base64ImageString
        );

        db.collection("users").document(userId).set(user, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show());
    }

    private String uriToBase64(Uri uri) {
        //Profile Picture functionality
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 400, 400, false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    private void setupDateSpinners() {
        //Date of Birth Selection
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) days.add(String.valueOf(i));
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<String> years = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= 1950; i--) years.add(String.valueOf(i));

        spinnerDay.setAdapter(createWhiteTextAdapter(days));
        spinnerMonth.setAdapter(createWhiteTextAdapter(Arrays.asList(months)));
        spinnerYear.setAdapter(createWhiteTextAdapter(years));
    }

    private ArrayAdapter<String> createWhiteTextAdapter(List<String> list) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.WHITE);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int pos = adapter.getPosition(value);
            if (pos >= 0) spinner.setSelection(pos);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            Profile.setImageURI(imageUri);
            base64ImageString = uriToBase64(imageUri);
        }
    }
}