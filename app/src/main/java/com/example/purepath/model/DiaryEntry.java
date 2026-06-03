package com.example.purepath.model;

public class DiaryEntry {
    private String date;
    private String description;
    private int aqi;
    private String aqiLabel;

    public DiaryEntry(String date, String description, int aqi, String aqiLabel) {
        this.date = date;
        this.description = description;
        this.aqi = aqi;
        this.aqiLabel = aqiLabel;
    }

    public String getDate() { return date; }
    public String getDescription() { return description; }
    public int getAqi() { return aqi; }
    public String getAqiLabel() { return aqiLabel; }
}