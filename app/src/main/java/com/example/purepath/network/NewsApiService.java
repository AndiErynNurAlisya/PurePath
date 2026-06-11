package com.example.purepath.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NewsApiService {

    @GET("v2/everything")
    Call<NewsResponse> getEnvironmentNews(
            @Query("q") String query,
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("pageSize") int pageSize,
            @Query("apiKey") String apiKey
    );
}