package com.example.purepath.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AirPollutionResponse {

    @SerializedName("list")
    public List<AqiData> list;

    public static class AqiData {
        @SerializedName("main")
        public Main main;

        @SerializedName("components")
        public Components components;

        public static class Main {
            @SerializedName("aqi")
            public int aqi; // 1=Baik, 2=Sedang, 3=Tidak Sehat, 4=Buruk, 5=Sangat Buruk
        }

        public static class Components {
            @SerializedName("pm2_5")
            public double pm25;

            @SerializedName("pm10")
            public double pm10;

            @SerializedName("no2")
            public double no2;

            @SerializedName("o3")
            public double o3;
        }
    }
}