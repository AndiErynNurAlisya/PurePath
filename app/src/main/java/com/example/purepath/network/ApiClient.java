package com.example.purepath.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String OWM_BASE_URL = "https://api.openweathermap.org/";
    private static final String METEO_BASE_URL = "https://api.open-meteo.com/";
    public static final String OWM_API_KEY = "7d943598523fc5a7238e32e9a1b17085";

    private static Retrofit retrofitOWM = null;
    private static Retrofit retrofitMeteo = null;

    public static OpenWeatherService getWeatherService() {
        if (retrofitOWM == null) {
            retrofitOWM = new Retrofit.Builder()
                    .baseUrl(OWM_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitOWM.create(OpenWeatherService.class);
    }

    public static MeteoApiService getMeteoService() {
        if (retrofitMeteo == null) {
            retrofitMeteo = new Retrofit.Builder()
                    .baseUrl(METEO_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitMeteo.create(MeteoApiService.class);
    }
}