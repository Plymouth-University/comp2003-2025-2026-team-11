package com.example.firebaseproject;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        //Category Groups
        RadioGroup categoryGroup = findViewById(R.id.category_group);
        categoryGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb != null) {
                loadPosts(rb.getText().toString());
            }
        });

        loadPosts("Progress");

        findViewById(R.id.fab_add_post).setOnClickListener(v ->
                startActivity(new Intent(this, UploadForumPage.class)));
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
                    post.setPostId(doc.getId()); // Needed for answers sub-collection

                    // --- SMART NAME CHECK ---
                    // Fixes "Guest U." by checking legacy 'username' field if firstName is null
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