package com.example.corefood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

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

        //Delete Account Button
        findViewById(R.id.btn_delete_account).setOnClickListener(v -> {
            showReauthDialog();
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

    private void showReauthDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);

        //Set Window Appearance
        android.text.SpannableString title = new android.text.SpannableString("DELETE ACCOUNT");
        title.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#03DAC5")), 0, title.length(), 0);
        title.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, title.length(), 0);
        builder.setTitle(title);

        android.text.SpannableString message = new android.text.SpannableString("Please enter your password to permanently delete your account.");
        message.setSpan(new android.text.style.ForegroundColorSpan(Color.WHITE), 0, message.length(), 0);
        builder.setMessage(message);

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password");
        passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordInput.setTextColor(Color.WHITE);
        passwordInput.setHintTextColor(Color.GRAY);

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50; params.rightMargin = 50;
        passwordInput.setLayoutParams(params);
        container.addView(passwordInput);
        builder.setView(container);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String password = passwordInput.getText().toString();
            if (!password.isEmpty()) {
                reauthenticateAndProcess(password);
            } else {
                Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Button Styling
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#FF1744"));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor("#FFFFFF"));

        // Window Appearance
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View decorView = dialog.getWindow().getDecorView();
        if (decorView instanceof ViewGroup) {
            View child = ((ViewGroup) decorView).getChildAt(0);
            child.setBackgroundColor(Color.parseColor("#1A1A1A"));
        }
    }

    private void reauthenticateAndProcess(String password) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    deleteUserAccount();
                } else {
                    Toast.makeText(this, "Verification failed. Check password.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteUserAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        WriteBatch batch = db.batch();

        //Delete main user profile
        batch.delete(db.collection("users").document(userId));

        //Delete the linked posts/food/exercises by the user
        List<com.google.android.gms.tasks.Task<QuerySnapshot>> tasks = new ArrayList<>();

        tasks.add(db.collection("posts").whereEqualTo("userId", userId).get());
        tasks.add(db.collection("FoodCollection").whereEqualTo("userId", userId).get());
        tasks.add(db.collection("ExerciseCollection").whereEqualTo("userId", userId).get());

        //Wait for all queries to complete, even if some return 0 results
        com.google.android.gms.tasks.Tasks.whenAllComplete(tasks).addOnCompleteListener(allTasks -> {
            for (com.google.android.gms.tasks.Task<?> t : tasks) {
                if (t.isSuccessful()) {
                    QuerySnapshot snap = (QuerySnapshot) t.getResult();
                    if (snap != null) {
                        for (DocumentSnapshot doc : snap) {
                            batch.delete(doc.getReference());
                        }
                    }
                }
            }

            //Commit the deletes
            executeBatchAndAuthDelete(batch, user);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error syncing with database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void executeBatchAndAuthDelete(WriteBatch batch, FirebaseUser user) {
        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //Delete the actual login credentials
                user.delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(this, "Your account has successfully been deleted.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, Login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        //Security check: If they haven't logged in recently, this will fail.
                        Toast.makeText(this, "Your Data has been wiped, but please re-login to delete account credentials.", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Failed to delete associated data.", Toast.LENGTH_SHORT).show();
            }
        });
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