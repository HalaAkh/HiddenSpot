package com.hiddenspot.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.hiddenspot.app.R;
import com.hiddenspot.app.adapters.ReviewAdapter;
import com.hiddenspot.app.models.Review;
import com.hiddenspot.app.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class PlaceDetailsActivity extends AppCompatActivity {

    // ── Intent extras (unchanged) ────────────────────────────────────────────
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
    public static final String EXTRA_POSTED_DATE     = "posted_date";

    // ── State ────────────────────────────────────────────────────────────────
    private String placeId, placeAddress, placeCity;
    private int    upvotes, downvotes;
    private String voteState  = "none";
    private boolean isFavorited;

    // ── Views ────────────────────────────────────────────────────────────────
    private android.widget.ImageView ivHero;
    private TextView tvCategoryBadge, tvPlaceName, tvAddress, tvPhone;
    private TextView tvRating, tvDescription, tvPosterName, tvPostedDate, tvPosterInitial;
    private LinearLayout layoutPhone;
    private MaterialButton btnUpvote, btnDownvote, btnMaps, btnCall;
    private ImageButton btnBack, btnShare, btnFavorite;

    // ── Review views ─────────────────────────────────────────────────────────
    private RecyclerView     rvReviews;
    private ReviewAdapter    reviewAdapter;
    private final List<Review> reviewList = new ArrayList<>();
    private RatingBar        ratingBarInput;
    private TextInputEditText etReviewComment;
    private MaterialButton   btnSubmitReview;
    private TextView         tvReviewCount, tvNoReviews;
    private float            myRating = 0f;

    // ── Map button ───────────────────────────────────────────────────────────
    private MaterialButton btnMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);
        bindViews();
        populateFromIntent();
        setupListeners();
        loadReviews();
        checkMyExistingReview();
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
        layoutPhone     = findViewById(R.id.layout_phone);
        btnUpvote       = findViewById(R.id.btn_upvote);
        btnDownvote     = findViewById(R.id.btn_downvote);
        btnMaps         = findViewById(R.id.btn_maps);
        btnCall         = findViewById(R.id.btn_call);
        btnBack         = findViewById(R.id.btn_back);
        btnShare        = findViewById(R.id.btn_share);
        btnFavorite     = findViewById(R.id.btn_favorite);

        // ── NEW: Review views ────────────────────────────────────────────────
        rvReviews        = findViewById(R.id.rv_reviews);
        ratingBarInput   = findViewById(R.id.rating_bar_input);
        etReviewComment  = findViewById(R.id.et_review_comment);
        btnSubmitReview  = findViewById(R.id.btn_submit_review);
        tvReviewCount    = findViewById(R.id.tv_review_count);
        tvNoReviews      = findViewById(R.id.tv_no_reviews);

        // ── NEW: Map view button ─────────────────────────────────────────────
        btnMapView = findViewById(R.id.btn_map_view);

        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
        rvReviews.setNestedScrollingEnabled(false);
    }

    private void populateFromIntent() {
        Intent intent = getIntent();
        placeId       = intent.getStringExtra(EXTRA_PLACE_ID);
        upvotes       = intent.getIntExtra(EXTRA_PLACE_UPVOTES, 0);
        downvotes     = intent.getIntExtra(EXTRA_PLACE_DOWNVOTES, 0);
        isFavorited   = intent.getBooleanExtra(EXTRA_PLACE_FAVORITED, false);

        String name     = intent.getStringExtra(EXTRA_PLACE_NAME);
        String image    = intent.getStringExtra(EXTRA_PLACE_IMAGE);
        placeCity       = intent.getStringExtra(EXTRA_PLACE_CITY);
        placeAddress    = intent.getStringExtra(EXTRA_PLACE_ADDRESS);
        String phone    = intent.getStringExtra(EXTRA_PLACE_PHONE);
        String desc     = intent.getStringExtra(EXTRA_PLACE_DESC);
        String category = intent.getStringExtra(EXTRA_PLACE_CATEGORY);
        double rating   = intent.getDoubleExtra(EXTRA_PLACE_RATING, 0.0);
        String poster   = intent.getStringExtra(EXTRA_POSTER_NAME);
        String date     = intent.getStringExtra(EXTRA_POSTED_DATE);

        // Load hero image
        if (image != null && !image.isEmpty()) {
            if (!image.startsWith("http")) {
                try {
                    byte[] bytes = Base64.decode(image, Base64.DEFAULT);
                    Glide.with(this).load(bytes)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .centerCrop().into(ivHero);
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
        String fullAddress = (placeAddress != null ? placeAddress : "")
                + (placeCity != null ? ", " + placeCity : "");
        tvAddress.setText(fullAddress);
        tvRating.setText(String.format("%.1f", rating));
        tvDescription.setText(desc);

        if (phone != null && !phone.isEmpty()) {
            layoutPhone.setVisibility(View.VISIBLE);
            btnCall.setVisibility(View.VISIBLE);
            tvPhone.setText(phone);
            final String p = phone;
            tvPhone.setOnClickListener(v -> dialPhone(p));
            btnCall.setOnClickListener(v -> dialPhone(p));
        }

        if (poster != null && !poster.isEmpty()) {
            tvPosterName.setText(poster);
            tvPosterInitial.setText(poster.substring(0, 1).toUpperCase());
        }
        if (date != null) tvPostedDate.setText("Posted on " + date);

        updateVoteButtons();
        updateFavoriteIcon();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT,
                    "Check out: " + tvPlaceName.getText() + " 📍 via HiddenSpot");
            startActivity(Intent.createChooser(i, "Share via"));
        });

        btnFavorite.setOnClickListener(v -> {
            isFavorited = !isFavorited;
            updateFavoriteIcon();
            String uid = uid();
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

        // ── Maps button: open MapActivity ────────────────────────────────────
        btnMaps.setOnClickListener(v -> openMaps());

        // ── NEW: Map view button (big button in map section) ─────────────────
        if (btnMapView != null) {
            btnMapView.setOnClickListener(v -> openMaps());
        }

        // ── NEW: Rating bar + review submit ──────────────────────────────────
        if (ratingBarInput != null) {
            ratingBarInput.setOnRatingBarChangeListener((bar, rating, fromUser) -> myRating = rating);
        }

        if (btnSubmitReview != null) {
            btnSubmitReview.setOnClickListener(v -> submitReview());
        }
    }

    // ── Map helper ────────────────────────────────────────────────────────────

    private void openMaps() {
        // Try to open MapActivity first (new embedded map feature)
        try {
            Intent mapIntent = new Intent(this, MapActivity.class);
            mapIntent.putExtra(MapActivity.EXTRA_ADDRESS,
                    (placeAddress != null ? placeAddress : "") +
                            (placeCity != null ? ", " + placeCity : ""));
            mapIntent.putExtra(MapActivity.EXTRA_PLACE_NAME,
                    tvPlaceName.getText().toString());
            startActivity(mapIntent);
        } catch (Exception e) {
            // Fallback: open in Google Maps app / browser
            String addr = tvAddress.getText().toString();
            Uri gmmUri = Uri.parse("geo:0,0?q=" + Uri.encode(addr));
            Intent intent = new Intent(Intent.ACTION_VIEW, gmmUri);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://maps.google.com/?q=" + Uri.encode(addr))));
            }
        }
    }

    // ── Reviews ───────────────────────────────────────────────────────────────

    private void loadReviews() {
        if (placeId == null) return;
        FirebaseHelper.getInstance().fetchReviewsForGem(placeId, snap -> {
            reviewList.clear();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Review r = doc.toObject(Review.class);
                if (r != null) { r.setId(doc.getId()); reviewList.add(r); }
            }
            runOnUiThread(() -> {
                reviewAdapter.updateReviews(reviewList);
                if (tvReviewCount != null)
                    tvReviewCount.setText(reviewList.size() + " review" +
                            (reviewList.size() != 1 ? "s" : ""));
                if (tvNoReviews != null)
                    tvNoReviews.setVisibility(reviewList.isEmpty() ? View.VISIBLE : View.GONE);
                rvReviews.setVisibility(reviewList.isEmpty() ? View.GONE : View.VISIBLE);
            });
        }, e -> { /* silently ignore */ });
    }

    /** Pre-fill the rating bar if this user already reviewed this place. */
    private void checkMyExistingReview() {
        String uid = uid();
        if (uid == null || placeId == null || ratingBarInput == null) return;
        FirebaseHelper.getInstance().fetchMyReview(uid, placeId, existing -> {
            if (existing != null) {
                runOnUiThread(() -> {
                    ratingBarInput.setRating(existing.getRating());
                    myRating = existing.getRating();
                    if (etReviewComment != null && existing.getComment() != null)
                        etReviewComment.setText(existing.getComment());
                    if (btnSubmitReview != null)
                        btnSubmitReview.setText("Update Review");
                });
            }
        }, e -> { /* ignore */ });
    }

    private void submitReview() {
        String uid = uid();
        if (uid == null) {
            Toast.makeText(this, "Please log in to leave a review", Toast.LENGTH_SHORT).show();
            return;
        }
        if (myRating == 0f) {
            Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show();
            return;
        }
        String comment = etReviewComment != null && etReviewComment.getText() != null
                ? etReviewComment.getText().toString().trim() : "";

        String userName = FirebaseHelper.getInstance().getCurrentUser() != null
                ? FirebaseHelper.getInstance().getCurrentUser().getDisplayName() : "Anonymous";
        if (userName == null || userName.isEmpty()) userName = "Anonymous";

        Review review = new Review(placeId, uid, userName, myRating, comment);

        if (btnSubmitReview != null) {
            btnSubmitReview.setEnabled(false);
            btnSubmitReview.setText("Saving…");
        }

        FirebaseHelper.getInstance().addReview(review, v -> runOnUiThread(() -> {
            Toast.makeText(this, "Review saved!", Toast.LENGTH_SHORT).show();
            if (btnSubmitReview != null) {
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Update Review");
            }
            loadReviews();   // Refresh list
        }), e -> runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            if (btnSubmitReview != null) {
                btnSubmitReview.setEnabled(true);
                btnSubmitReview.setText("Submit Review");
            }
        }));
    }

    // ── Votes ─────────────────────────────────────────────────────────────────

    private void handleVote(String type) {
        if (placeId == null) return;
        FirebaseHelper fb = FirebaseHelper.getInstance();
        if (voteState.equals(type)) {
            if (type.equals("up"))   { upvotes--;   fb.removeUpvote(placeId);   }
            else                     { downvotes--; fb.removeDownvote(placeId); }
            voteState = "none";
        } else {
            if (voteState.equals("up"))   { upvotes--;   fb.removeUpvote(placeId);   }
            if (voteState.equals("down")) { downvotes--; fb.removeDownvote(placeId); }
            if (type.equals("up"))   { upvotes++;   fb.upvoteGem(placeId);   }
            else                     { downvotes++; fb.downvoteGem(placeId); }
            voteState = type;
        }
        updateVoteButtons();
    }

    private void updateVoteButtons() {
        btnUpvote.setText(String.valueOf(upvotes));
        btnDownvote.setText(String.valueOf(downvotes));
        if (voteState.equals("up")) {
            btnUpvote.setBackgroundResource(R.drawable.bg_vote_up_active);
            btnUpvote.setTextColor(getResources().getColor(R.color.upvote_green, null));
        } else {
            btnUpvote.setBackgroundResource(R.drawable.bg_chip_inactive);
            btnUpvote.setTextColor(getResources().getColor(R.color.muted_foreground, null));
        }
        if (voteState.equals("down")) {
            btnDownvote.setBackgroundResource(R.drawable.bg_vote_down_active);
            btnDownvote.setTextColor(getResources().getColor(R.color.downvote_red, null));
        } else {
            btnDownvote.setBackgroundResource(R.drawable.bg_chip_inactive);
            btnDownvote.setTextColor(getResources().getColor(R.color.muted_foreground, null));
        }
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorited
                ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
    }

    private void dialPhone(String phone) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
    }

    private String uid() {
        return FirebaseHelper.getInstance().getCurrentUser() != null
                ? FirebaseHelper.getInstance().getCurrentUser().getUid() : null;
    }

    // ── Category helpers (unchanged) ──────────────────────────────────────────

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
