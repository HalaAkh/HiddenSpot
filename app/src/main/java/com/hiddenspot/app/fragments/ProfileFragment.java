package com.hiddenspot.app.fragments;

<<<<<<< HEAD
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
=======
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
>>>>>>> 93c6f54 (Modified)
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hiddenspot.app.R;
import com.hiddenspot.app.activities.AuthActivity;
<<<<<<< HEAD
import com.hiddenspot.app.adapters.PlaceAdapter;
import com.hiddenspot.app.adapters.ReviewAdapter;
=======
import com.hiddenspot.app.activities.EditProfileActivity;
import com.hiddenspot.app.activities.PlaceDetailsActivity;
import com.hiddenspot.app.adapters.NotificationAdapter;
import com.hiddenspot.app.adapters.PlaceAdapter;
import com.hiddenspot.app.adapters.ReviewAdapter;
import com.hiddenspot.app.models.AppNotification;
>>>>>>> 93c6f54 (Modified)
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.models.Review;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
=======
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;
>>>>>>> 93c6f54 (Modified)

public class ProfileFragment extends Fragment {

    private static final int REQUEST_GALLERY = 300;
    private static final int REQUEST_PERM = 301;

    private TextView tvUsername, tvAvatarLetter, tvPostsCount;
    private TextView tvStatPlaces, tvStatLikes, tvStatRating;
    private ImageView ivAvatar;
    private RecyclerView rvMyPosts;
    private LinearLayout rowEditProfile, rowRatings, rowNotifications, rowHelp;
    private MaterialButton btnLogout;

    private PlaceAdapter postsAdapter;
    private final List<Place> myPosts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUsername = view.findViewById(R.id.tv_username);
        tvAvatarLetter = view.findViewById(R.id.tv_avatar_letter);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvPostsCount = view.findViewById(R.id.tv_posts_count);
        tvStatPlaces = view.findViewById(R.id.tv_stat_places);
        tvStatLikes = view.findViewById(R.id.tv_stat_likes);
        tvStatRating = view.findViewById(R.id.tv_stat_rating);
        rvMyPosts = view.findViewById(R.id.rv_my_posts);
        btnLogout = view.findViewById(R.id.btn_logout);
        rowEditProfile = view.findViewById(R.id.row_edit_profile);
        rowRatings = view.findViewById(R.id.row_ratings);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowHelp = view.findViewById(R.id.row_help);

<<<<<<< HEAD
        setMenuRow(rowEditProfile, R.id.tv_edit_icon, R.id.tv_edit_label, "✏️", "Edit Profile");
        setMenuRow(rowRatings, R.id.tv_ratings_icon, R.id.tv_ratings_label, "⭐", "My Ratings");
        setMenuRow(rowNotifications, R.id.tv_notif_icon, R.id.tv_notif_label, "🔔", "Notifications");
        setMenuRow(rowHelp, R.id.tv_help_icon, R.id.tv_help_label, "❓", "Help & Support");
=======
        setMenuRow(rowEditProfile, "✏️", "Edit Profile");
        setMenuRow(rowRatings, "⭐", "My Ratings");
        setMenuRow(rowNotifications, "🔔", "Notifications");
        setMenuRow(rowHelp, "❓", "Help & Support");
>>>>>>> 93c6f54 (Modified)

        postsAdapter = new PlaceAdapter(requireContext(), myPosts);
        postsAdapter.setShowDeleteButton(true);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(postsAdapter);
        rvMyPosts.setNestedScrollingEnabled(false);

<<<<<<< HEAD
=======
            boolean newState = !place.isFavorited();
            place.setFavorited(newState);
            postsAdapter.notifyItemChanged(pos);

            if (newState) {
                FirebaseHelper.getInstance().saveGem(uid, place.getId(),
                        v -> requireActivity().runOnUiThread(this::loadUserProfile),
                        e -> requireActivity().runOnUiThread(this::loadUserProfile));
            } else {
                FirebaseHelper.getInstance().unsaveGem(uid, place.getId(),
                        v -> requireActivity().runOnUiThread(this::loadUserProfile),
                        e -> requireActivity().runOnUiThread(this::loadUserProfile));
            }
        });
        postsAdapter.setOnDeleteClickListener((place, pos) -> showDeletePostDialog(place, pos));

        resetProfileUi();
>>>>>>> 93c6f54 (Modified)
        loadUserProfile();

        // ── Logout ────────────────────────────────────────────────────────
        btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            Intent i = new Intent(requireActivity(), AuthActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

<<<<<<< HEAD
        // ── Edit Profile ──────────────────────────────────────────────────
        rowEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // ── My Ratings ────────────────────────────────────────────────────
        rowRatings.setOnClickListener(v -> showMyRatings());

        // ── Notifications ─────────────────────────────────────────────────
        rowNotifications.setOnClickListener(v -> showNotificationsDialog());

        // ── Help & Support ────────────────────────────────────────────────
        rowHelp.setOnClickListener(v -> showHelpDialog());

        // ── Avatar tap ────────────────────────────────────────────────────
        View avatarContainer = view.findViewById(R.id.avatar_container);
        if (avatarContainer != null) {
            avatarContainer.setOnClickListener(v -> showAvatarOptions());
        }
=======
        View.OnClickListener openEditProfile = v ->
                startActivity(new Intent(requireActivity(), EditProfileActivity.class));
        rowEditProfile.setOnClickListener(openEditProfile);
        btnChangePhoto.setOnClickListener(openEditProfile);
        rowRatings.setOnClickListener(v -> showMyRatings());
        rowNotifications.setOnClickListener(v -> showNotificationsListDialog());
        rowHelp.setOnClickListener(v -> showHelpDialog());
>>>>>>> 93c6f54 (Modified)
    }

    // ── Load profile ──────────────────────────────────────────────────────

    private void loadUserProfile() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;
        String uid = user.getUid();
        String name = user.getDisplayName();
        if (name == null || name.isEmpty())
            name = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";

        final String displayName = name;
        tvUsername.setText(displayName);
        tvAvatarLetter.setText(displayName.substring(0, 1).toUpperCase());

        FirebaseHelper.getInstance().fetchUserDoc(uid, doc -> {
            if (doc.exists() && isAdded()) {
                String avatarBase64 = doc.getString("avatarBase64");
                if (avatarBase64 != null && !avatarBase64.isEmpty()) showAvatarImage(avatarBase64);
                String storedName = doc.getString("displayName");
                if (storedName != null && !storedName.isEmpty() && isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        tvUsername.setText(storedName);
                        tvAvatarLetter.setText(storedName.substring(0, 1).toUpperCase());
                    });
                }
            }
        }, e -> {
        });

        FirebaseHelper.getInstance().fetchGemsByUser(uid, snap -> {
            myPosts.clear();
            int likes = 0;
            double totalRating = 0;
            int ratedCount = 0;
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Place p = doc.toObject(Place.class);
                if (p != null) {
                    p.setId(doc.getId());
                    myPosts.add(p);
                    likes += p.getLikesCount();
                    if (p.getRating() > 0) {
                        totalRating += p.getRating();
                        ratedCount++;
                    }
                }
            }
            final int pl = myPosts.size(), lk = likes, rc = ratedCount;
            final double avg = rc > 0 ? totalRating / rc : 0;
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                postsAdapter.updatePlaces(myPosts);
                tvStatPlaces.setText(String.valueOf(pl));
                tvStatLikes.setText(String.valueOf(lk));
                tvStatRating.setText(rc > 0 ? String.format("%.1f", avg) : "—");
                tvPostsCount.setText(pl + " Posts");
            });
        }, e -> {
        });
    }

    // ── Avatar options dialog ─────────────────────────────────────────────

    private void showAvatarOptions() {
        String[] options = {"📷 Take Photo", "🖼️ Choose from Gallery", "Cancel"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Change Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else if (which == 1) checkPermissionAndPickImage();
                })
                .show();
    }

    private void openCamera() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(i, 302);
        } else {
            Toast.makeText(requireContext(), "Camera not available on this device", Toast.LENGTH_SHORT).show();
        }
    }

<<<<<<< HEAD
    private void checkPermissionAndPickImage() {
        String perm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(requireContext(), perm)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{perm}, REQUEST_PERM);
        }
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK) return;

        if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
            processAndUploadAvatarFromUri(data.getData());
        } else if (requestCode == 302 && data != null && data.getExtras() != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) processAndUploadAvatarFromBitmap(bitmap);
        }
    }

    private void processAndUploadAvatarFromUri(Uri uri) {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;
        new Thread(() -> {
            try {
                InputStream is = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();
                processAndUploadAvatarFromBitmap(bitmap);
            } catch (Exception e) {
                if (isAdded()) requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error reading image", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void processAndUploadAvatarFromBitmap(Bitmap bitmap) {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;
        new Thread(() -> {
            try {
                int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                int xOff = (bitmap.getWidth() - size) / 2;
                int yOff = (bitmap.getHeight() - size) / 2;
                Bitmap cropped = Bitmap.createBitmap(bitmap, xOff, yOff, size, size);
                Bitmap resized = Bitmap.createScaledBitmap(cropped, 200, 200, true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                String base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                FirebaseHelper.getInstance().updateUserAvatar(user.getUid(), base64, v -> {
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        showAvatarImage(base64);
                        Toast.makeText(requireContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show();
                    });
                }, e -> {
                    if (isAdded()) requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            } catch (Exception e) {
                if (isAdded()) requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error processing image", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showAvatarImage(String base64) {
        if (!isAdded() || ivAvatar == null) return;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            requireActivity().runOnUiThread(() -> {
                Glide.with(requireContext()).load(bytes).circleCrop().into(ivAvatar);
                ivAvatar.setVisibility(View.VISIBLE);
                tvAvatarLetter.setVisibility(View.GONE);
            });
        } catch (Exception ignored) {
        }
    }

    // ── Edit Profile dialog ───────────────────────────────────────────────

    private void showEditProfileDialog() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_profile, null);
        TextInputEditText etName = dialogView.findViewById(R.id.et_edit_name);
        if (etName != null) etName.setText(tvUsername.getText().toString());

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName != null && etName.getText() != null
                            ? etName.getText().toString().trim() : "";
                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseHelper.getInstance().updateUserDisplayName(user.getUid(), newName,
                            v -> requireActivity().runOnUiThread(() -> {
                                tvUsername.setText(newName);
                                tvAvatarLetter.setText(newName.substring(0, 1).toUpperCase());
                                Toast.makeText(requireContext(), "Name updated!", Toast.LENGTH_SHORT).show();
                            }),
                            e -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
=======
    private void showDeletePostDialog(Place place, int position) {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null || place == null || place.getId() == null) return;
        if (!user.getUid().equals(place.getUserId())) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Delete \"" + place.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deletePost(place, position))
>>>>>>> 93c6f54 (Modified)
                .setNegativeButton("Cancel", null)
                .show();
    }

<<<<<<< HEAD
    // ── My Ratings dialog ─────────────────────────────────────────────────
=======
    private void deletePost(Place place, int position) {
        if (position < 0 || position >= myPosts.size()) return;
        Place removed = myPosts.remove(position);
        postsAdapter.updatePlaces(myPosts);
        updatePostStatsAfterDelete();

        FirebaseHelper.getInstance().deleteGem(place.getId(), v -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show();
                loadUserProfile();
            });
        }, e -> {
            if (isAdded()) requireActivity().runOnUiThread(() -> {
                myPosts.add(position, removed);
                postsAdapter.updatePlaces(myPosts);
                updatePostStatsAfterDelete();
                Toast.makeText(requireContext(),
                        e.getMessage() != null ? e.getMessage() : "Failed to delete post",
                        Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updatePostStatsAfterDelete() {
        int posts = myPosts.size();
        int likes = 0;
        double totalRating = 0;
        int ratedCount = 0;

        for (Place post : myPosts) {
            likes += post.getUpvotes();
            if (post.getRating() > 0) {
                totalRating += post.getRating();
                ratedCount++;
            }
        }

        tvStatPlaces.setText(String.valueOf(posts));
        tvStatLikes.setText(String.valueOf(likes));
        tvStatRating.setText(ratedCount > 0
                ? String.format(Locale.getDefault(), "%.1f", totalRating / ratedCount) : "—");
        tvPostsCount.setText(getString(R.string.posts_count_format, posts));
    }
>>>>>>> 93c6f54 (Modified)

    private void showMyRatings() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

<<<<<<< HEAD
        // Build a dialog that shows a RecyclerView of all reviews this user has written
=======
>>>>>>> 93c6f54 (Modified)
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_my_ratings, null);
        RecyclerView rv = dialogView.findViewById(R.id.rv_my_ratings);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_no_ratings);

        List<Review> myReviews = new ArrayList<>();
<<<<<<< HEAD
        ReviewAdapter adapter = new ReviewAdapter(requireContext(), myReviews);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("⭐ My Ratings")
=======
        ReviewAdapter adapter = new ReviewAdapter(requireContext(), myReviews, user.getUid());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        adapter.setOnDeleteClickListener((review, position) -> new AlertDialog.Builder(requireContext())
                .setTitle("Delete Review")
                .setMessage("Delete this review?")
                .setPositiveButton("Delete", (d, w) -> {
                    if (position < 0 || position >= myReviews.size()) return;
                    Review removed = myReviews.remove(position);
                    adapter.updateReviews(myReviews);
                    boolean empty = myReviews.isEmpty();
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    rv.setVisibility(empty ? View.GONE : View.VISIBLE);

                    FirebaseHelper.getInstance().deleteReview(removed, v -> {
                        if (isAdded()) requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Review deleted", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        });
                    }, e -> {
                        if (isAdded()) requireActivity().runOnUiThread(() -> {
                            myReviews.add(position, removed);
                            adapter.updateReviews(myReviews);
                            boolean revertedEmpty = myReviews.isEmpty();
                            tvEmpty.setVisibility(revertedEmpty ? View.VISIBLE : View.GONE);
                            rv.setVisibility(revertedEmpty ? View.GONE : View.VISIBLE);
                            Toast.makeText(requireContext(),
                                    e.getMessage() != null ? e.getMessage() : "Failed to delete review",
                                    Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show());
        adapter.setOnReviewClickListener((review, position) -> openPlaceFromGemId(review.getGemId()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("My Ratings")
>>>>>>> 93c6f54 (Modified)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();

<<<<<<< HEAD
        // Load from Firestore
=======
>>>>>>> 93c6f54 (Modified)
        FirebaseHelper.getInstance().getDb()
                .collection(FirebaseHelper.COLLECTION_REVIEWS)
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    myReviews.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
<<<<<<< HEAD
                        Review r = doc.toObject(Review.class);
                        if (r != null) {
                            r.setId(doc.getId());
                            myReviews.add(r);
                        }
                    }
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        adapter.updateReviews(myReviews);
                        tvEmpty.setVisibility(myReviews.isEmpty() ? View.VISIBLE : View.GONE);
                        rv.setVisibility(myReviews.isEmpty() ? View.GONE : View.VISIBLE);
                    });
                });
    }

    // ── Notifications dialog ──────────────────────────────────────────────

    private void showNotificationsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("🔔 Notifications")
                .setMessage("You have no new notifications.\n\nYou will be notified when:\n• Someone reviews your place\n• Your place gets upvoted\n• New places are added near you")
                .setPositiveButton("OK", null)
                .show();
    }

    // ── Help & Support dialog ─────────────────────────────────────────────

    private void showHelpDialog() {
        String[] options = {
                "📧 Contact Us",
                "📖 How to add a place",
                "⭐ How to leave a review",
                "🗺️ How to use the map",
                "🔒 Privacy Policy"
        };
        new AlertDialog.Builder(requireContext())
                .setTitle("❓ Help & Support")
                .setMessage("Need help? Contact our team:\n📧 mariam.kafel@outlook.com\n📧 reem.diab02@lau.edu\n📧 hala.elakhrass@lau.edu")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Open email with all three team members
=======
                        Review review = doc.toObject(Review.class);
                        if (review != null) {
                            review.setId(doc.getId());
                            myReviews.add(review);
                        }
                    }
                    hydrateReviewGemNames(myReviews, () -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapter.updateReviews(myReviews);
                            boolean empty = myReviews.isEmpty();
                            tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                            rv.setVisibility(empty ? View.GONE : View.VISIBLE);
                        });
                    });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Failed to load ratings", Toast.LENGTH_SHORT).show());
                });
    }

    private void showNotificationsDialog() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Notifications")
                .setMessage("Loading...")
                .setPositiveButton("Close", null)
                .create();
        dialog.show();

        FirebaseHelper.getInstance().fetchGemsByUser(user.getUid(), snap -> {
            List<DocumentSnapshot> gemDocs = snap.getDocuments();
            if (gemDocs.isEmpty()) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        dialog.setMessage("You have no notifications yet.\n\nAdd a place to start receiving activity."));
                return;
            }

            List<String> notifications = new ArrayList<>();
            AtomicInteger pending = new AtomicInteger(gemDocs.size() * 2);

            Runnable finish = () -> {
                if (pending.decrementAndGet() != 0 || !isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (notifications.isEmpty()) {
                        dialog.setMessage("You have no new notifications.\n\nYou will be notified when:\n• Someone reviews your place\n• Your place gets upvoted");
                    } else {
                        StringBuilder builder = new StringBuilder();
                        for (String item : notifications) {
                            if (builder.length() > 0) builder.append("\n\n");
                            builder.append("• ").append(item);
                        }
                        dialog.setMessage(builder.toString());
                    }
                });
            };

            for (DocumentSnapshot gemDoc : gemDocs) {
                String gemId = gemDoc.getId();
                String gemName = gemDoc.getString("name");
                if (gemName == null || gemName.trim().isEmpty()) gemName = "your place";
                String finalGemName = gemName;

                FirebaseHelper.getInstance().fetchReviewsForGem(gemId, reviewsSnap -> {
                    for (DocumentSnapshot reviewDoc : reviewsSnap.getDocuments()) {
                        Review review = reviewDoc.toObject(Review.class);
                        if (review != null && !user.getUid().equals(review.getUserId())) {
                            String reviewer = review.getUserName() != null && !review.getUserName().trim().isEmpty()
                                    ? review.getUserName() : "Someone";
                            notifications.add(reviewer + " reviewed " + finalGemName + ".");
                        }
                    }
                    finish.run();
                }, e -> finish.run());

                FirebaseHelper.getInstance().getDb()
                        .collection(FirebaseHelper.COLLECTION_GEMS)
                        .document(gemId)
                        .collection(FirebaseHelper.SUBCOLLECTION_VOTES)
                        .whereEqualTo("value", "upvote")
                        .get()
                        .addOnSuccessListener(voteSnap -> {
                            int upvoteCount = 0;
                            for (DocumentSnapshot voteDoc : voteSnap.getDocuments()) {
                                if (!user.getUid().equals(voteDoc.getId())) upvoteCount++;
                            }
                            if (upvoteCount > 0) {
                                notifications.add(finalGemName + " received " + upvoteCount + " upvote"
                                        + (upvoteCount == 1 ? "" : "s") + ".");
                            }
                            finish.run();
                        })
                        .addOnFailureListener(e -> finish.run());
            }
        }, e -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    dialog.setMessage("Failed to load notifications."));
        });
    }

    private void showNotificationsListDialog() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_notifications, null);
        RecyclerView rv = dialogView.findViewById(R.id.rv_notifications);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_no_notifications);

        List<AppNotification> notifications = new ArrayList<>();
        NotificationAdapter adapter = new NotificationAdapter(requireContext(), notifications);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        adapter.setOnNotificationClickListener((notification, position) -> openPlaceFromGemId(notification.getGemId()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Notifications")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();

        FirebaseHelper.getInstance().fetchNotifications(user.getUid(), snap -> {
            notifications.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                AppNotification notification = doc.toObject(AppNotification.class);
                if (notification != null) {
                    notification.setId(doc.getId());
                    notifications.add(notification);
                }
            }

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                adapter.updateNotifications(notifications);
                boolean empty = notifications.isEmpty();
                tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                rv.setVisibility(empty ? View.GONE : View.VISIBLE);
            });
        }, e -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show());
        });
    }

    private void hydrateReviewGemNames(List<Review> reviews, Runnable onComplete) {
        if (reviews.isEmpty()) {
            onComplete.run();
            return;
        }

        AtomicInteger remaining = new AtomicInteger(reviews.size());
        for (Review review : reviews) {
            if (review.getGemId() == null || review.getGemId().trim().isEmpty()) {
                if (remaining.decrementAndGet() == 0) onComplete.run();
                continue;
            }

            FirebaseHelper.getInstance().fetchGemById(review.getGemId(), snap -> {
                if (snap.exists()) {
                    String gemName = snap.getString("name");
                    if (gemName != null && !gemName.trim().isEmpty()) {
                        review.setGemName(gemName);
                    }
                }
                if (remaining.decrementAndGet() == 0) onComplete.run();
            }, e -> {
                if (remaining.decrementAndGet() == 0) onComplete.run();
            });
        }
    }

    private void openPlaceFromGemId(String gemId) {
        if (gemId == null || gemId.trim().isEmpty()) return;

        FirebaseHelper.getInstance().fetchGemById(gemId, snap -> {
            if (!snap.exists()) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show());
                return;
            }

            Place place = snap.toObject(Place.class);
            if (place == null) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Post not found", Toast.LENGTH_SHORT).show());
                return;
            }

            place.setId(snap.getId());
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    startActivity(PlaceDetailsActivity.createIntent(requireContext(), place)));
        }, e -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "Failed to open post", Toast.LENGTH_SHORT).show());
        });
    }

    private void showHelpDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_help_support, null);
        TextView tvMessage = dialogView.findViewById(R.id.tv_help_message);
        tvMessage.setText("Need help? Contact our team:\n\nmariam.kafel@outlook.com\nreem.diab02@lau.edu\nhala.elakhrass@lau.edu");

        String[] options = new String[] {
                "Contact Us",
                "How to add a place",
                "How to leave a review",
                "Privacy Policy"
        };

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
>>>>>>> 93c6f54 (Modified)
                            Intent email = new Intent(Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:mariam.kafel@outlook.com,reem.diab02@lau.edu,hala.elakhrass@lau.edu"));
                            email.putExtra(Intent.EXTRA_SUBJECT, "HiddenSpot Support");
                            email.putExtra(Intent.EXTRA_TEXT, "Hi HiddenSpot Team,\n\n");
                            startActivity(Intent.createChooser(email, "Send Email"));
                            break;
                        case 1:
                            showInfoDialog("How to Add a Place",
                                    "1. Tap the '+' button in the bottom navigation\n" +
                                            "2. Fill in the place name, category, city and address\n" +
                                            "3. Add a photo from your gallery or camera\n" +
                                            "4. Write a description about what makes it special\n" +
                                            "5. Tap 'Share This Hidden Gem' to submit");
                            break;
                        case 2:
                            showInfoDialog("How to Leave a Review",
<<<<<<< HEAD
                                    "1. Tap on any place card to open its details\n" +
                                            "2. Scroll down to the Reviews section\n" +
                                            "3. Tap the stars to give a rating (1-5)\n" +
                                            "4. Write an optional comment\n" +
                                            "5. Tap 'Submit Review'");
                            break;
                        case 3:
                            showInfoDialog("How to Use the Map",
                                    "1. Tap on any place card to open its details\n" +
                                            "2. Tap the green 'Open in Maps' button\n" +
                                            "3. Google Maps will open with the location pinned\n" +
                                            "4. You can get directions from your current location");
                            break;
                        case 4:
                            showInfoDialog("Privacy Policy",
                                    "HiddenSpot collects minimal data:\n\n" +
                                            "• Your email address (for login)\n" +
                                            "• Your display name\n" +
                                            "• Places you submit\n" +
                                            "• Reviews you write\n\n" +
                                            "We do not sell your data to third parties.\n" +
                                            "All data is stored securely on Firebase.");
                            break;
                    }
                })
=======
                                    "1. Open any place details screen\n" +
                                            "2. Scroll to the Reviews section\n" +
                                            "3. Select a star rating\n" +
                                            "4. Add an optional comment\n" +
                                            "5. Tap 'Submit Review'");
                            break;
                        case 3:
                            showInfoDialog("Privacy Policy",
                                    "HiddenSpot collects minimal data:\n\n" +
                                            "• Your email address\n" +
                                            "• Your display name\n" +
                                            "• Places you submit\n" +
                                            "• Reviews you write\n\n" +
                                            "All data is stored on Firebase.");
                            break;
                        default:
                            break;
                    }
                })
                .setPositiveButton("Close", null)
>>>>>>> 93c6f54 (Modified)
                .show();
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

<<<<<<< HEAD
    // ── Helpers ───────────────────────────────────────────────────────────

    private void setMenuRow(LinearLayout row, int iconId, int labelId, String emoji, String label) {
        if (row == null) return;
        TextView tvIcon = row.findViewById(iconId);
        TextView tvLabel = row.findViewById(labelId);
        if (tvIcon != null) tvIcon.setText(emoji);
=======
    private void setMenuRow(LinearLayout row, String icon, String label) {
        if (row == null) return;
        TextView tvIcon = row.findViewById(R.id.tv_row_icon);
        TextView tvLabel = row.findViewById(R.id.tv_row_label);
        if (tvIcon != null) tvIcon.setText(icon);
>>>>>>> 93c6f54 (Modified)
        if (tvLabel != null) tvLabel.setText(label);
    }

    @Override
    public void onRequestPermissionsResult(int req, @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == REQUEST_PERM && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
