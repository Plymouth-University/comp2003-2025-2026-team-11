package com.example.corefoodsprototype.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.corefoodsprototype.R;

import com.example.corefoodsprototype.data.PrototypeDataStore;

public class CaloriesActivity extends AppCompatActivity {

    private EditText etDailyTarget;
    private TextView tvConsumed, tvBurned, tvNet, tvNotes;

    @Override
    protected void onResume() {
        super.onResume();
        refreshFromStore(null);
    }

    private void refreshFromStore(Integer target) {
        int consumed = PrototypeDataStore.getInstance().getTotalCaloriesConsumed();
        int burned = PrototypeDataStore.getInstance().getTotalCaloriesBurned();
        renderTotals(consumed, burned, target);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);

        etDailyTarget = findViewById(R.id.etDailyTarget);
        tvConsumed = findViewById(R.id.tvConsumed);
        tvBurned = findViewById(R.id.tvBurned);
        tvNet = findViewById(R.id.tvNet);
        tvNotes = findViewById(R.id.tvNotes);

        Button btnRecalculate = findViewById(R.id.btnRecalculate);
        Button btnResetDay = findViewById(R.id.btnResetDay);

        refreshFromStore(null);

        btnRecalculate.setOnClickListener(v -> recalculate());
        btnResetDay.setOnClickListener(v -> resetDay());
    }

    private void recalculate() {
        String targetStr = etDailyTarget.getText().toString().trim();
        if (TextUtils.isEmpty(targetStr)) {
            Toast.makeText(this, "Please enter a daily target first.", Toast.LENGTH_SHORT).show();
            return;
        }

        int target;
        try {
            target = Integer.parseInt(targetStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Target must be a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (target <= 0) {
            Toast.makeText(this, "Target must be greater than 0.", Toast.LENGTH_SHORT).show();
            return;
        }

        refreshFromStore(target);
        Toast.makeText(this, "Recalculated.", Toast.LENGTH_SHORT).show();
    }

    private void resetDay() {
        etDailyTarget.setText("");

        PrototypeDataStore.getInstance().resetDay();
        refreshFromStore(null);

        tvNotes.setText("Day reset. Log meals and exercises again to rebuild totals.");
        Toast.makeText(this, "Day reset.", Toast.LENGTH_SHORT).show();
    }

    private void renderTotals(int consumed, int burned, Integer target) {
        tvConsumed.setText("Calories consumed: " + consumed + " kcal");
        tvBurned.setText("Calories burned (estimated): " + burned + " kcal");

        int net = consumed - burned;
        tvNet.setText("Net balance: " + net + " kcal");

        if (target != null) {
            int delta = target - net;
            String status = (delta >= 0)
                    ? ("You are about " + delta + " kcal under your target.")
                    : ("You are about " + Math.abs(delta) + " kcal over your target.");
            tvNotes.setText(status);
        }
    }
}