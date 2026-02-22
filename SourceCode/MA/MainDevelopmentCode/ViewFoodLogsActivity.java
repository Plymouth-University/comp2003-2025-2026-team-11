package com.example.firestoredatabasetest;

import android.os.Bundle;
import android.widget.Toast;import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ViewFoodLogsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<FoodLog> foodLogArrayList;
    private FoodLogAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_food_logs); // Create a simple layout with a RecyclerView

        recyclerView = findViewById(R.id.idRVFoodLogs);
        db = FirebaseFirestore.getInstance();
        foodLogArrayList = new ArrayList<>();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FoodLogAdapter(foodLogArrayList);
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        db.collection("MealCollection").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot d : list) {
                            FoodLog log = d.toObject(FoodLog.class);
                            foodLogArrayList.add(log);
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}