package com.example.purepath.network;
import com.google.gson.annotations.SerializedName;
public class MeteoResponse {

    @SerializedName("current")
    public Current current;

    public static class Current {
        @SerializedName("uv_index")
        public double uvIndex;
    }
}
