package com.example.purepath.model;

public class NewsArticle {
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String sourceName;

    public NewsArticle(String title, String description, String url,
                       String urlToImage, String publishedAt, String sourceName) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
        this.sourceName = sourceName;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getUrl() { return url; }
    public String getUrlToImage() { return urlToImage; }
    public String getPublishedAt() { return publishedAt; }
    public String getSourceName() { return sourceName; }
}