package com.example.firestoredatabasetest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ExerciseLogInputPage extends AppCompatActivity {

    private EditText ExerciseTypeEdt, ExerciseIntensityEdt, ExerciseDurationEdt, ExerciseCaloriesBurnedEdt, ExerciseTimeEdt, ExerciseNotesEdt;

    private Button submitExerciseBtn;

    private String ExerciseType, ExerciseIntensity, ExerciseDuration, ExerciseCaloriesBurned, ExerciseTime, ExerciseNotes;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_excercise_log_input_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        ExerciseTypeEdt = findViewById(R.id.idEdtExerciseType);
        ExerciseIntensityEdt = findViewById(R.id.idEdtExerciseIntensity);
        ExerciseDurationEdt = findViewById(R.id.idEdtDuration);
        ExerciseCaloriesBurnedEdt = findViewById(R.id.idEdtCaloriesBurned);
        ExerciseTimeEdt = findViewById(R.id.idEdtTime);
        ExerciseNotesEdt = findViewById(R.id.idEdtNotes);
        submitExerciseBtn = findViewById(R.id.idsubmitExerciseBtn);

        submitExerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting data from edittext fields.
                ExerciseType = ExerciseTypeEdt.getText().toString();
                ExerciseIntensity = ExerciseIntensityEdt.getText().toString();
                ExerciseDuration = ExerciseDurationEdt.getText().toString();
                ExerciseCaloriesBurned = ExerciseCaloriesBurnedEdt.getText().toString();
                ExerciseTime = ExerciseTimeEdt.getText().toString();
                ExerciseNotes = ExerciseNotesEdt.getText().toString();

                // validating the text fields if empty or not.
                if (TextUtils.isEmpty(ExerciseType)) {
                    ExerciseTypeEdt.setError("Please enter Exercise type");
                } else if (TextUtils.isEmpty(ExerciseIntensity)) {
                    ExerciseIntensityEdt.setError("Please enter Exercise intensity");
                } else if (TextUtils.isEmpty(ExerciseDuration)) {
                    ExerciseDurationEdt.setError("Please enter Exercise duration");
                } else if (TextUtils.isEmpty(ExerciseCaloriesBurned)) {
                    ExerciseCaloriesBurnedEdt.setError("Please enter calories burned");
                } else if (TextUtils.isEmpty(ExerciseTime)) {
                    ExerciseTimeEdt.setError("Please enter Exercise time");
                } else if (TextUtils.isEmpty(ExerciseNotes)) {
                    ExerciseNotesEdt.setError("Please enter Notes");
                } else {
                    // calling method to add data to Firebase Firestore.
                    addDataToFirestore(ExerciseType, ExerciseIntensity, ExerciseDuration, ExerciseCaloriesBurned, ExerciseTime, ExerciseNotes);
                }
            }
        });
    }

    private void addDataToFirestore(String ExerciseType, String ExerciseIntensity, String ExerciseDuration, String ExerciseCaloriesBurned, String ExerciseTime, String ExerciseNotes) {

        // creating a collection reference
        // for our Firebase Firestore database.
        CollectionReference dbCourses = db.collection("ExerciseCollection");

        // adding our data to our courses object class.
        ExerciseLog exerciseLog = new ExerciseLog(ExerciseType, ExerciseIntensity, ExerciseDuration, ExerciseCaloriesBurned, ExerciseTime, ExerciseNotes);

        // below method is use to add data to Firebase Firestore.
        dbCourses.add(exerciseLog).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // after the data addition is successful
                // we are displaying a success toast message.
                Toast.makeText(ExerciseLogInputPage.this, "Your Exercise has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // this method is called when the data addition process is failed.
                // displaying a toast message when data addition is failed.
                Toast.makeText(ExerciseLogInputPage.this, "Fail to add Exercise \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
