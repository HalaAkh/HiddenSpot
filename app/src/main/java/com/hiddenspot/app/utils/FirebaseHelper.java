package com.hiddenspot.app.utils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hiddenspot.app.models.Place;

import java.util.HashMap;
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

    private FirebaseHelper() {
        db      = FirebaseFirestore.getInstance();
        auth    = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseFirestore getDb() { return db; }
    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    // AUTH
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
                    FirebaseUser firebaseUser = r.getUser();
                    if (firebaseUser == null) {
                        onFailure.onFailure(new IllegalStateException("User creation failed"));
                        return;
                    }
                    String uid = firebaseUser.getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("displayName", username);
                    user.put("email", email);
                    user.put("bio", "");
                    user.put("avatarUrl", "");
                    user.put("gemsCount", 0);
                    firebaseUser.updateProfile(new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build())
                            .addOnSuccessListener(v ->
                                    db.collection(COLLECTION_USERS).document(uid).set(user)
                                            .addOnSuccessListener(v2 -> onSuccess.onSuccess(null))
                                            .addOnFailureListener(onFailure))
                            .addOnFailureListener(onFailure);
                }).addOnFailureListener(onFailure);
    }

    public void signOut() { auth.signOut(); }

    // GEMS
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

    public void addGem(Place place,
                       OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", place.getName());
        data.put("city", place.getCity());
        data.put("address", place.getAddress());
        data.put("phone", place.getPhone());
        data.put("description", place.getDescription());
        data.put("category", place.getCategory());
        data.put("images", place.getImages());
        data.put("userId", place.getUserId());
        data.put("userName", place.getUserName());
        data.put("rating", place.getRating());
        data.put("ratingCount", place.getRatingCount());
        data.put("likesCount", place.getLikesCount());
        data.put("upvotes", place.getUpvotes());
        data.put("downvotes", place.getDownvotes());
        data.put("status", "pending");
        data.put("isVerified", false);
        data.put("createdAt", FieldValue.serverTimestamp());
        
        db.collection(COLLECTION_GEMS).add(data)
                .addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    // VOTES
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

    // SAVES
    public void saveGem(String userId, String gemId,
                        OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        Map<String, Object> save = new HashMap<>();
        save.put("userId", userId);
        save.put("gemId", gemId);
        save.put("collectionName", "Default");
        save.put("savedAt", FieldValue.serverTimestamp());
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

    // USER PROFILE
    public void fetchUserProfile(String userId,
                                 OnSuccessListener<DocumentSnapshot> onSuccess,
                                 OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(userId)
                .get().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void updateUserProfile(String userId, Map<String, Object> updates,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_USERS).document(userId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void updateAuthProfile(String displayName, String avatarUrl,
                                  OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            onFailure.onFailure(new IllegalStateException("No authenticated user"));
            return;
        }

        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName);
        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("http")) {
            builder.setPhotoUri(android.net.Uri.parse(avatarUrl));
        }
        user.updateProfile(builder.build())
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void syncUserProfileToGems(String userId, String displayName, String avatarUrl,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection(COLLECTION_GEMS).whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        batch.update(doc.getReference(), "userName", displayName, "userAvatar", avatarUrl);
                    }
                    batch.commit().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
                })
                .addOnFailureListener(onFailure);
    }

    // STORAGE
    public StorageReference getImageUploadRef() {
        return storage.getReference().child("gem_images/" + UUID.randomUUID().toString() + ".jpg");
    }

    public StorageReference getAvatarUploadRef(String userId) {
        return storage.getReference().child("avatars/" + userId + "/" + UUID.randomUUID().toString() + ".jpg");
    }
}
