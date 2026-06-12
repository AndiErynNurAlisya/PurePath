package com.example.purepath.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.purepath.R;
import com.example.purepath.adapter.NewsAdapter;
import com.example.purepath.model.NewsArticle;
import com.example.purepath.network.ApiClient;
import com.example.purepath.network.NewsResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {

    private RecyclerView rvNews;
    private NewsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<NewsArticle> articleList = new ArrayList<>();
    private String currentQuery = "air pollution OR smog OR AQI OR \"air quality\" Indonesia";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        rvNews = view.findViewById(R.id.rv_news);
        swipeRefresh = view.findViewById(R.id.swipe_refresh_news);

        setupRecyclerView();
        setupChips(view);
        fetchNews(currentQuery);

        swipeRefresh.setOnRefreshListener(() -> fetchNews(currentQuery));

        return view;
    }

    private void setupRecyclerView() {
        adapter = new NewsAdapter(getContext(), articleList);
        rvNews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNews.setAdapter(adapter);
    }

    private void setupChips(View view) {
        com.google.android.material.chip.Chip chipAll =
                view.findViewById(R.id.chip_all);
        com.google.android.material.chip.Chip chipPollution =
                view.findViewById(R.id.chip_pollution);
        com.google.android.material.chip.Chip chipAir =
                view.findViewById(R.id.chip_air);
        com.google.android.material.chip.Chip chipClimate =
                view.findViewById(R.id.chip_climate);

        chipAll.setOnClickListener(v -> {
            currentQuery = "polusi udara OR kualitas udara OR lingkungan hidup OR KLHK";
            fetchNews(currentQuery);
        });

        chipPollution.setOnClickListener(v -> {
            currentQuery = "polusi udara Jakarta smog PM2.5 asap";
            fetchNews(currentQuery);
        });

        chipAir.setOnClickListener(v -> {
            currentQuery = "ISPU kualitas udara AQI Indonesia";
            fetchNews(currentQuery);
        });

        chipClimate.setOnClickListener(v -> {
            currentQuery = "perubahan iklim emisi karbon deforestasi Indonesia";
            fetchNews(currentQuery);
        });
    }

    private void fetchNews(String query) {
        swipeRefresh.setRefreshing(true);

        ApiClient.getNewsService().getEnvironmentNews(
                query, "en", "publishedAt", 20, ApiClient.NEWS_API_KEY
        ).enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewsResponse> call,
                                   @NonNull Response<NewsResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null
                        && isAdded()) {
                    List<NewsArticle> articles = new ArrayList<>();
                    for (NewsResponse.Article article : response.body().articles) {
                        if (article.title != null && !article.title.equals("[Removed]")) {
                            articles.add(new NewsArticle(
                                    article.title,
                                    article.description,
                                    article.url,
                                    article.urlToImage,
                                    article.publishedAt,
                                    article.source != null ? article.source.name : "Unknown"
                            ));
                        }
                    }
                    adapter.updateArticles(articles);
                } else {
                    if (isAdded()) {
                        Toast.makeText(getContext(),
                                "Gagal memuat berita", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewsResponse> call,
                                  @NonNull Throwable t) {
                swipeRefresh.setRefreshing(false);
                if (isAdded()) {
                    Toast.makeText(getContext(),
                            "Tidak ada koneksi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}