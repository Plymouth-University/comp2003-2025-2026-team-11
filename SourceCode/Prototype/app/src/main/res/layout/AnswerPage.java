package com.example.firebaseproject;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AnswerPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);
        String postId = getIntent().getStringExtra("postId");
        EditText input = findViewById(R.id.edit_answer);
        Button submit = findViewById(R.id.btn_submit_answer);

        //Send the inputted answers to Firebase
        submit.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty()) {
                Map<String, Object> ans = new HashMap<>();
                ans.put("answerText", text);
                ans.put("timestamp", FieldValue.serverTimestamp());

                FirebaseFirestore.getInstance().collection("posts").document(postId)
                        .collection("answers").add(ans)
                        .addOnSuccessListener(doc -> finish())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show());
            }
        });
    }
}