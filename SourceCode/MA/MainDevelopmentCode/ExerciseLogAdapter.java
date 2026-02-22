package com.example.firestoredatabasetest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ExerciseLogAdapter extends RecyclerView.Adapter<ExerciseLogAdapter.ViewHolder> {

    private ArrayList<ExerciseLog> exerciseLogArrayList;

    public ExerciseLogAdapter(ArrayList<ExerciseLog> exerciseLogArrayList) {
        this.exerciseLogArrayList = exerciseLogArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exercise_log_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseLog model = exerciseLogArrayList.get(position);
        holder.tvType.setText(model.getEL_EXERCISE_TYPE());
        holder.tvDetails.setText(model.getEl_INTENSITY() + " | " + model.getEL_DURATION_MINS() + " mins | " + model.getEL_CALORIES_BURNED() + " kcal");
        holder.tvNotes.setText(model.getEL_NOTES());
    }

    @Override
    public int getItemCount() {
        return exerciseLogArrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvNotes;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.idTVExerciseType);
            tvDetails = itemView.findViewById(R.id.idTVExerciseDetails);
            tvNotes = itemView.findViewById(R.id.idTVExerciseNotes);
        }
    }
}