package com.example.firebaseproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfilePage extends AppCompatActivity {

    private TextView tvFullName, tvTrainerStatus;
    private LinearLayout containerPosts, containerSchemes, containerWorkout, containerDiets;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Check if user is authenticated; if not, exit the activity
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            finish();
            return;
        }

        tvFullName = findViewById(R.id.tv_full_name);
        tvTrainerStatus = findViewById(R.id.tv_trainer_status);
        containerPosts = findViewById(R.id.container_posts);
        containerSchemes = findViewById(R.id.container_schemes);
        containerWorkout = findViewById(R.id.container_workout);
        containerDiets = findViewById(R.id.container_diets);
        ImageButton btnSettings = findViewById(R.id.btn_settings);

        //Fetch user profile data from Firestore 'users' collection
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Display user's name and trainer status
                        tvFullName.setText(doc.getString("firstName") + " " + doc.getString("lastName"));
                        Boolean isTrainer = doc.getBoolean("isTrainer");
                        tvTrainerStatus.setText(isTrainer != null && isTrainer ? "Certified Trainer" : "Member");

                        //Load personal posts and saved items
                        loadMyPosts();
                        loadSavedSchemes();
                    }
                });

        //Take the user to the settings page
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsPage.class)));
    }

    private void loadMyPosts() {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Primary sorting via Firestore
                .get()
                .addOnSuccessListener(snapshots -> {
                    containerPosts.removeAllViews();
                    containerSchemes.removeAllViews();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        renderPostRow(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    //Fallback if code breaks
                    db.collection("posts").whereEqualTo("userId", userId).get().addOnSuccessListener(snapshots -> {
                        containerPosts.removeAllViews();
                        containerSchemes.removeAllViews();

                        List<DocumentSnapshot> list = new ArrayList<>(snapshots.getDocuments());

                        // Manual descending sort based on timestamp
                        Collections.sort(list, (d1, d2) -> {
                            Long t1 = d1.getLong("timestamp");
                            Long t2 = d2.getLong("timestamp");
                            if (t1 == null) t1 = 0L;
                            if (t2 == null) t2 = 0L;
                            return t2.compareTo(t1); // Newest first
                        });

                        for (DocumentSnapshot doc : list) {
                            renderPostRow(doc);
                        }
                    });
                });
    }

    private void renderPostRow(DocumentSnapshot doc) {
        String title = doc.getString("caption");
        String category = doc.getString("category");
        Object rawTimestamp = doc.get("timestamp");
        if (category == null) category = "General";

        //Sort into the forum container
        if (category.equalsIgnoreCase("Progress") || category.equalsIgnoreCase("Questions")) {
            addSimpleRow(containerPosts, title, category, doc.getId(), false, rawTimestamp);
        } else {
            //Sort into the training container
            String displayLabel = category.equalsIgnoreCase("Training") ? "Workout" : category;
            addSimpleRow(containerSchemes, title, displayLabel, doc.getId(), true, rawTimestamp);
        }
    }

    private void loadSavedSchemes() {
        db.collection("users").document(userId).collection("favourites")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    containerWorkout.removeAllViews();
                    containerDiets.removeAllViews();
                    for (QueryDocumentSnapshot favDoc : snapshots) {
                        String id = favDoc.getString("postId");
                        if (id != null) {
                            //Fetch the post data using the ID stored in favourites
                            db.collection("posts").document(id).get().addOnSuccessListener(postDoc -> {
                                if (postDoc.exists()) {
                                    String cat = postDoc.getString("category");
                                    Object ts = postDoc.get("timestamp");
                                    //Separate saved items into Workout and Diet containers
                                    if ("Nutritions".equalsIgnoreCase(cat) || "Diet".equalsIgnoreCase(cat)) {
                                        addSimpleRow(containerDiets, postDoc.getString("caption"), "Nutrition", id, true, ts);
                                    } else {
                                        addSimpleRow(containerWorkout, postDoc.getString("caption"), "Workout", id, true, ts);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void addSimpleRow(LinearLayout container, String title, String subtitle, String postId, boolean isClickable, Object rawTimestamp) {
        //Main containers
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(30, 40, 30, 40);

        //Headers
        RelativeLayout headerLayout = new RelativeLayout(this);
        headerLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        //Post Title
        TextView t1 = new TextView(this);
        t1.setText(title);
        t1.setTextColor(Color.WHITE);
        t1.setTextSize(17);
        t1.setTypeface(null, android.graphics.Typeface.BOLD);

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        t1.setLayoutParams(titleParams);

        //Date Format
        TextView timestampTv = new TextView(this);
        if (rawTimestamp != null) {
            Date date;

            if (rawTimestamp instanceof com.google.firebase.Timestamp) {
                date = ((com.google.firebase.Timestamp) rawTimestamp).toDate();
            } else {
                date = new Date((Long) rawTimestamp);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM, h:mm a", Locale.getDefault());
            String formattedDate = sdf.format(date).replace("AM", "am").replace("PM", "pm");
            timestampTv.setText(formattedDate);
        }

        timestampTv.setTextColor(Color.parseColor("#999797"));
        timestampTv.setTextSize(12);

        RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        dateParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        timestampTv.setLayoutParams(dateParams);

        headerLayout.addView(t1);
        headerLayout.addView(timestampTv);

        //Sub-labels
        TextView t2 = new TextView(this);
        t2.setText(subtitle);
        t2.setTextColor(Color.parseColor("#03DAC5"));
        t2.setTextSize(13);
        t2.setPadding(0, 8, 0, 0);

        row.addView(headerLayout);
        row.addView(t2);

        //Take user to the instructions page for training schemes
        if (isClickable) {
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(v -> {
                Intent i = new Intent(this, TrainerInstructionsPage.class);
                i.putExtra("postId", postId);
                startActivity(i);
            });
        }

        //Add visual divider between rows
        View div = new View(this);
        div.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
        div.setBackgroundColor(Color.parseColor("#333333"));

        container.addView(row);
        container.addView(div);
    }
}