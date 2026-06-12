package com.example.purepath.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.purepath.BuildConfig;

public class ApiClient {

    private static final String OWM_BASE_URL = "https://api.openweathermap.org/";
    private static final String METEO_BASE_URL = "https://api.open-meteo.com/";
    public static final String OWM_API_KEY = BuildConfig.OWM_API_KEY;

    private static Retrofit retrofitOWM = null;
    private static Retrofit retrofitMeteo = null;

    private static final String NEWS_BASE_URL = "https://newsapi.org/";
    public static final String NEWS_API_KEY = BuildConfig.NEWS_API_KEY;

    private static Retrofit retrofitNews = null;

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

    public static NewsApiService getNewsService() {
        if (retrofitNews == null) {
            retrofitNews = new Retrofit.Builder()
                    .baseUrl(NEWS_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitNews.create(NewsApiService.class);
    }
}