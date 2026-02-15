package com.example.firebaseproject;

import com.google.firebase.firestore.PropertyName;

public class Post {
    private String postId;
    private String firstName;
    private String lastName;
    private String caption;
    private String category;
    private Object timestamp;

    public Post() {}

    //Getters
    public String getPostId() { return postId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getCaption() { return caption; }
    public String getCategory() { return category; }
    public Object getTimestamp() { return timestamp; }

    //Setters
    public void setPostId(String postId) { this.postId = postId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setCaption(String caption) { this.caption = caption; }
    public void setCategory(String category) { this.category = category; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }
}