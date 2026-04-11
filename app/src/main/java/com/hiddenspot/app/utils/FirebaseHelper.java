package com.hiddenspot.app.utils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiddenspot.app.models.AppNotification;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.models.Review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirebaseHelper {

    private static FirebaseHelper instance;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final FirebaseStorage storage;

    public static final String COLLECTION_GEMS    = "gems";
    public static final String COLLECTION_USERS   = "users";
    public static final String COLLECTION_SAVES   = "saves";
    public static final String COLLECTION_REVIEWS = "reviews";
<<<<<<< HEAD
=======
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String SUBCOLLECTION_VOTES = "votes";
>>>>>>> 93c6f54 (Modified)

    private FirebaseHelper() {
        db      = FirebaseFirestore.getInstance();
        auth    = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    public FirebaseAuth       getAuth()        { return auth; }
    public FirebaseFirestore  getDb()          { return db; }
    public FirebaseUser       getCurrentUser() { return auth.getCurrentUser(); }

    // ── AUTH ─────────────────────────────────────────────────────────────────

    public void signIn(String email, String password,
                       OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void signUp(String email, String password, String username,
                       OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    FirebaseUser user = r.getUser();
                    if (user == null) { onFailure.onFailure(new Exception("User creation failed")); return; }
                    
                    // Set display name on FirebaseAuth profile
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(username).build();
                    user.updateProfile(profileUpdates);
                    
                    // Note: We don't create the Firestore document here anymore.
                    // It will be created upon first login/verification check in AuthActivity.
                    onSuccess.onSuccess(null);
                }).addOnFailureListener(onFailure);
    }

    public void createUserDocument(String uid, String email, String username,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("uid",         uid);
        userDoc.put("displayName", username);
        userDoc.put("email",       email);
        userDoc.put("gemsCount",   0);
        userDoc.put("avatarBase64", "");
        db.collection(COLLECTION_USERS).document(uid).set(userDoc)
                .addOnSuccessListener(v -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void signOut() { auth.signOut(); }

    // ── USER PROFILE ─────────────────────────────────────────────────────────

    /**
     * Save a base64-encoded avatar string to the user's Firestore document.
     */
    public void updateUserAvatar(String uid, String base64Avatar,
                                 OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(uid)
                .update("avatarBase64", base64Avatar)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Update display name in both FirebaseAuth profile and Firestore doc.
     */
    public void updateUserDisplayName(String uid, String newName,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName).build();
            user.updateProfile(req);
        }
        db.collection(COLLECTION_USERS).document(uid)
                .update("displayName", newName)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Fetch the Firestore user document (contains avatarBase64, displayName, etc.).
     */
    public void fetchUserDoc(String uid,
                             OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot> onSuccess,
                             OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    // ── GEMS ─────────────────────────────────────────────────────────────────

    public void fetchAllGems(OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_GEMS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void fetchGemsByUser(String userId,
                                OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_GEMS).whereEqualTo("userId", userId)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void fetchGemById(String gemId,
                             OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_GEMS).document(gemId)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void deleteGem(String gemId,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        String currentUid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (currentUid == null) {
            onFailure.onFailure(new IllegalStateException("Not authenticated"));
            return;
        }

        DocumentReference gemRef = db.collection(COLLECTION_GEMS).document(gemId);
        gemRef.get()
                .addOnSuccessListener(gemDoc -> {
                    if (!gemDoc.exists()) {
                        onFailure.onFailure(new IllegalStateException("Post not found"));
                        return;
                    }

                    String ownerId = gemDoc.getString("userId");
                    if (ownerId == null || !ownerId.equals(currentUid)) {
                        onFailure.onFailure(new IllegalStateException("You can only delete your own posts"));
                        return;
                    }
                    gemRef.delete()
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    public void addGem(Place place,
                       OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("name",        place.getName());
        data.put("city",        place.getCity());
        data.put("address",     place.getAddress());
        data.put("phone",       place.getPhone());
        data.put("description", place.getDescription());
        data.put("category",    place.getCategory());
        data.put("images",      place.getImages());
        data.put("userId",      place.getUserId());
        data.put("userName",    place.getUserName());
        data.put("rating",      place.getRating());
        data.put("ratingCount", place.getRatingCount());
        data.put("likesCount",  place.getLikesCount());
        data.put("upvotes",     place.getUpvotes());
        data.put("downvotes",   place.getDownvotes());
        data.put("status",      "pending");
        data.put("isVerified",  false);
        data.put("createdAt",   FieldValue.serverTimestamp());
        db.collection(COLLECTION_GEMS).add(data)
                .addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    // ── VOTES ────────────────────────────────────────────────────────────────

    public void upvoteGem(String gemId) {
        db.collection(COLLECTION_GEMS).document(gemId).update("upvotes", FieldValue.increment(1));
    }
    public void removeUpvote(String gemId) {
        db.collection(COLLECTION_GEMS).document(gemId).update("upvotes", FieldValue.increment(-1));
    }
    public void downvoteGem(String gemId) {
        db.collection(COLLECTION_GEMS).document(gemId).update("downvotes", FieldValue.increment(1));
    }
    public void removeDownvote(String gemId) {
        db.collection(COLLECTION_GEMS).document(gemId).update("downvotes", FieldValue.increment(-1));
    }

    // ── SAVES ────────────────────────────────────────────────────────────────

    public void saveGem(String userId, String gemId,
                        OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> save = new HashMap<>();
        save.put("userId",         userId);
        save.put("gemId",          gemId);
        save.put("collectionName", "Default");
        save.put("savedAt",        FieldValue.serverTimestamp());
        db.collection(COLLECTION_SAVES).document(userId + "_" + gemId)
                .set(save).addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void unsaveGem(String userId, String gemId,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_SAVES).document(userId + "_" + gemId)
                .delete().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void fetchSavedGemIds(String userId,
                                 OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_SAVES).whereEqualTo("userId", userId)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

<<<<<<< HEAD
    // ── REVIEWS ──────────────────────────────────────────────────────────────
=======
    // REVIEWS
    public void addReview(Review review,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("gemId", review.getGemId());
        data.put("userId", review.getUserId());
        data.put("userName", review.getUserName());
        data.put("userAvatar", review.getUserAvatar());
        data.put("rating", review.getRating());
        data.put("comment", review.getComment());
        data.put("createdAt", FieldValue.serverTimestamp());

        String docId = review.getUserId() + "_" + review.getGemId();
        db.collection(COLLECTION_GEMS).document(review.getGemId()).get()
                .addOnSuccessListener(gemDoc -> {
                    String ownerId = gemDoc.getString("userId");
                    if (ownerId != null && ownerId.equals(review.getUserId())) {
                        onFailure.onFailure(new IllegalStateException("You can't rate your own post"));
                        return;
                    }

                    db.collection(COLLECTION_REVIEWS).document(docId).set(data)
                            .addOnSuccessListener(v -> recalculateRating(review.getGemId(), onSuccess, onFailure))
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    public void fetchMyReview(String userId, String gemId,
                              OnSuccessListener<Review> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_REVIEWS).document(userId + "_" + gemId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        onSuccess.onSuccess(null);
                        return;
                    }
                    Review review = doc.toObject(Review.class);
                    if (review != null) review.setId(doc.getId());
                    onSuccess.onSuccess(review);
                })
                .addOnFailureListener(onFailure);
    }

    public void fetchReviewsForGem(String gemId,
                                   OnSuccessListener<QuerySnapshot> onSuccess,
                                   OnFailureListener onFailure) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("gemId", gemId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteReview(Review review,
                             OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (review == null || review.getUserId() == null || review.getGemId() == null) {
            onFailure.onFailure(new IllegalArgumentException("Invalid review"));
            return;
        }

        String currentUid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (currentUid == null || !currentUid.equals(review.getUserId())) {
            onFailure.onFailure(new IllegalStateException("You can only delete your own review"));
            return;
        }

        String docId = review.getUserId() + "_" + review.getGemId();
        db.collection(COLLECTION_REVIEWS).document(docId)
                .delete()
                .addOnSuccessListener(v -> recalculateRating(review.getGemId(), onSuccess, onFailure))
                .addOnFailureListener(onFailure);
    }

    public void createNotification(AppNotification notification,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (notification == null) {
            onFailure.onFailure(new IllegalArgumentException("Invalid notification"));
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("recipientUserId", notification.getRecipientUserId());
        data.put("actorUserId", notification.getActorUserId());
        data.put("actorName", notification.getActorName());
        data.put("actionType", notification.getActionType());
        data.put("gemId", notification.getGemId());
        data.put("gemName", notification.getGemName());
        data.put("message", notification.getMessage());
        data.put("createdAt", FieldValue.serverTimestamp());

        db.collection(COLLECTION_NOTIFICATIONS)
                .add(data)
                .addOnSuccessListener(r -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    public void fetchNotifications(String userId,
                                   OnSuccessListener<QuerySnapshot> onSuccess,
                                   OnFailureListener onFailure) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("recipientUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public ListenerRegistration listenForNotifications(String userId,
                                                       com.google.firebase.firestore.EventListener<QuerySnapshot> listener) {
        return db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("recipientUserId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener(listener);
    }

    private void recalculateRating(String gemId,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("gemId", gemId)
                .get()
                .addOnSuccessListener(snap -> {
                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Double rating = doc.getDouble("rating");
                        if (rating != null) {
                            total += rating;
                            count++;
                        }
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("rating", count > 0 ? total / count : 0.0);
                    updates.put("ratingCount", count);
                    db.collection(COLLECTION_GEMS).document(gemId)
                            .update(updates)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // USER PROFILE
    public void fetchUserProfile(String userId,
                                 OnSuccessListener<DocumentSnapshot> onSuccess,
                                 OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(userId)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }
>>>>>>> 93c6f54 (Modified)

    /**
     * Add a review for a gem and atomically update the gem's rating average.
     */
    public void addReview(Review review,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("gemId",     review.getGemId());
        data.put("userId",    review.getUserId());
        data.put("userName",  review.getUserName());
        data.put("rating",    review.getRating());
        data.put("comment",   review.getComment());
        data.put("createdAt", FieldValue.serverTimestamp());

        // Use the userId+gemId as document ID so each user can only review a place once.
        String docId = review.getUserId() + "_" + review.getGemId();
        db.collection(COLLECTION_REVIEWS).document(docId).set(data)
                .addOnSuccessListener(v -> {
                    // Re-calculate average rating on the gem document
                    recalculateRating(review.getGemId(), onSuccess, onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Check whether the current user has already reviewed a gem.
     * Returns the existing Review via onSuccess (null if none found).
     */
    public void fetchMyReview(String userId, String gemId,
                              OnSuccessListener<Review> onSuccess, OnFailureListener onFailure) {
        String docId = userId + "_" + gemId;
        db.collection(COLLECTION_REVIEWS).document(docId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Review r = doc.toObject(Review.class);
                        if (r != null) r.setId(doc.getId());
                        onSuccess.onSuccess(r);
                    } else {
                        onSuccess.onSuccess(null);
                    }
                })
                .addOnFailureListener(onFailure);
    }

    /**
     * Fetch all reviews for a gem, newest first.
     */
    public void fetchReviewsForGem(String gemId,
                                   OnSuccessListener<QuerySnapshot> onSuccess,
                                   OnFailureListener onFailure) {
        db.collection(COLLECTION_REVIEWS)
                .whereEqualTo("gemId", gemId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Pull all reviews for a gem and compute a new average, then write it back.
     */
    private void recalculateRating(String gemId,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_REVIEWS).whereEqualTo("gemId", gemId).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) { onSuccess.onSuccess(null); return; }
                    double total = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        Double r = doc.getDouble("rating");
                        if (r != null) total += r;
                    }
                    double avg   = total / snap.size();
                    int    count = snap.size();
                    Map<String, Object> update = new HashMap<>();
                    update.put("rating",      avg);
                    update.put("ratingCount", count);
                    db.collection(COLLECTION_GEMS).document(gemId).update(update)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // ── STORAGE ──────────────────────────────────────────────────────────────

    public StorageReference getImageUploadRef() {
        return storage.getReference().child("gem_images/" + UUID.randomUUID() + ".jpg");
    }
    public void fetchUserProfile(String uid,
                                 OnSuccessListener<com.google.firebase.firestore.DocumentSnapshot> onSuccess,
                                 OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(uid).get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public void updateUserProfile(String uid, java.util.Map<String, Object> updates,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(uid)
                .update(updates)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
    public void updateAuthProfile(String displayName, String avatarUrl,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseUser user = getCurrentUser();
        if (user == null) { onFailure.onFailure(new Exception("Not logged in")); return; }
        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(avatarUrl != null && !avatarUrl.isEmpty()
                        ? android.net.Uri.parse(avatarUrl) : null)
                .build();
        user.updateProfile(req)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }
    public void syncUserProfileToGems(String uid, String displayName, String avatarUrl,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        java.util.Map<String, Object> updates = new HashMap<>();
        updates.put("userName", displayName);
        updates.put("userAvatar", avatarUrl != null ? avatarUrl : "");
        db.collection(COLLECTION_GEMS).whereEqualTo("userId", uid).get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) { onSuccess.onSuccess(null); return; }
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snap.getDocuments()) {
                        db.collection(COLLECTION_GEMS).document(doc.getId()).update(updates);
                    }
                    onSuccess.onSuccess(null);
                })
                .addOnFailureListener(onFailure);
    }
}
