package com.example.corefood;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ForumPage extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

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

        //Profile Picture Setup
        profileImage = findViewById(R.id.profile_image);
        findViewById(R.id.profile_image).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilePage.class)));

        loadUserProfileImage();

        //Category Groups
        RadioGroup categoryGroup = findViewById(R.id.category_group);
        categoryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                loadPosts(rb.getText().toString());
            }
        });

        loadPosts("All");

        findViewById(R.id.fab_add_post).setOnClickListener(v ->
                startActivity(new Intent(this, UploadForumPage.class)));
    }

    //Load the current user's profile image
    private void loadUserProfileImage() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.contains("profileImageBase64")) {
                String encodedImage = doc.getString("profileImageBase64");
                if (encodedImage != null && !encodedImage.isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        profileImage.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //Grab the post information from Firebase
    private void loadPosts(String category) {
        if (listenerRegistration != null) listenerRegistration.remove();

        Query query = db.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING);

        if (!category.equalsIgnoreCase("All")) {
            query = query.whereEqualTo("category", category);
        }

        listenerRegistration = query.addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;

            postList.clear();
            for (QueryDocumentSnapshot doc : value) {
                Post post = doc.toObject(Post.class);

                if (post != null) {
                    post.setPostId(doc.getId());

                    //Name Check
                    if (post.getFirstName() == null && doc.contains("username")) {
                        String full = doc.getString("username");
                        if (full != null) {
                            String[] parts = full.trim().split("\\s+");
                            post.setFirstName(parts[0]);
                            post.setLastName(parts.length > 1 ? parts[parts.length - 1] : "");
                        }
                    }
                    postList.add(post);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
