package com.example.purepath.network;

import com.google.gson.annotations.SerializedName;

public class GeocodingResponse {

    @SerializedName("name")
    public String name;

    @SerializedName("local_names")
    public LocalNames localNames;

    @SerializedName("lat")
    public double lat;

    @SerializedName("lon")
    public double lon;

    @SerializedName("country")
    public String country;

    @SerializedName("state")
    public String state;

    public static class LocalNames {
        @SerializedName("id")
        public String id;
    }
}