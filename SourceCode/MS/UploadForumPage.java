package com.example.firebaseproject;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_forum);

        editCaption = findViewById(R.id.edit_caption);
        progressBar = findViewById(R.id.progress_bar);

        //Back button
        findViewById(R.id.button_back).setOnClickListener(v -> finish());

        //Category Selector
        setupCategory(R.id.row_progress, "Progress", "Share your fitness milestones", true);
        setupCategory(R.id.row_questions, "Questions", "Ask the community for advice", false);
        setupCategory(R.id.row_tips, "Tips", "Share workout or diet secrets", false);

        findViewById(R.id.button_post).setOnClickListener(v -> uploadPost());
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
            if (lastSelectedIndicator != null) lastSelectedIndicator.setSelected(false);
            indicator.setSelected(true);
            lastSelectedIndicator = indicator;
            selectedCategory = name;
        });
    }

    //Push post to Firebase
    private void uploadPost() {
        String caption = editCaption.getText().toString().trim();
        if (caption.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveToFirestore(caption);
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

            if (documentSnapshot.exists()) {
                fName = documentSnapshot.getString("firstName");
                lName = documentSnapshot.getString("lastName");
            }

            //Create the post
            Map<String, Object> postData = new HashMap<>();
            postData.put("firstName", fName);
            postData.put("lastName", lName);
            postData.put("caption", caption);
            postData.put("category", selectedCategory);
            postData.put("timestamp", System.currentTimeMillis());
            postData.put("userId", uid); // Good practice to keep track of who posted

            db.collection("posts").add(postData)
                    .addOnSuccessListener(doc -> finish())
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