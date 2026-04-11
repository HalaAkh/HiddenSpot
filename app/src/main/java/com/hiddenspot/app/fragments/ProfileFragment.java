package com.hiddenspot.app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hiddenspot.app.R;
import com.hiddenspot.app.activities.AuthActivity;
import com.hiddenspot.app.activities.EditProfileActivity;
import com.hiddenspot.app.activities.PlaceDetailsActivity;
import com.hiddenspot.app.adapters.NotificationAdapter;
import com.hiddenspot.app.adapters.PlaceAdapter;
import com.hiddenspot.app.adapters.ReviewAdapter;
import com.hiddenspot.app.models.AppNotification;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.models.Review;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private TextView tvUsername;
    private TextView tvBio;
    private TextView tvAvatarLetter;
    private TextView tvPostsCount;
    private TextView tvFavoritesCount;
    private TextView tvStatPlaces;
    private TextView tvStatLikes;
    private TextView tvStatRating;
    private TextView tvNoPosts;
    private RecyclerView rvMyPosts;
    private LinearLayout rowEditProfile;
    private LinearLayout rowRatings;
    private LinearLayout rowNotifications;
    private LinearLayout rowHelp;
    private com.google.android.material.button.MaterialButton btnLogout;
    private CircleImageView ivAvatar;
    private ImageView btnChangePhoto;
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
        tvBio = view.findViewById(R.id.tv_bio);
        tvAvatarLetter = view.findViewById(R.id.tv_avatar_letter);
        tvPostsCount = view.findViewById(R.id.tv_posts_count);
        tvFavoritesCount = view.findViewById(R.id.tv_favorites_count);
        tvStatPlaces = view.findViewById(R.id.tv_stat_places);
        tvStatLikes = view.findViewById(R.id.tv_stat_likes);
        tvStatRating = view.findViewById(R.id.tv_stat_rating);
        tvNoPosts = view.findViewById(R.id.tv_no_posts);
        rvMyPosts = view.findViewById(R.id.rv_my_posts);
        btnLogout = view.findViewById(R.id.btn_logout);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);
        rowEditProfile = view.findViewById(R.id.row_edit_profile);
        rowRatings = view.findViewById(R.id.row_ratings);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowHelp = view.findViewById(R.id.row_help);

        setMenuRow(rowEditProfile, "✏️", "Edit Profile");
        setMenuRow(rowRatings, "⭐", "My Ratings");
        setMenuRow(rowNotifications, "🔔", "Notifications");
        setMenuRow(rowHelp, "❓", "Help & Support");

        postsAdapter = new PlaceAdapter(requireContext(), myPosts);
        postsAdapter.setShowDeleteButton(true);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(postsAdapter);
        rvMyPosts.setNestedScrollingEnabled(false);
        postsAdapter.setOnFavoriteClickListener((place, pos) -> {
            String uid = FirebaseHelper.getInstance().getCurrentUser() != null
                    ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
            if (uid == null || place.getId() == null) return;

            //toggle fav state
            boolean newState = !place.isFavorited();
            place.setFavorited(newState);
            // just refresh the item in recycleview
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
        loadUserProfile();

        btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            Intent i = new Intent(requireActivity(), AuthActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        View.OnClickListener openEditProfile = v ->
                startActivity(new Intent(requireActivity(), EditProfileActivity.class));
        rowEditProfile.setOnClickListener(openEditProfile);
        btnChangePhoto.setOnClickListener(openEditProfile);
        rowRatings.setOnClickListener(v -> showMyRatings());
        rowNotifications.setOnClickListener(v -> showNotificationsListDialog());
        rowHelp.setOnClickListener(v -> showHelpDialog());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

        resetProfileUi();

        FirebaseHelper.getInstance().fetchUserProfile(user.getUid(), profileSnap -> {
            String name = profileSnap.getString("displayName");
            String avatarUrl = profileSnap.getString("avatarUrl");
            String bio = profileSnap.getString("bio");

            if (name == null || name.trim().isEmpty()) {
                name = user.getDisplayName();
            }
            if (name == null || name.trim().isEmpty()) {
                name = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";
            }

            String finalName = name;
            String finalAvatarUrl = avatarUrl != null ? avatarUrl
                    : (user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
            String finalBio = bio != null ? bio.trim() : "";
            requireActivity().runOnUiThread(() ->
                    bindProfileHeader(finalName, finalBio, finalAvatarUrl));
        }, e -> {
            String fallbackName = user.getDisplayName();
            if (fallbackName == null || fallbackName.trim().isEmpty()) {
                fallbackName = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";
            }
            String avatarUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "";
            String finalFallbackName = fallbackName;
            requireActivity().runOnUiThread(() ->
                    bindProfileHeader(finalFallbackName, "", avatarUrl));
        });

        FirebaseHelper.getInstance().fetchSavedGemIds(user.getUid(), savesSnap -> {
            Set<String> savedIds = new HashSet<>();
            for (DocumentSnapshot doc : savesSnap.getDocuments()) {
                String gemId = doc.getString("gemId");
                if (gemId != null) savedIds.add(gemId);
            }

            FirebaseHelper.getInstance().fetchAllGems(gemsSnap -> {
                Set<String> validGemIds = new HashSet<>();
                for (DocumentSnapshot doc : gemsSnap.getDocuments()) {
                    validGemIds.add(doc.getId());
                }

                int favorites = 0;
                for (String savedId : savedIds) {
                    if (validGemIds.contains(savedId)) favorites++;
                }
                final int finalFavorites = favorites;
                requireActivity().runOnUiThread(() ->
                        tvFavoritesCount.setText(getString(R.string.favorites_count_format, finalFavorites)));

                FirebaseHelper.getInstance().fetchGemsByUser(user.getUid(), snap -> {
                    myPosts.clear();
                    int likes = 0;
                    double totalRating = 0;
                    int ratedCount = 0;

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Place p = doc.toObject(Place.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            p.setFavorited(savedIds.contains(doc.getId()));
                            myPosts.add(p);
                            likes += p.getUpvotes();
                            if (p.getRating() > 0) {
                                totalRating += p.getRating();
                                ratedCount++;
                            }
                        }
                    }

                    final int posts = myPosts.size();
                    final int totalLikes = likes;
                    final int totalRated = ratedCount;
                    final double avg = totalRated > 0 ? totalRating / totalRated : 0;

                    requireActivity().runOnUiThread(() -> {
                        postsAdapter.updatePlaces(myPosts);
                        updatePostsEmptyState();
                        tvStatPlaces.setText(String.valueOf(posts));
                        tvStatLikes.setText(String.valueOf(totalLikes));
                        tvStatRating.setText(totalRated > 0
                                ? String.format(Locale.getDefault(), "%.1f", avg) : "—");
                        tvPostsCount.setText(getString(R.string.posts_count_format, posts));
                    });
                }, e -> Toast.makeText(requireContext(), "Error loading posts", Toast.LENGTH_SHORT).show());
            }, e -> requireActivity().runOnUiThread(() ->
                    tvFavoritesCount.setText(getString(R.string.favorites_count_format, 0))));
        }, e -> requireActivity().runOnUiThread(() ->
                tvFavoritesCount.setText(getString(R.string.favorites_count_format, 0))));
    }

    private void resetProfileUi() {
        tvUsername.setText("");
        tvBio.setText("");
        tvPostsCount.setText("");
        tvFavoritesCount.setText("");
        tvStatPlaces.setText("0");
        tvStatLikes.setText("0");
        tvStatRating.setText("—");
        tvAvatarLetter.setText("");
        tvAvatarLetter.setVisibility(View.VISIBLE);
        ivAvatar.setImageDrawable(null);
        myPosts.clear();
        if (postsAdapter != null) {
            postsAdapter.updatePlaces(myPosts);
        }
        updatePostsEmptyState();
    }

    private void bindProfileHeader(String name, String bio, String avatarUrl) {
        tvUsername.setText(name);
        tvBio.setText(bio);
        tvAvatarLetter.setText(name.substring(0, 1).toUpperCase(Locale.getDefault()));

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            tvAvatarLetter.setVisibility(View.GONE);
            if (avatarUrl.startsWith("http")) {
                Glide.with(this).load(avatarUrl).centerCrop().into(ivAvatar);
            } else {
                try {
                    byte[] imageBytes = Base64.decode(avatarUrl, Base64.DEFAULT);
                    Glide.with(this).load(imageBytes).centerCrop().into(ivAvatar);
                } catch (Exception e) {
                    ivAvatar.setImageDrawable(null);
                    tvAvatarLetter.setVisibility(View.VISIBLE);
                }
            }
        } else {
            ivAvatar.setImageDrawable(null);
            tvAvatarLetter.setVisibility(View.VISIBLE);
        }
    }

    private void showDeletePostDialog(Place place, int position) {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null || place == null || place.getId() == null) return;
        if (!user.getUid().equals(place.getUserId())) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Delete \"" + place.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deletePost(place, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost(Place place, int position) {
        if (position < 0 || position >= myPosts.size()) return;
        Place removed = myPosts.remove(position);
        postsAdapter.updatePlaces(myPosts);
        updatePostsEmptyState();
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
                updatePostsEmptyState();
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

    private void updatePostsEmptyState() {
        if (tvNoPosts == null || rvMyPosts == null) return;
        boolean empty = myPosts.isEmpty();
        tvNoPosts.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvMyPosts.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showMyRatings() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_my_ratings, null);
        RecyclerView rv = dialogView.findViewById(R.id.rv_my_ratings);
        TextView tvEmpty = dialogView.findViewById(R.id.tv_no_ratings);

        List<Review> myReviews = new ArrayList<>();
        ReviewAdapter adapter = new ReviewAdapter(requireContext(), myReviews, user.getUid());
        adapter.setDeleteEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        adapter.setOnReviewClickListener((review, position) -> openPlaceFromGemId(review.getGemId()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("My Ratings")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();

        FirebaseHelper.getInstance().getDb()
                .collection(FirebaseHelper.COLLECTION_REVIEWS)
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(snap -> {
                    myReviews.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
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
        String[] options = new String[] {
                "Contact Us",
                "How to add a place",
                "How to leave a review",
                "Privacy Policy"
        };

        new AlertDialog.Builder(requireContext())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            Intent email = new Intent(Intent.ACTION_SENDTO,
                                    Uri.parse("mailto:mariam.kafel@outlook.com,reem.diab02@lau.edu,hala.elakhrass@lau.edu"));
                            email.putExtra(Intent.EXTRA_SUBJECT, "HiddenSpot Support");
                            email.putExtra(Intent.EXTRA_TEXT, "Hi HiddenSpot Team,\n\n");
                            startActivity(Intent.createChooser(email, "Send Email"));
                            break;
                        case 1:
                            showInfoDialog("How to Add a Place",
                                    "1. Tap the '+' button in the bottom navigation\n"
                                            + "2. Fill in the place name, category, city and address\n"
                                            + "3. Add a photo from your gallery or camera\n"
                                            + "4. Write a description about what makes it special\n"
                                            + "5. Tap 'Share This Hidden Gem' to submit");
                            break;
                        case 2:
                            showInfoDialog("How to Leave a Review",
                                    "1. Open any place details screen\n"
                                            + "2. Scroll to the Reviews section\n"
                                            + "3. Select a star rating\n"
                                            + "4. Add an optional comment\n"
                                            + "5. Tap 'Submit Review'");
                            break;
                        case 3:
                            showInfoDialog("Privacy Policy",
                                    "HiddenSpot collects minimal data:\n\n"
                                            + "• Your email address\n"
                                            + "• Your display name\n"
                                            + "• Places you submit\n"
                                            + "• Reviews you write\n\n"
                                            + "All data is stored on Firebase.");
                            break;
                        default:
                            break;
                    }
                })
                .setPositiveButton("Close", null)
                .show();
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

    private void setMenuRow(LinearLayout row, String icon, String label) {
        if (row == null) return;
        TextView tvIcon = row.findViewById(R.id.tv_row_icon);
        TextView tvLabel = row.findViewById(R.id.tv_row_label);
        if (tvIcon != null) tvIcon.setText(icon);
        if (tvLabel != null) tvLabel.setText(label);
    }
}
