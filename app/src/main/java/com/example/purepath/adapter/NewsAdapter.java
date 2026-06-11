package com.example.purepath.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.purepath.R;
import com.example.purepath.activity.WebViewActivity;
import com.example.purepath.model.NewsArticle;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsArticle> articles;
    private Context context;

    public NewsAdapter(Context context, List<NewsArticle> articles) {
        this.context = context;
        this.articles = articles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsArticle article = articles.get(position);

        holder.tvTitle.setText(article.getTitle());
        holder.tvDescription.setText(
                article.getDescription() != null ? article.getDescription() : "");
        holder.tvSource.setText(article.getSourceName());

        // Format tanggal
        try {
            java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",
                            java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat("dd MMM yyyy",
                            new java.util.Locale("id"));
            java.util.Date date = inputFormat.parse(article.getPublishedAt());
            holder.tvDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvDate.setText(article.getPublishedAt());
        }

        // Load thumbnail dengan Glide
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Glide.with(context)
                    .load(article.getUrlToImage())
                    .placeholder(R.drawable.ic_explore)
                    .error(R.drawable.ic_explore)
                    .into(holder.ivThumbnail);
        }

        // Tap → buka browser
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra("url", article.getUrl());
            intent.putExtra("title", article.getTitle());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void updateArticles(List<NewsArticle> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvDescription, tvSource, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
    }
}