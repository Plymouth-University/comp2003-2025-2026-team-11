package com.example.firebaseproject;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class TrainerInstructionsPage extends AppCompatActivity {

    private boolean isFavourited = false;
    private boolean isLiked = false;
    private int likeCount = 0;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId = FirebaseAuth.getInstance().getUid();
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer_instructions);

        //Initialise Views
        TextView tvTitle = findViewById(R.id.detail_title);
        TextView tvTrainerName = findViewById(R.id.detail_trainer_name);
        TextView tvTrainerType = findViewById(R.id.detail_trainer_type);
        TextView tvScheme = findViewById(R.id.detail_scheme);
        TextView tvLikeCount = findViewById(R.id.text_like_count);

        ImageButton btnBack = findViewById(R.id.button_back);
        ImageButton btnFavourite = findViewById(R.id.button_favourite);
        ImageButton btnLike = findViewById(R.id.button_like);

        //Get data from Intent
        postId = getIntent().getStringExtra("postId");

        //Fetch the data from Firestore using the postId
        if (postId != null) {
            db.collection("posts").document(postId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            //Extract strings from Firestore
                            String title = documentSnapshot.getString("caption");
                            String fName = documentSnapshot.getString("firstName");
                            String lName = documentSnapshot.getString("lastName");
                            String trainerType = documentSnapshot.getString("trainerType");

                            //Decide what to display
                            String trainingScheme = documentSnapshot.getString("trainingScheme");
                            String dietScheme = documentSnapshot.getString("dietScheme");
                            String finalScheme = (trainingScheme != null) ? trainingScheme : dietScheme;

                            //Set data to views
                            tvTitle.setText(title);
                            tvTrainerName.setText(fName + " " + lName);
                            tvTrainerType.setText(trainerType);
                            tvScheme.setText(finalScheme);
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show());
        }

        //Check if user has already liked or favourited this post
        if (currentUserId != null && postId != null) {
            checkFirebaseStatus(btnLike, btnFavourite);
        }

        //Back Button
        btnBack.setOnClickListener(v -> finish());

        //Favourite Star
        btnFavourite.setOnClickListener(v -> {
            isFavourited = !isFavourited;

            DocumentReference favRef = db.collection("users").document(currentUserId)
                    .collection("favourites").document(postId);

            if (isFavourited) {
                btnFavourite.setImageResource(android.R.drawable.btn_star_big_on);
                btnFavourite.setColorFilter(Color.parseColor("#03DAC5"));

                //Save to Firestore Favourites
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", FieldValue.serverTimestamp());
                data.put("postId", postId);
                favRef.set(data);
            } else {
                btnFavourite.setImageResource(android.R.drawable.btn_star_big_off);
                btnFavourite.setColorFilter(Color.WHITE);

                //Remove from Firestore Favourites
                favRef.delete();
            }

            //Animation for the star
            btnFavourite.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100).withEndAction(() ->
                    btnFavourite.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100));
        });

        //Like Button
        btnLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            DocumentReference postRef = db.collection("posts").document(postId);
            DocumentReference userLikeRef = postRef.collection("user_likes").document(currentUserId);

            if (isLiked) {
                btnLike.setColorFilter(Color.parseColor("#03DAC5"));

                //Increment likeCount
                postRef.update("likeCount", FieldValue.increment(1));
                Map<String, Object> likeData = new HashMap<>();
                likeData.put("likedAt", FieldValue.serverTimestamp());
                userLikeRef.set(likeData);
            } else {
                btnLike.setColorFilter(Color.WHITE);

                //Decrement likeCount
                postRef.update("likeCount", FieldValue.increment(-1));
                userLikeRef.delete();
            }

            //Animation for the heart
            btnLike.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(() ->
                    btnLike.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100));
        });

        //Listener for the like count
        if (postId != null) {
            db.collection("posts").document(postId).addSnapshotListener((snapshot, e) -> {
                if (snapshot != null && snapshot.exists()) {
                    Long count = snapshot.getLong("likeCount");
                    if (count != null) {
                        likeCount = count.intValue();
                        tvLikeCount.setText(String.valueOf(likeCount));
                    }
                }
            });
        }
    }

    private void checkFirebaseStatus(ImageButton btnLike, ImageButton btnFav) {
        //Check Liked Status
        db.collection("posts").document(postId).collection("user_likes").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        isLiked = true;
                        btnLike.setColorFilter(Color.parseColor("#03DAC5"));
                    }
                });

        //Check Favourite Status
        db.collection("users").document(currentUserId).collection("favourites").document(postId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        isFavourited = true;
                        btnFav.setImageResource(android.R.drawable.btn_star_big_on);
                        btnFav.setColorFilter(Color.parseColor("#03DAC5"));
                    }
                });
    }
}