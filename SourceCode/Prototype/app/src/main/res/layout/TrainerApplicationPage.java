package com.example.firebaseproject;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TrainerApplicationPage extends DialogFragment {

    private TextView tvQuestion;
    private MaterialButton btn1, btn2;
    private int currentStep = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trainer, container, false);

        //Make the background of the dialog transparent so the rounded corners show
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        tvQuestion = view.findViewById(R.id.tv_question);
        btn1 = view.findViewById(R.id.btn_option_1);
        btn2 = view.findViewById(R.id.btn_option_2);

        showQuestion1();

        view.findViewById(R.id.tv_cancel).setOnClickListener(v -> dismiss());

        return view;
    }

    private void showQuestion1() {
        tvQuestion.setText("What type of account do you want?");
        btn1.setText("Trainer");
        btn2.setText("Member");

        btn1.setOnClickListener(v -> showQuestion2());
        btn2.setOnClickListener(v -> updateStatus("Member", false));
    }

    private void showQuestion2() {
        tvQuestion.setText("What is your area of expertise?");
        btn1.setText("Workout & Fitness");
        btn2.setText("Diet & Nutrition");

        btn1.setOnClickListener(v -> updateStatus("Exercise Trainer", true));
        btn2.setOnClickListener(v -> updateStatus("Nutrition Trainer", true));
    }

    private void updateStatus(String type, boolean isTrainer) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(uid)
                .update("isTrainer", isTrainer, "trainerType", type)
                .addOnSuccessListener(aVoid -> {
                    String message = isTrainer ? "You are now an " + type : "Account set as Member";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (getActivity() != null) getActivity().recreate(); //Refresh the page to show new title
                });
    }
}