package com.example.firestoredatabasetest;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ViewExerciseLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<ExerciseLog> exerciseLogArrayList;
    private ExerciseLogAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_exercise_logs);

        recyclerView = findViewById(R.id.idRVExerciseLogs);
        db = FirebaseFirestore.getInstance();
        exerciseLogArrayList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExerciseLogAdapter(exerciseLogArrayList);
        recyclerView.setAdapter(adapter);

        loadExercisesFromFirestore();
    }

    private void loadExercisesFromFirestore() {
        db.collection("ExerciseCollection").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            // This matches the variables in your ExerciseLog.java
                            ExerciseLog log = d.toObject(ExerciseLog.class);
                            exerciseLogArrayList.add(log);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Fail to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}