package com.example.firebaseproject;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        //Name Format
        String fName = post.getFirstName();
        String lName = post.getLastName();

        if (fName != null && lName != null && !lName.isEmpty()) {
            String formattedName = fName + " " + lName.substring(0, 1).toUpperCase() + ".";
            holder.username.setText(formattedName);
        } else if (fName != null) {
            holder.username.setText(fName);
        } else {
            holder.username.setText("Guest User");
        }

        holder.caption.setText(post.getCaption());

        //Date Format
        if (post.getTimestamp() != null) {
            Date date;
            if (post.getTimestamp() instanceof com.google.firebase.Timestamp) {
                date = ((com.google.firebase.Timestamp) post.getTimestamp()).toDate();
            } else {
                date = new Date((Long) post.getTimestamp());
            }

            SimpleDateFormat sdf = new SimpleDateFormat("d MMM, h:mm a", Locale.getDefault());
            String formattedDate = sdf.format(date);
            formattedDate = formattedDate.replace("AM", "am").replace("PM", "pm");
            holder.timestamp.setText(formattedDate);
        }

        //Answers Format
        FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.getPostId())
                .collection("answers")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        holder.answerRecycler.setVisibility(View.VISIBLE);
                        List<String> answerTexts = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value) {
                            String reply = doc.getString("answerText");
                            if (reply != null) answerTexts.add(reply);
                        }
                        holder.answerRecycler.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
                        holder.answerRecycler.setAdapter(new AnswerInternalAdapter(answerTexts));
                    } else {
                        holder.answerRecycler.setVisibility(View.GONE);
                    }
                });

        //Allow Answers to be given
        if ("Questions".equalsIgnoreCase(post.getCategory())) {
            holder.btnAnswer.setVisibility(View.VISIBLE);
            holder.btnAnswer.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), AnswerPage.class);
                intent.putExtra("postId", post.getPostId());
                v.getContext().startActivity(intent);
            });
        } else {
            holder.btnAnswer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return postList.size(); }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView username, caption, timestamp;
        ImageButton btnAnswer;
        RecyclerView answerRecycler;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.text_username);
            caption = itemView.findViewById(R.id.text_caption);
            timestamp = itemView.findViewById(R.id.text_timestamp);
            btnAnswer = itemView.findViewById(R.id.btn_answer);
            answerRecycler = itemView.findViewById(R.id.recycler_answers);
        }
    }
}

//Answer logic
class AnswerInternalAdapter extends RecyclerView.Adapter<AnswerInternalAdapter.AVHolder> {
    private List<String> list;
    public AnswerInternalAdapter(List<String> list) { this.list = list; }
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