package com.hiddenspot.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

<<<<<<< HEAD
=======
import java.text.SimpleDateFormat;
import java.util.Locale;

>>>>>>> 93c6f54 (Modified)
public class Review {

    @DocumentId
    private String id;
    private String gemId;
<<<<<<< HEAD

    private String userId;
    private String userName;
    private float rating;       // 1–5 stars
    private String comment;
=======
    private String userId;
    private String userName;
    private String userAvatar;
    private float rating;
    private String comment;
    private String gemName;
>>>>>>> 93c6f54 (Modified)

    @ServerTimestamp
    private Timestamp createdAt;

<<<<<<< HEAD
    public Review() {
    }

    public Review(String gemId, String userId, String userName, float rating, String comment) {
        this.gemId = gemId;
        this.userId = userId;
        this.userName = userName;
=======
    public Review() {}

    public Review(String gemId, String userId, String userName, String userAvatar, float rating, String comment) {
        this.gemId = gemId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
>>>>>>> 93c6f54 (Modified)
        this.rating = rating;
        this.comment = comment;
    }

<<<<<<< HEAD
    // ── Getters ──────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getGemId() {
        return gemId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(String id) {
        this.id = id;
    }

    public void setGemId(String gemId) {
        this.gemId = gemId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(Timestamp t) {
        this.createdAt = t;
    }
    // ── Helpers ───────────────────────────────────────────────────────────────
    public String getFormattedDate() {
        if (createdAt == null) return "Recently";
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        return sdf.format(createdAt.toDate());
=======
    public String getId() { return id; }
    public String getGemId() { return gemId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public String getGemName() { return gemName; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setGemId(String gemId) { this.gemId = gemId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setRating(float rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setGemName(String gemName) { this.gemName = gemName; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getFormattedDate() {
        if (createdAt == null) return "Recently";
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(createdAt.toDate());
>>>>>>> 93c6f54 (Modified)
    }
}
