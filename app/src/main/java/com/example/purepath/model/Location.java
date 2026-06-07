package com.example.purepath.model;

public class Location {
    private String cityName;
    private String province;
    private int aqi;
    private String aqiStatus;
    private boolean bookmarked;
    private double lat;
    private double lon;

    public Location(String cityName, String province, int aqi, String aqiStatus,
                    boolean bookmarked, double lat, double lon) {
        this.cityName = cityName;
        this.province = province;
        this.aqi = aqi;
        this.aqiStatus = aqiStatus;
        this.bookmarked = bookmarked;
        this.lat = lat;
        this.lon = lon;
    }

    public String getCityName() { return cityName; }
    public String getProvince() { return province; }
    public int getAqi() { return aqi; }
    public String getAqiStatus() { return aqiStatus; }
    public boolean isBookmarked() { return bookmarked; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
}
