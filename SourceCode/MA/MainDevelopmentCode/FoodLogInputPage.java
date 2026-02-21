package com.example.firestoredatabasetest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FoodLogInputPage extends AppCompatActivity {

    private EditText MealNameEdt, MealTypeEdt, MealCaloriesEdt, MealTimeEdt, MealNotesEdt;

    private Button submitMealBtn;

    private String MealName, MealType, MealCalories, MealTime, MealNotes;

    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_log_input_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        MealNameEdt = findViewById(R.id.idEdtMealName);
        MealTypeEdt = findViewById(R.id.idEdtMealType);
        MealCaloriesEdt = findViewById(R.id.idEdtCalories);
        MealTimeEdt = findViewById(R.id.idEdtTime);
        MealNotesEdt = findViewById(R.id.idEdtNotes);
        submitMealBtn = findViewById(R.id.idBtnSubmitCourse);

        submitMealBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting data from edittext fields.
                MealName = MealNameEdt.getText().toString();
                MealType = MealTypeEdt.getText().toString();
                MealCalories = MealCaloriesEdt.getText().toString();
                MealTime = MealTimeEdt.getText().toString();
                MealNotes = MealNotesEdt.getText().toString();


                // validating the text fields if empty or not.
                if (TextUtils.isEmpty(MealName)) {
                    MealNameEdt.setError("Please enter Meal Name");
                } else if (TextUtils.isEmpty(MealType)) {
                    MealTypeEdt.setError("Please enter Meal Type");
                } else if (TextUtils.isEmpty(MealCalories)) {
                    MealCaloriesEdt.setError("Please enter Meal Calories");
                } else if (TextUtils.isEmpty(MealTime)) {
                    MealTimeEdt.setError("Please enter Meal Time");
                } else if (TextUtils.isEmpty(MealNotes)) {
                    MealNotesEdt.setError("Please enter Notes");
                } else {
                    // calling method to add data to Firebase Firestore.
                    addDataToFirestore(MealName, MealType, MealCalories, MealTime, MealNotes);
                }
            }
        });
    }

    private void addDataToFirestore(String MealName, String MealType, String MealCalories, String MealTime, String MealNotes) {

        // creating a collection reference
        // for our Firebase Firestore database.
        CollectionReference dbCourses = db.collection("MealCollection");

        // adding our data to our courses object class.
        FoodLog foodLog = new FoodLog(MealType, MealName, MealCalories, MealTime, MealNotes);

        // below method is use to add data to Firebase Firestore.
        dbCourses.add(foodLog).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // after the data addition is successful
                // we are displaying a success toast message.
                Toast.makeText(FoodLogInputPage.this, "Your Meal has been added to Firebase Firestore", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // this method is called when the data addition process is failed.
                // displaying a toast message when data addition is failed.
                Toast.makeText(FoodLogInputPage.this, "Fail to add Meal \n" + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
}