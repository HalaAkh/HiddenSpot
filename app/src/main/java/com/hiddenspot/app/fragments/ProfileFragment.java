package com.hiddenspot.app.fragments;

import android.content.Intent;
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
import com.hiddenspot.app.adapters.PlaceAdapter;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
        rvMyPosts = view.findViewById(R.id.rv_my_posts);
        btnLogout = view.findViewById(R.id.btn_logout);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        btnChangePhoto = view.findViewById(R.id.btn_change_photo);
        rowEditProfile = view.findViewById(R.id.row_edit_profile);
        rowRatings = view.findViewById(R.id.row_ratings);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowHelp = view.findViewById(R.id.row_help);

        setMenuRow(rowEditProfile, "Edit Profile");
        setMenuRow(rowRatings, "My Ratings");
        setMenuRow(rowNotifications, "Notifications");
        setMenuRow(rowHelp, "Help & Support");

        postsAdapter = new PlaceAdapter(requireContext(), myPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(postsAdapter);
        rvMyPosts.setNestedScrollingEnabled(false);
        postsAdapter.setOnFavoriteClickListener((place, pos) -> {
            String uid = FirebaseHelper.getInstance().getCurrentUser() != null
                    ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
            if (uid == null || place.getId() == null) return;

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

        for (LinearLayout row : new LinearLayout[]{rowRatings, rowNotifications, rowHelp}) {
            row.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show());
        }
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
                            likes += p.getLikesCount();
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

    private void setMenuRow(LinearLayout row, String label) {
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.tv_row_label);
        if (tvLabel != null) tvLabel.setText(label);
    }
}
