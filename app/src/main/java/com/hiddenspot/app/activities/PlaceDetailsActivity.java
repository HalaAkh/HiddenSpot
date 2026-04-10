package com.hiddenspot.app.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.hiddenspot.app.R;
import com.hiddenspot.app.utils.FirebaseHelper;

public class PlaceDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_PLACE_ID        = "place_id";
    public static final String EXTRA_PLACE_NAME      = "place_name";
    public static final String EXTRA_PLACE_IMAGE     = "place_image";
    public static final String EXTRA_PLACE_CITY      = "place_city";
    public static final String EXTRA_PLACE_ADDRESS   = "place_address";
    public static final String EXTRA_PLACE_PHONE     = "place_phone";
    public static final String EXTRA_PLACE_DESC      = "place_desc";
    public static final String EXTRA_PLACE_CATEGORY  = "place_category";
    public static final String EXTRA_PLACE_RATING    = "place_rating";
    public static final String EXTRA_PLACE_UPVOTES   = "place_upvotes";
    public static final String EXTRA_PLACE_DOWNVOTES = "place_downvotes";
    public static final String EXTRA_PLACE_FAVORITED = "place_favorited";
    public static final String EXTRA_POSTER_NAME     = "poster_name";
    public static final String EXTRA_POSTER_AVATAR   = "poster_avatar";
    public static final String EXTRA_POSTED_DATE     = "posted_date";

    private String placeId;
    private int upvotes, downvotes;
    private String voteState = "none";
    private boolean isFavorited;

    private android.widget.ImageView ivHero;
    private TextView tvCategoryBadge, tvPlaceName, tvAddress, tvPhone;
    private TextView tvRating, tvDescription, tvPosterName, tvPostedDate, tvPosterInitial;
    private de.hdodenhof.circleimageview.CircleImageView ivPosterAvatar;
    private LinearLayout layoutPhone;
    private MaterialButton btnUpvote, btnDownvote, btnMaps, btnCall;
    private ImageButton btnBack, btnShare, btnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        bindViews();
        populateFromIntent();
        setupListeners();
        loadCurrentVoteState();
    }

    private void bindViews() {
        ivHero          = findViewById(R.id.iv_hero);
        tvCategoryBadge = findViewById(R.id.tv_category_badge);
        tvPlaceName     = findViewById(R.id.tv_place_name);
        tvAddress       = findViewById(R.id.tv_address);
        tvPhone         = findViewById(R.id.tv_phone);
        tvRating        = findViewById(R.id.tv_rating);
        tvDescription   = findViewById(R.id.tv_description);
        tvPosterName    = findViewById(R.id.tv_poster_name);
        tvPostedDate    = findViewById(R.id.tv_posted_date);
        tvPosterInitial = findViewById(R.id.tv_poster_initial);
        ivPosterAvatar  = findViewById(R.id.iv_poster_avatar);
        layoutPhone     = findViewById(R.id.layout_phone);
        btnUpvote       = findViewById(R.id.btn_upvote);
        btnDownvote     = findViewById(R.id.btn_downvote);
        btnMaps         = findViewById(R.id.btn_maps);
        btnCall         = findViewById(R.id.btn_call);
        btnBack         = findViewById(R.id.btn_back);
        btnShare        = findViewById(R.id.btn_share);
        btnFavorite     = findViewById(R.id.btn_favorite);
    }

    private void populateFromIntent() {
        Intent intent = getIntent();
        placeId     = intent.getStringExtra(EXTRA_PLACE_ID);
        upvotes     = intent.getIntExtra(EXTRA_PLACE_UPVOTES, 0);
        downvotes   = intent.getIntExtra(EXTRA_PLACE_DOWNVOTES, 0);
        isFavorited = intent.getBooleanExtra(EXTRA_PLACE_FAVORITED, false);

        String name     = intent.getStringExtra(EXTRA_PLACE_NAME);
        String image    = intent.getStringExtra(EXTRA_PLACE_IMAGE);
        String city     = intent.getStringExtra(EXTRA_PLACE_CITY);
        String address  = intent.getStringExtra(EXTRA_PLACE_ADDRESS);
        String phone    = intent.getStringExtra(EXTRA_PLACE_PHONE);
        String desc     = intent.getStringExtra(EXTRA_PLACE_DESC);
        String category = intent.getStringExtra(EXTRA_PLACE_CATEGORY);
        double rating   = intent.getDoubleExtra(EXTRA_PLACE_RATING, 0.0);
        String poster   = intent.getStringExtra(EXTRA_POSTER_NAME);
        String posterAvatar = intent.getStringExtra(EXTRA_POSTER_AVATAR);
        String date     = intent.getStringExtra(EXTRA_POSTED_DATE);

        if (image != null && !image.isEmpty()) {
            if (!image.startsWith("http")) {
                try {
                    byte[] imageBytes = Base64.decode(image, Base64.DEFAULT);
                    Glide.with(this)
                            .load(imageBytes)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop()
                            .into(ivHero);
                } catch (Exception e) {
                    ivHero.setImageResource(R.color.muted);
                }
            } else {
                Glide.with(this).load(image)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop().into(ivHero);
            }
        }

        tvCategoryBadge.setText(getCategoryDisplay(category));
        tvCategoryBadge.setBackgroundColor(getCategoryColor(category));
        tvPlaceName.setText(name);
        tvAddress.setText((address != null ? address : "") + (city != null ? ", " + city : ""));
        tvRating.setText(String.format("%.1f", rating));
        tvDescription.setText(desc);

        if (phone != null && !phone.isEmpty()) {
            layoutPhone.setVisibility(View.VISIBLE);
            btnCall.setVisibility(View.VISIBLE);
            tvPhone.setText(phone);
            final String p = phone;
            tvPhone.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p))));
            btnCall.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + p))));
        }

        if (poster != null && !poster.isEmpty()) {
            tvPosterName.setText(poster);
            tvPosterInitial.setText(poster.substring(0, 1).toUpperCase());
        }
        updatePosterAvatar(posterAvatar);
        if (date != null) tvPostedDate.setText("Posted on " + date);

        updateVoteButtons();
        updateFavoriteIcon();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "Check out: " + tvPlaceName.getText() + " 📍 via HiddenSpot");
            startActivity(Intent.createChooser(i, "Share via"));
        });

        btnFavorite.setOnClickListener(v -> {
            isFavorited = !isFavorited;
            updateFavoriteIcon();
            String uid = FirebaseHelper.getInstance().getCurrentUser() != null
                    ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
            if (uid != null && placeId != null) {
                if (isFavorited) {
                    FirebaseHelper.getInstance().saveGem(uid, placeId, s -> {}, e -> {});
                    Toast.makeText(this, "Saved to favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseHelper.getInstance().unsaveGem(uid, placeId, s -> {}, e -> {});
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUpvote.setOnClickListener(v -> handleVote("up"));
        btnDownvote.setOnClickListener(v -> handleVote("down"));

        btnMaps.setOnClickListener(v -> {
            String addr = tvAddress.getText().toString();
            Uri gmmUri = Uri.parse("geo:0,0?q=" + Uri.encode(addr));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) startActivity(mapIntent);
            else startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com/?q=" + Uri.encode(addr))));
        });
    }

    private void handleVote(String type) {
        String uid = FirebaseHelper.getInstance().getCurrentUser() != null
                ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
        if (placeId == null || uid == null) return;

        String previousVoteState = voteState;
        int previousUpvotes = upvotes;
        int previousDownvotes = downvotes;
        String newVoteState = voteState.equals(type) ? "none" : type;
        applyVoteStateChange(voteState, newVoteState);
        updateVoteButtons();
        btnUpvote.setEnabled(false);
        btnDownvote.setEnabled(false);

        FirebaseHelper.getInstance().setGemVote(placeId, uid,
                "none".equals(newVoteState) ? null : toVoteValue(newVoteState),
                v -> runOnUiThread(() -> {
                    btnUpvote.setEnabled(true);
                    btnDownvote.setEnabled(true);
                }),
                e -> runOnUiThread(() -> {
                    voteState = previousVoteState;
                    upvotes = previousUpvotes;
                    downvotes = previousDownvotes;
                    updateVoteButtons();
                    btnUpvote.setEnabled(true);
                    btnDownvote.setEnabled(true);
                    Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Failed to update vote",
                            Toast.LENGTH_SHORT).show();
                }));
    }

    private void updateVoteButtons() {
        btnUpvote.setText(String.valueOf(upvotes));
        btnDownvote.setText(String.valueOf(downvotes));
        if (voteState.equals("up")) {
            btnUpvote.setBackgroundResource(R.drawable.bg_vote_up_active);
            btnUpvote.setTextColor(getResources().getColor(R.color.upvote_green, null));
            btnUpvote.setIconTint(ColorStateList.valueOf(
                    getResources().getColor(R.color.upvote_green, null)));
        } else {
            btnUpvote.setBackgroundResource(R.drawable.bg_chip_inactive);
            btnUpvote.setTextColor(getResources().getColor(R.color.muted_foreground, null));
            btnUpvote.setIconTint(ColorStateList.valueOf(
                    getResources().getColor(R.color.muted_foreground, null)));
        }
        if (voteState.equals("down")) {
            btnDownvote.setBackgroundResource(R.drawable.bg_vote_down_active);
            btnDownvote.setTextColor(getResources().getColor(R.color.downvote_red, null));
            btnDownvote.setIconTint(ColorStateList.valueOf(
                    getResources().getColor(R.color.downvote_red, null)));
        } else {
            btnDownvote.setBackgroundResource(R.drawable.bg_chip_inactive);
            btnDownvote.setTextColor(getResources().getColor(R.color.muted_foreground, null));
            btnDownvote.setIconTint(ColorStateList.valueOf(
                    getResources().getColor(R.color.muted_foreground, null)));
        }
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorited ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
    }

    private void loadCurrentVoteState() {
        String uid = FirebaseHelper.getInstance().getCurrentUser() != null
                ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
        if (placeId == null || uid == null) return;

        FirebaseHelper.getInstance().fetchGemVote(placeId, uid, snap -> runOnUiThread(() -> {
            String storedVote = snap.getString("value");
            if ("upvote".equals(storedVote)) voteState = "up";
            else if ("downvote".equals(storedVote)) voteState = "down";
            else voteState = "none";
            updateVoteButtons();
        }), e -> runOnUiThread(this::updateVoteButtons));
    }

    private void applyVoteStateChange(String oldState, String newState) {
        if ("up".equals(oldState)) upvotes = Math.max(0, upvotes - 1);
        if ("down".equals(oldState)) downvotes = Math.max(0, downvotes - 1);
        if ("up".equals(newState)) upvotes += 1;
        if ("down".equals(newState)) downvotes += 1;
        voteState = newState;
    }

    private String toVoteValue(String state) {
        return "up".equals(state) ? "upvote" : "downvote";
    }

    private void updatePosterAvatar(String posterAvatar) {
        if (posterAvatar != null && !posterAvatar.trim().isEmpty()) {
            try {
                tvPosterInitial.setVisibility(View.GONE);
                if (posterAvatar.startsWith("http")) {
                    Glide.with(this).load(posterAvatar).centerCrop().into(ivPosterAvatar);
                } else {
                    byte[] imageBytes = Base64.decode(posterAvatar, Base64.DEFAULT);
                    Glide.with(this).load(imageBytes).centerCrop().into(ivPosterAvatar);
                }
            } catch (Exception e) {
                ivPosterAvatar.setImageDrawable(null);
                tvPosterInitial.setVisibility(View.VISIBLE);
            }
        } else {
            ivPosterAvatar.setImageDrawable(null);
            tvPosterInitial.setVisibility(View.VISIBLE);
        }
    }

    private String getCategoryDisplay(String cat) {
        if (cat == null) return "";
        switch (cat) {
            case "Restaurant": return "🍽️ Restaurant";
            case "Garden":     return "🌿 Garden";
            case "Café":       return "☕ Café";
            case "Viewpoint":  return "🏔️ Viewpoint";
            case "Park":       return "🌳 Park";
            case "Beach":      return "🏖️ Beach";
            case "Library":    return "📚 Library";
            case "Shop":       return "🛍️ Shop";
            case "Historical": return "🏛️ Historical";
            default:           return cat;
        }
    }

    private int getCategoryColor(String cat) {
        if (cat == null) return getResources().getColor(R.color.primary, null);
        switch (cat) {
            case "Restaurant": return getResources().getColor(R.color.badge_restaurant, null);
            case "Garden":     return getResources().getColor(R.color.badge_garden, null);
            case "Café":       return getResources().getColor(R.color.badge_cafe, null);
            case "Viewpoint":  return getResources().getColor(R.color.badge_viewpoint, null);
            case "Park":       return getResources().getColor(R.color.badge_park, null);
            case "Beach":      return getResources().getColor(R.color.badge_beach, null);
            case "Library":    return getResources().getColor(R.color.badge_library, null);
            case "Shop":       return getResources().getColor(R.color.badge_shop, null);
            case "Historical": return getResources().getColor(R.color.badge_historical, null);
            default:           return getResources().getColor(R.color.primary, null);
        }
    }
}
