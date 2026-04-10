package com.hiddenspot.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.hiddenspot.app.R;
import com.hiddenspot.app.activities.PlaceDetailsActivity;
import com.hiddenspot.app.models.Place;

import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private final Context context;
    private List<Place> places;
    private OnFavoriteClickListener favoriteListener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Place place, int position);
    }

    public PlaceAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    public void updatePlaces(List<Place> newPlaces) {
        this.places = newPlaces;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_place_card, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        holder.bind(places.get(position), position);
    }

    @Override
    public int getItemCount() { return places != null ? places.size() : 0; }

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final ImageView ivPlace;
        final TextView tvCategoryBadge, tvPlaceName, tvCity, tvDescription;
        final TextView tvRating, tvRatingCount, tvUpvotes, tvDownvotes;
        final ImageButton btnFavorite;

        PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView        = (CardView) itemView;
            ivPlace         = itemView.findViewById(R.id.iv_place);
            tvCategoryBadge = itemView.findViewById(R.id.tv_category_badge);
            btnFavorite     = itemView.findViewById(R.id.btn_favorite);
            tvPlaceName     = itemView.findViewById(R.id.tv_place_name);
            tvCity          = itemView.findViewById(R.id.tv_city);
            tvDescription   = itemView.findViewById(R.id.tv_description);
            tvRating        = itemView.findViewById(R.id.tv_rating);
            tvRatingCount   = itemView.findViewById(R.id.tv_rating_count);
            tvUpvotes       = itemView.findViewById(R.id.tv_upvotes);
            tvDownvotes     = itemView.findViewById(R.id.tv_downvotes);
        }

        void bind(Place place, int position) {
            Glide.with(context).load(place.getFirstImage())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.color.muted).centerCrop().into(ivPlace);

            tvCategoryBadge.setText(place.getCategoryDisplay());
            tvCategoryBadge.setBackgroundColor(getCategoryColor(place.getCategory()));
            btnFavorite.setImageResource(place.isFavorited()
                    ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
            tvPlaceName.setText(place.getName());
            tvCity.setText(place.getCity());
            tvDescription.setText(place.getDescription());
            tvRating.setText(String.valueOf(place.getRating()));
            tvRatingCount.setText("(" + place.getRatingCount() + ")");
            tvUpvotes.setText(String.valueOf(place.getUpvotes()));
            tvDownvotes.setText(String.valueOf(place.getDownvotes()));

            cardView.setOnClickListener(v -> {
                Intent i = new Intent(context, PlaceDetailsActivity.class);
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_ID,        place.getId());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_NAME,      place.getName());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_IMAGE,     place.getFirstImage());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_CITY,      place.getCity());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_ADDRESS,   place.getAddress());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_PHONE,     place.getPhone());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_DESC,      place.getDescription());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_CATEGORY,  place.getCategory());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_RATING,    place.getRating());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_UPVOTES,   place.getUpvotes());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_DOWNVOTES, place.getDownvotes());
                i.putExtra(PlaceDetailsActivity.EXTRA_PLACE_FAVORITED, place.isFavorited());
                i.putExtra(PlaceDetailsActivity.EXTRA_POSTER_NAME,     place.getUserName());
                i.putExtra(PlaceDetailsActivity.EXTRA_POSTED_DATE,     place.getFormattedDate());
                context.startActivity(i);
            });

            btnFavorite.setOnClickListener(v -> {
                if (favoriteListener != null) favoriteListener.onFavoriteClick(place, position);
            });
        }

        private int getCategoryColor(String cat) {
            if (cat == null) return context.getResources().getColor(R.color.primary, null);
            switch (cat) {
                case "Restaurant": return context.getResources().getColor(R.color.badge_restaurant, null);
                case "Garden":     return context.getResources().getColor(R.color.badge_garden, null);
                case "Café":       return context.getResources().getColor(R.color.badge_cafe, null);
                case "Viewpoint":  return context.getResources().getColor(R.color.badge_viewpoint, null);
                case "Park":       return context.getResources().getColor(R.color.badge_park, null);
                case "Beach":      return context.getResources().getColor(R.color.badge_beach, null);
                case "Library":    return context.getResources().getColor(R.color.badge_library, null);
                case "Shop":       return context.getResources().getColor(R.color.badge_shop, null);
                case "Historical": return context.getResources().getColor(R.color.badge_historical, null);
                default:           return context.getResources().getColor(R.color.primary, null);
            }
        }
    }
}
