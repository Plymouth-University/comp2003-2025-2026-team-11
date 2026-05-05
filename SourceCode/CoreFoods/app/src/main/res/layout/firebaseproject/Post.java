package com.example.firebaseproject;

public class Post {
    private String postId;
    private String firstName;
    private String lastName;
    private String caption;
    private String category;
    private Object timestamp;
    private boolean isTrainer;
    private String trainerType;
    private String trainingDescription;
    private String trainingScheme;
    private int likeCount;

    public Post() {}

    //Getters
    public String getPostId() { return postId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCaption() { return caption; }
    public String getCategory() { return category; }
    public Object getTimestamp() { return timestamp; }
    public boolean isTrainer() { return isTrainer; }
    public String getTrainerType() { return trainerType; }
    public String getTrainingDescription() { return trainingDescription; }
    public String getTrainingScheme() { return trainingScheme; }
    public int getLikeCount() { return likeCount; }


    //Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setCategory(String category) { this.category = category; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
    public void setTrainer(boolean trainer) { isTrainer = trainer; }
    public void setTrainerType(String trainerType) { this.trainerType = trainerType; }
    public void setTrainingDescription(String trainingDescription) { this.trainingDescription = trainingDescription; }
    public void setTrainingScheme(String trainingScheme) { this.trainingScheme = trainingScheme; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
}