package com.hiddenspot.app.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class Review {

    @DocumentId
    private String id;
    private String gemId;

    private String userId;
    private String userName;
    private float rating;       // 1–5 stars
    private String comment;

    @ServerTimestamp
    private Timestamp createdAt;

    public Review() {
    }

    public Review(String gemId, String userId, String userName, float rating, String comment) {
        this.gemId = gemId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
    }

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
    }
}
