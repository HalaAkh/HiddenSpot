package com.hiddenspot.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hiddenspot.app.R;
import com.hiddenspot.app.activities.AuthActivity;
import com.hiddenspot.app.adapters.PlaceAdapter;
import com.hiddenspot.app.models.Place;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvAvatarLetter, tvPostsCount;
    private TextView tvStatPlaces, tvStatLikes, tvStatRating;
    private RecyclerView rvMyPosts;
    private LinearLayout rowEditProfile, rowRatings, rowNotifications, rowHelp;
    private com.google.android.material.button.MaterialButton btnLogout;
    private PlaceAdapter postsAdapter;
    private final List<Place> myPosts = new ArrayList<>();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvUsername       = view.findViewById(R.id.tv_username);
        tvAvatarLetter   = view.findViewById(R.id.tv_avatar_letter);
        tvPostsCount     = view.findViewById(R.id.tv_posts_count);
        tvStatPlaces     = view.findViewById(R.id.tv_stat_places);
        tvStatLikes      = view.findViewById(R.id.tv_stat_likes);
        tvStatRating     = view.findViewById(R.id.tv_stat_rating);
        rvMyPosts        = view.findViewById(R.id.rv_my_posts);
        btnLogout        = view.findViewById(R.id.btn_logout);
        rowEditProfile   = view.findViewById(R.id.row_edit_profile);
        rowRatings       = view.findViewById(R.id.row_ratings);
        rowNotifications = view.findViewById(R.id.row_notifications);
        rowHelp          = view.findViewById(R.id.row_help);

        setMenuRow(rowEditProfile,   "✏️", "Edit Profile");
        setMenuRow(rowRatings,       "⭐", "My Ratings");
        setMenuRow(rowNotifications, "🔔", "Notifications");
        setMenuRow(rowHelp,          "❓", "Help & Support");

        postsAdapter = new PlaceAdapter(requireContext(), myPosts);
        rvMyPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMyPosts.setAdapter(postsAdapter);
        rvMyPosts.setNestedScrollingEnabled(false);

        loadUserProfile();

        btnLogout.setOnClickListener(v -> {
            FirebaseHelper.getInstance().signOut();
            Intent i = new Intent(requireActivity(), AuthActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        for (LinearLayout row : new LinearLayout[]{rowEditProfile, rowRatings, rowNotifications, rowHelp})
            row.setOnClickListener(v -> Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show());
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseHelper.getInstance().getCurrentUser();
        if (user == null) return;
        String name = user.getDisplayName();
        if (name == null || name.isEmpty())
            name = user.getEmail() != null ? user.getEmail().split("@")[0] : "User";
        tvUsername.setText(name);
        tvAvatarLetter.setText(name.substring(0, 1).toUpperCase());

        FirebaseHelper.getInstance().fetchGemsByUser(user.getUid(), snap -> {
            myPosts.clear();
            int likes = 0; double totalRating = 0; int ratedCount = 0;
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Place p = doc.toObject(Place.class);
                if (p != null) {
                    p.setId(doc.getId()); myPosts.add(p);
                    likes += p.getLikesCount();
                    if (p.getRating() > 0) { totalRating += p.getRating(); ratedCount++; }
                }
            }
            final int pl = myPosts.size(), lk = likes, rc = ratedCount;
            final double avg = rc > 0 ? totalRating / rc : 0;
            requireActivity().runOnUiThread(() -> {
                postsAdapter.updatePlaces(myPosts);
                tvStatPlaces.setText(String.valueOf(pl));
                tvStatLikes.setText(String.valueOf(lk));
                tvStatRating.setText(rc > 0 ? String.format("%.1f", avg) : "—");
                tvPostsCount.setText(pl + " Posts");
            });
        }, e -> Toast.makeText(requireContext(), "Error loading posts", Toast.LENGTH_SHORT).show());
    }

    private void setMenuRow(LinearLayout row, String emoji, String label) {
        if (row == null) return;
        TextView tvIcon  = row.findViewById(R.id.tv_row_icon);
        TextView tvLabel = row.findViewById(R.id.tv_row_label);
        if (tvIcon  != null) tvIcon.setText(emoji);
        if (tvLabel != null) tvLabel.setText(label);
    }
}
