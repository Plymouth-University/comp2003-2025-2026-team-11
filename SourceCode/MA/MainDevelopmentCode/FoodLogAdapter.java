package com.example.firestoredatabasetest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.ViewHolder> {

    private ArrayList<FoodLog> foodLogArrayList;

    public FoodLogAdapter(ArrayList<FoodLog> foodLogArrayList) {
        this.foodLogArrayList = foodLogArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodLog model = foodLogArrayList.get(position);
        holder.tvMealName.setText(model.getFL_MEAL_NAME());
        holder.tvDetails.setText(model.getFL_MEAL_TYPE() + " | " + model.getFL_CALORIES() + " Cal | " + model.getFL_TIME());
        holder.tvNotes.setText(model.getFL_NOTES());
    }

    @Override
    public int getItemCount() {
        return foodLogArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName, tvDetails, tvNotes;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvDetails = itemView.findViewById(R.id.tvMealDetails);
            tvNotes = itemView.findViewById(R.id.tvMealNotes);
        }
    }
}