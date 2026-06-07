package com.example.purepath.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherService {

    @GET("data/2.5/weather")
    Call<WeatherResponse> getWeather(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units,
            @Query("lang") String lang
    );

    @GET("data/2.5/air_pollution")
    Call<AirPollutionResponse> getAirPollution(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey
    );

    @GET("geo/1.0/direct")
    Call<List<GeocodingResponse>> searchCity(
            @Query("q") String cityName,
            @Query("limit") int limit,
            @Query("appid") String apiKey
    );
}