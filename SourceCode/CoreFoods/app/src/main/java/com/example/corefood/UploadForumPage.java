package com.example.corefood;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UploadForumPage extends AppCompatActivity {

    private String selectedCategory = "Progress";
    private View lastSelectedIndicator;
    private EditText editCaption;
    private ProgressBar progressBar;
    private LinearLayout trainingContainer;
    private TextView inputHeader;
    private EditText editTrainingTitle, editTrainingDesc, editTrainingScheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_forum);

        editCaption = findViewById(R.id.edit_caption);
        progressBar = findViewById(R.id.progress_bar);
        trainingContainer = findViewById(R.id.training_container);
        inputHeader = findViewById(R.id.text_input_header);
        editTrainingTitle = findViewById(R.id.edit_training_title);
        editTrainingDesc = findViewById(R.id.edit_training_description);
        editTrainingScheme = findViewById(R.id.edit_training_scheme);

        //Bottom Nav Bar
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_forum);

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

        //Back button
        findViewById(R.id.button_back).setOnClickListener(v -> startActivity(new Intent(this, ForumPage.class)));

        //Category Selector
        setupCategory(R.id.row_progress, "Progress", "Share your fitness milestones", true);
        setupCategory(R.id.row_questions, "Questions", "Ask the community for advice", false);
        setupCategory(R.id.row_training, "Training", "Share workout or diet schemes", false);

        findViewById(R.id.button_post).setOnClickListener(v -> {
            uploadPost();
        });

        updateTrainingHint();
    }

    private void setupCategory(int rowId, String name, String description, boolean isDefault) {
        View row = findViewById(rowId);
        TextView nameTxt = row.findViewById(R.id.category_name);
        TextView descTxt = row.findViewById(R.id.category_description);
        View indicator = row.findViewById(R.id.selection_indicator);

        nameTxt.setText(name);
        descTxt.setText(description);

        if (isDefault) {
            indicator.setSelected(true);
            lastSelectedIndicator = indicator;
            selectedCategory = name;
        }

        row.setOnClickListener(v -> {
            //Check if the user has trainer status
            if (name.equalsIgnoreCase("Training")) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                            .addOnSuccessListener(doc -> {
                                boolean isTrainer = doc.exists() && Boolean.TRUE.equals(doc.getBoolean("isTrainer"));
                                if (isTrainer) {
                                    applyCategorySelection(indicator, name);
                                } else {
                                    Toast.makeText(this, "Only Trainers can post in this section", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else {
                applyCategorySelection(indicator, name);
            }
        });
    }

    //Changes UI
    private void applyCategorySelection(View indicator, String name) {
        if (lastSelectedIndicator != null) lastSelectedIndicator.setSelected(false);
        indicator.setSelected(true);
        lastSelectedIndicator = indicator;
        selectedCategory = name;

        //Toggle training fields
        if (name.equalsIgnoreCase("Training")) {
            //Hide the standard header and show training inputs
            inputHeader.setVisibility(View.GONE);
            trainingContainer.setVisibility(View.VISIBLE);
            editCaption.setVisibility(View.GONE);
        } else {
            //Show standard header and caption
            inputHeader.setVisibility(View.VISIBLE);
            inputHeader.setText("Add Description");
            trainingContainer.setVisibility(View.GONE);
            editCaption.setVisibility(View.VISIBLE);
        }
    }

    //Set hint based on trainerType in Firestore
    private void updateTrainingHint() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String type = doc.getString("trainerType");
                        if (type != null && type.toLowerCase().contains("diet")) {
                            editTrainingScheme.setHint("Break down the meal plan...");
                        } else {
                            editTrainingScheme.setHint("Break down the workout plan...");
                        }
                    }
                });
    }

    //Push post to Firebase
    private void uploadPost() {
        String finalCaption;

        if (selectedCategory.equalsIgnoreCase("Training")) {
            finalCaption = editTrainingTitle.getText().toString().trim();
            if (finalCaption.isEmpty()) {
                Toast.makeText(this, "Please enter a scheme title", Toast.LENGTH_SHORT).show();
                return;
            }

            if (editTrainingDesc.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter a short description", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            finalCaption = editCaption.getText().toString().trim();
            if (finalCaption.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        saveToFirestore(finalCaption);
    }

    private void saveToFirestore(String caption) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Get the user's name from Firebase
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            String fName = "Guest";
            String lName = "User";
            boolean isTrainer = false;
            String type = "Member";

            if (documentSnapshot.exists()) {
                fName = documentSnapshot.getString("firstName");
                lName = documentSnapshot.getString("lastName");

                if (documentSnapshot.contains("isTrainer")) {
                    isTrainer = Boolean.TRUE.equals(documentSnapshot.getBoolean("isTrainer"));
                }
                if (documentSnapshot.contains("trainerType")) {
                    type = documentSnapshot.getString("trainerType");
                }
            }

            //Check the trainer status again
            if (selectedCategory.equalsIgnoreCase("Training") && !isTrainer) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Trainer status required to post here", Toast.LENGTH_SHORT).show();
                return;
            }

            //Create the post
            Map<String, Object> postData = new HashMap<>();
            postData.put("firstName", fName);
            postData.put("lastName", lName);
            postData.put("caption", caption);
            postData.put("category", selectedCategory);
            postData.put("timestamp", System.currentTimeMillis());
            postData.put("isTrainer", isTrainer);
            postData.put("trainerType", type);
            postData.put("likeCount", 0);
            postData.put("userId", uid); //Keep track of who has posted (good practice)

            //Fields for training option
            if (selectedCategory.equalsIgnoreCase("Training")) {
                postData.put("trainingDescription", editTrainingDesc.getText().toString().trim());
                postData.put("trainingScheme", editTrainingScheme.getText().toString().trim());
            }

            db.collection("posts").add(postData)
                    .addOnSuccessListener(doc -> {
                        startActivity(new Intent(this, ForumPage.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                    });

            //Error Handler
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user profile", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        });
    }
}