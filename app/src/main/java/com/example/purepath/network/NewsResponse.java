package com.example.purepath.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("totalResults")
    public int totalResults;

    @SerializedName("articles")
    public List<Article> articles;

    public static class Article {
        @SerializedName("source")
        public Source source;

        @SerializedName("title")
        public String title;

        @SerializedName("description")
        public String description;

        @SerializedName("url")
        public String url;

        @SerializedName("urlToImage")
        public String urlToImage;

        @SerializedName("publishedAt")
        public String publishedAt;

        public static class Source {
            @SerializedName("name")
            public String name;
        }
    }
}