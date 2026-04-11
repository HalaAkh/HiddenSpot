package com.hiddenspot.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hiddenspot.app.R;
import com.hiddenspot.app.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private List<Review> reviews;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    public void updateReviews(List<Review> newReviews) {
        this.reviews = newReviews;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review r = reviews.get(position);
        String name = r.getUserName() != null ? r.getUserName() : "Anonymous";
        holder.tvInitial.setText(name.substring(0, 1).toUpperCase());
        holder.tvName.setText(name);
        holder.tvDate.setText(r.getFormattedDate());
        holder.ratingBar.setRating(r.getRating());
        holder.tvComment.setText(r.getComment() != null ? r.getComment() : "");
        holder.tvComment.setVisibility(
                (r.getComment() != null && !r.getComment().trim().isEmpty())
                        ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        final TextView tvInitial, tvName, tvDate, tvComment;
        final RatingBar ratingBar;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_reviewer_initial);
            tvName = itemView.findViewById(R.id.tv_reviewer_name);
            tvDate = itemView.findViewById(R.id.tv_review_date);
            ratingBar = itemView.findViewById(R.id.rating_bar_review);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
        }
    }
}
