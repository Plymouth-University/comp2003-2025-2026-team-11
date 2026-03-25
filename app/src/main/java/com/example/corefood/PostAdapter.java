package com.example.corefood;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//Post Adapter
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_STANDARD = 0;
    private static final int VIEW_TYPE_TRAINING = 1;
    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @Override
    public int getItemViewType(int position) {
        Post post = postList.get(position);
        if (post.getCategory() != null && post.getCategory().equalsIgnoreCase("Training")) {
            return VIEW_TYPE_TRAINING;
        }
        return VIEW_TYPE_STANDARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TRAINING) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_training_post, parent, false);
            return new TrainingViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post, parent, false);
            return new StandardViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Post post = postList.get(position);

        if (holder instanceof TrainingViewHolder) {
            ((TrainingViewHolder) holder).bind(post);
        } else {
            ((StandardViewHolder) holder).bind(post);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //Training Post
    static class TrainingViewHolder extends RecyclerView.ViewHolder {
        TextView title, trainer_name, trainer_type, description;
        ImageView btnArrow;

        public TrainingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.training_title);
            trainer_name = itemView.findViewById(R.id.trainer_name);
            trainer_type = itemView.findViewById(R.id.trainer_type);
            description = itemView.findViewById(R.id.training_description);
            btnArrow = itemView.findViewById(R.id.arrow_btn);
        }

        public void bind(Post post) {
            title.setText(post.getCaption());
            trainer_name.setText("Trainer: " + post.getFirstName() + " " + post.getLastName());
            trainer_type.setText(post.getTrainerType());

            if (post.getTrainingDescription() != null && !post.getTrainingDescription().isEmpty()) {
                description.setText(post.getTrainingDescription());
            } else {
                description.setText("No description provided.");
            }

            //Arrow button for instructions page
            btnArrow.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), TrainerInstructionsPage.class);

                intent.putExtra("postId", post.getPostId());
                intent.putExtra("title", post.getCaption());
                intent.putExtra("trainerName", "Trainer: " + post.getFirstName() + " " + post.getLastName());
                intent.putExtra("trainerType", post.getTrainerType());
                intent.putExtra("scheme", post.getTrainingScheme());
                v.getContext().startActivity(intent);
            });
        }
    }

    //Standard Post
    static class StandardViewHolder extends RecyclerView.ViewHolder {
        TextView username, caption, timestamp;
        ImageButton btnAnswer;
        RecyclerView answerRecycler;

        public StandardViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.text_username);
            caption = itemView.findViewById(R.id.text_caption);
            timestamp = itemView.findViewById(R.id.text_timestamp);
            btnAnswer = itemView.findViewById(R.id.btn_answer);
            answerRecycler = itemView.findViewById(R.id.recycler_answers);
        }

        public void bind(Post post) {
            //Name Format
            String fName = post.getFirstName();
            String lName = post.getLastName();
            if (fName != null && lName != null && !lName.isEmpty()) {
                String formattedName = fName + " " + lName.substring(0, 1).toUpperCase() + ".";
                username.setText(formattedName);
            } else if (fName != null) {
                username.setText(fName);
            } else {
                username.setText("Guest User");
            }

            caption.setText(post.getCaption());

            //Date Format
            if (post.getTimestamp() != null) {
                Date date;
                if (post.getTimestamp() instanceof com.google.firebase.Timestamp) {
                    date = ((com.google.firebase.Timestamp) post.getTimestamp()).toDate();
                } else {
                    date = new Date((Long) post.getTimestamp());
                }
                SimpleDateFormat sdf = new SimpleDateFormat("d MMM, h:mm a", Locale.getDefault());
                String formattedDate = sdf.format(date).replace("AM", "am").replace("PM", "pm");
                timestamp.setText(formattedDate);
            }

            //Answers Format
            FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(post.getPostId())
                    .collection("answers")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((value, error) -> {
                        if (value != null && !value.isEmpty()) {
                            answerRecycler.setVisibility(View.VISIBLE);
                            List<String> answerTexts = new ArrayList<>();
                            for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                                String reply = doc.getString("answerText");
                                if (reply != null) answerTexts.add(reply);
                            }
                            answerRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                            answerRecycler.setAdapter(new AnswerAdapter(answerTexts));
                        } else {
                            answerRecycler.setVisibility(View.GONE);
                        }
                    });

            //Allows answers to be given
            if ("Questions".equalsIgnoreCase(post.getCategory())) {
                btnAnswer.setVisibility(View.VISIBLE);
                btnAnswer.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), AnswerPage.class);
                    intent.putExtra("postId", post.getPostId());
                    v.getContext().startActivity(intent);
                });
            } else {
                btnAnswer.setVisibility(View.GONE);
            }
        }
    }
}

//Answer Adapter
class AnswerAdapter extends RecyclerView.Adapter<AnswerAdapter.AVHolder> {
    private List<String> list;
    public AnswerAdapter(List<String> list) { this.list = list; }
    @NonNull @Override public AVHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_answer, p, false);
        return new AVHolder(v);
    }
    @Override public void onBindViewHolder(@NonNull AVHolder h, int p) { h.t.setText(list.get(p)); }
    @Override public int getItemCount() { return list.size(); }
    static class AVHolder extends RecyclerView.ViewHolder {
        TextView t; public AVHolder(View v) { super(v); t = v.findViewById(R.id.text_answer_body); }
    }
}