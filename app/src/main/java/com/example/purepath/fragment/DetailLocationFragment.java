package com.example.purepath.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.purepath.R;
import com.example.purepath.network.AirPollutionResponse;
import com.example.purepath.network.ApiClient;
import com.example.purepath.network.MeteoResponse;
import com.example.purepath.network.WeatherResponse;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailLocationFragment extends Fragment {

    private TextView tvCityName, tvAqiValue, tvAqiLabel, tvAqiDesc;
    private TextView tvPm25, tvPm10, tvCo, tvNo2, tvO3, tvSo2;
    private TextView tvFeelsLike, tvHumidity, tvWind, tvVisibility;
    private TextView tvUvLabel, tvUvValue, tvSaran;
    private ProgressBar progressUv;
    private SharedPreferences prefs;

    private double lat = -6.1751; // default Jakarta Pusat
    private double lon = 106.8650;
    private String cityName = "Jakarta Pusat";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_location, container, false);

        prefs = requireActivity().getSharedPreferences("PurePathPrefs", 0);

        // Ambil argument dari ExploreFragment
        if (getArguments() != null) {
            cityName = getArguments().getString("cityName", "Jakarta Pusat");
            lat = getArguments().getDouble("lat", -6.1751);
            lon = getArguments().getDouble("lon", 106.8650);
        }

        // Init views
        tvCityName = view.findViewById(R.id.tv_city_name);
        tvAqiValue = view.findViewById(R.id.tv_aqi_value);
        tvAqiLabel = view.findViewById(R.id.tv_aqi_label);
        tvAqiDesc = view.findViewById(R.id.tv_aqi_desc);
        tvPm25 = view.findViewById(R.id.tv_pm25);
        tvPm10 = view.findViewById(R.id.tv_pm10);
        tvNo2 = view.findViewById(R.id.tv_no2);
        tvO3 = view.findViewById(R.id.tv_o3);
        tvFeelsLike = view.findViewById(R.id.tv_feels_like);
        tvHumidity = view.findViewById(R.id.tv_humidity);
        tvWind = view.findViewById(R.id.tv_wind);
        tvUvLabel = view.findViewById(R.id.tv_uv_label);
        tvUvValue = view.findViewById(R.id.tv_uv_value);
        tvSaran = view.findViewById(R.id.tv_saran);
        progressUv = view.findViewById(R.id.progress_uv);

        tvCityName.setText(cityName);

        // Back button
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().onBackPressed());

        // Buka di Maps
        MaterialButton btnMaps = view.findViewById(R.id.btn_maps);
        btnMaps.setOnClickListener(v -> {
            Uri uri = Uri.parse("geo:" + lat + "," + lon + "?q=" + cityName);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Kalau Google Maps tidak ada, buka browser
                Uri webUri = Uri.parse("https://maps.google.com/?q=" + lat + "," + lon);
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });

        // Bagikan
        MaterialButton btnBagikan = view.findViewById(R.id.btn_bagikan);
        btnBagikan.setOnClickListener(v -> {
            String shareText = "Kualitas udara di " + cityName + " saat ini: " +
                    tvAqiValue.getText() + " AQI (" + tvAqiLabel.getText() + ")\n" +
                    "Cek via PurePath App!";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Bagikan via"));
        });

        fetchAllData();

        return view;
    }

    private void fetchAllData() {
        fetchAirQuality();
        fetchWeather();
        fetchUvIndex();
    }

    private void fetchAirQuality() {
        ApiClient.getWeatherService().getAirPollution(lat, lon, ApiClient.OWM_API_KEY)
                .enqueue(new Callback<AirPollutionResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<AirPollutionResponse> call,
                                           @NonNull Response<AirPollutionResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().list.isEmpty()) {
                            AirPollutionResponse.AqiData data = response.body().list.get(0);
                            requireActivity().runOnUiThread(() -> {
                                int aqi = data.main.aqi;
                                updateAqiUI(aqi);
                                tvPm25.setText(String.format("%.1f μg/m³", data.components.pm25));
                                tvPm10.setText(String.format("%.1f μg/m³", data.components.pm10));
                                tvNo2.setText(String.format("%.1f μg/m³", data.components.no2));
                                tvO3.setText(String.format("%.1f μg/m³", data.components.o3));
                                // CO dan SO2 perlu ditambah di AirPollutionResponse
                                tvCo.setText("N/A");
                                tvSo2.setText("N/A");
                                updateSaran(aqi);
                            });
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<AirPollutionResponse> call,
                                          @NonNull Throwable t) {}
                });
    }

    private void fetchWeather() {
        ApiClient.getWeatherService().getWeather(lat, lon, ApiClient.OWM_API_KEY, "metric", "id")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse data = response.body();
                            requireActivity().runOnUiThread(() -> {
                                tvFeelsLike.setText((int) data.main.temp + "°C");
                                tvHumidity.setText(data.main.humidity + "%");
                                tvWind.setText((int) data.wind.speed + " km/h");
                                tvVisibility.setText("N/A");
                            });
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call,
                                          @NonNull Throwable t) {}
                });
    }

    private void fetchUvIndex() {
        ApiClient.getMeteoService().getWeather(lat, lon, "uv_index", "Asia/Jakarta")
                .enqueue(new Callback<MeteoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MeteoResponse> call,
                                           @NonNull Response<MeteoResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            double uv = response.body().current.uvIndex;
                            requireActivity().runOnUiThread(() -> {
                                tvUvValue.setText(String.valueOf((int) uv));
                                String label = getUvLabel(uv);
                                tvUvLabel.setText(label);
                                progressUv.setProgress((int)(uv / 11.0 * 100));
                            });
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MeteoResponse> call,
                                          @NonNull Throwable t) {}
                });
    }

    private void updateAqiUI(int owmAqi) {
        int[] aqiValues = {25, 75, 125, 175, 250};
        String[] labels = {"BAIK", "SEDANG", "TIDAK SEHAT", "BURUK", "BERBAHAYA"};
        String[] descs = {
                "Kualitas udara sangat baik. Aman untuk semua aktivitas.",
                "Kualitas udara dapat diterima. Beberapa polutan mungkin menjadi perhatian.",
                "Anggota kelompok sensitif mungkin mengalami efek kesehatan.",
                "Semua orang mulai merasakan dampak kesehatan.",
                "Peringatan kesehatan darurat. Semua orang terpengaruh."
        };
        int idx = Math.min(owmAqi - 1, 4);
        tvAqiValue.setText(String.valueOf(aqiValues[idx]));
        tvAqiLabel.setText(labels[idx]);
        tvAqiDesc.setText(descs[idx]);
    }

    private void updateSaran(int owmAqi) {
        boolean hasAsma = prefs.getBoolean("health_asma", false);
        boolean hasIspa = prefs.getBoolean("health_ispa", false);

        String saran;
        if (owmAqi >= 3 && (hasAsma || hasIspa)) {
            saran = "⚠️ Kualitas udara tidak aman untuk kondisi pernapasan Anda. " +
                    "Gunakan masker N95 dan batasi aktivitas luar ruangan.";
        } else if (owmAqi >= 3) {
            saran = "Kualitas udara kurang optimal. Disarankan menggunakan masker " +
                    "saat berada di luar ruangan dalam waktu lama.";
        } else {
            saran = "✅ Kualitas udara baik. Aman untuk beraktivitas di luar ruangan.";
        }
        tvSaran.setText(saran);
    }

    private String getUvLabel(double uv) {
        if (uv <= 2) return "Rendah";
        else if (uv <= 5) return "Sedang";
        else if (uv <= 7) return "Tinggi";
        else if (uv <= 10) return "Sangat Tinggi";
        else return "Ekstrem";
    }
}