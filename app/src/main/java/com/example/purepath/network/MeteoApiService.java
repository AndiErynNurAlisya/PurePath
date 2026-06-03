package com.example.purepath.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 1. Ubah class menjadi interface
public interface MeteoApiService {

    @GET("v1/forecast")
    Call<MeteoResponse> getWeather(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            // 2. Sesuaikan tipe data query jika menggunakan Open-Meteo
            @Query("current") String current,
            @Query("timezone") String timezone
    );
}