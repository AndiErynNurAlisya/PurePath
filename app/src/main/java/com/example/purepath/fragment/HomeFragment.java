package com.example.purepath.fragment;

import android.content.SharedPreferences;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView tvGreeting, tvLocation, tvAqiValue, tvAqiLabel, tvAqiDesc;
    private TextView tvTemp, tvWeatherDesc, tvHumidity, tvWind, tvUv;
    private TextView tvBreathIndex, tvBreathDesc, tvRekoTitle, tvRekoDesc;
    private ProgressBar progressBreath;

    // Koordinat default Makassar
    private double lat = -5.1477;
    private double lon = 119.4327;

    private int currentAqi = 0;
    private double currentUv = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("PurePathPrefs", 0);

        // Init views
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvLocation = view.findViewById(R.id.tv_location);
        tvAqiValue = view.findViewById(R.id.tv_aqi_value);
        tvAqiLabel = view.findViewById(R.id.tv_aqi_label);
        tvAqiDesc = view.findViewById(R.id.tv_aqi_desc);
        tvTemp = view.findViewById(R.id.tv_temp);
        tvWeatherDesc = view.findViewById(R.id.tv_weather_desc);
        tvHumidity = view.findViewById(R.id.tv_humidity);
        tvWind = view.findViewById(R.id.tv_wind);
        tvUv = view.findViewById(R.id.tv_uv);
        tvBreathIndex = view.findViewById(R.id.tv_breath_index);
        tvBreathDesc = view.findViewById(R.id.tv_breath_desc);
        tvRekoTitle = view.findViewById(R.id.tv_reko_title);
        tvRekoDesc = view.findViewById(R.id.tv_reko_desc);
        progressBreath = view.findViewById(R.id.progress_breath);

        loadUserGreeting();
        fetchWeatherData();
        fetchAirQuality();
        fetchUvIndex();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRekomendasi(currentAqi, currentUv);
    }

    private void loadUserGreeting() {
        String name = prefs.getString("user_name", "");
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 11) greeting = "Selamat pagi";
        else if (hour < 15) greeting = "Selamat siang";
        else if (hour < 18) greeting = "Selamat sore";
        else greeting = "Selamat malam";
        tvGreeting.setText(name.isEmpty() ? greeting + "!" : greeting + ", " + name + "!");
    }

    private void fetchWeatherData() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ApiClient.getWeatherService().getWeather(
                    lat, lon, ApiClient.OWM_API_KEY, "metric", "id"
            ).enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(@NonNull Call<WeatherResponse> call,
                                       @NonNull Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse data = response.body();
                        requireActivity().runOnUiThread(() -> {
                            tvTemp.setText((int) data.main.temp + "°C");
                            tvHumidity.setText(data.main.humidity + "%");
                            tvWind.setText((int) data.wind.speed + " km/h");
                            tvLocation.setText("📍 " + data.cityName + ", Indonesia");
                            if (!data.weather.isEmpty()) {
                                String desc = data.weather.get(0).description;
                                tvWeatherDesc.setText(capitalize(desc));
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                    requireActivity().runOnUiThread(() ->
                            tvWeatherDesc.setText("Gagal memuat data"));
                }
            });
        });
    }

    private void fetchAirQuality() {
        ApiClient.getWeatherService().getAirPollution(
                lat, lon, ApiClient.OWM_API_KEY
        ).enqueue(new Callback<AirPollutionResponse>() {
            @Override
            public void onResponse(@NonNull Call<AirPollutionResponse> call,
                                   @NonNull Response<AirPollutionResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().list.isEmpty()) {
                    AirPollutionResponse.AqiData data = response.body().list.get(0);
                    currentAqi = data.main.aqi;

                    // Convert OWM AQI (1-5) ke skala 0-300
                    int aqiDisplay = convertOwmAqi(currentAqi);
                    double pm25 = data.components.pm25;

                    requireActivity().runOnUiThread(() -> {
                        tvAqiValue.setText(String.valueOf(aqiDisplay));
                        updateAqiUI(currentAqi, pm25);
                        updateBreathIndex(currentAqi);
                        updateRekomendasi(currentAqi, currentUv);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<AirPollutionResponse> call,
                                  @NonNull Throwable t) {
                requireActivity().runOnUiThread(() ->
                        tvAqiLabel.setText("Gagal memuat"));
            }
        });
    }

    private void fetchUvIndex() {
        ApiClient.getMeteoService().getWeather(
                lat, lon, "uv_index", "Asia/Makassar"
        ).enqueue(new Callback<MeteoResponse>() {
            @Override
            public void onResponse(@NonNull Call<MeteoResponse> call,
                                   @NonNull Response<MeteoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUv = response.body().current.uvIndex;
                    requireActivity().runOnUiThread(() -> {
                        String uvLabel = getUvLabel(currentUv);
                        tvUv.setText((int) currentUv + " (" + uvLabel + ")");
                        updateRekomendasi(currentAqi, currentUv);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeteoResponse> call,
                                  @NonNull Throwable t) {
                requireActivity().runOnUiThread(() -> tvUv.setText("N/A"));
            }
        });
    }

    private int convertOwmAqi(int owmAqi) {
        switch (owmAqi) {
            case 1: return 25;
            case 2: return 75;
            case 3: return 125;
            case 4: return 175;
            case 5: return 250;
            default: return 0;
        }
    }

    private void updateAqiUI(int owmAqi, double pm25) {
        String label, desc;
        int color;
        switch (owmAqi) {
            case 1:
                label = "Baik"; desc = "Udara sangat bersih. Sempurna untuk aktivitas luar ruangan.";
                color = getResources().getColor(R.color.aqi_good, null); break;
            case 2:
                label = "Sedang"; desc = "Kualitas udara dapat diterima. Aktivitas luar ruangan masih aman.";
                color = getResources().getColor(R.color.aqi_moderate, null); break;
            case 3:
                label = "Tidak Sehat (Sensitif)"; desc = "Kelompok sensitif perlu berhati-hati di luar ruangan.";
                color = getResources().getColor(R.color.aqi_sensitive, null); break;
            case 4:
                label = "Tidak Sehat"; desc = "Semua orang mulai merasakan dampak. Kurangi aktivitas luar.";
                color = getResources().getColor(R.color.aqi_unhealthy, null); break;
            default:
                label = "Berbahaya"; desc = "Kondisi darurat kesehatan. Hindari semua aktivitas luar ruangan.";
                color = getResources().getColor(R.color.aqi_hazardous, null);
        }
        tvAqiLabel.setText(label);
        tvAqiLabel.setTextColor(color);
        tvAqiValue.setTextColor(color);
        tvAqiDesc.setText(desc);
    }

    private void updateBreathIndex(int owmAqi) {
        int breathScore = Math.max(0, 100 - (owmAqi - 1) * 20);
        tvBreathIndex.setText(String.valueOf(breathScore));
        progressBreath.setProgress(breathScore);
        if (breathScore >= 80) tvBreathDesc.setText("Paru-paru Anda menyukai udara ini.");
        else if (breathScore >= 60) tvBreathDesc.setText("Kualitas udara cukup baik untuk beraktivitas.");
        else tvBreathDesc.setText("Pertimbangkan menggunakan masker hari ini.");
    }

    private String getUvLabel(double uv) {
        if (uv <= 2) return "Rendah";
        else if (uv <= 5) return "Sedang";
        else if (uv <= 7) return "Tinggi";
        else if (uv <= 10) return "Sangat Tinggi";
        else return "Ekstrem";
    }

    private void updateRekomendasi(int aqi, double uv) {
        if (tvRekoTitle == null) return;
        boolean hasAsma = prefs.getBoolean("health_asma", false);
        boolean hasIspa = prefs.getBoolean("health_ispa", false);
        boolean hasLupus = prefs.getBoolean("health_lupus", false);
        boolean hasEksim = prefs.getBoolean("health_eksim", false);
        boolean hasRosacea = prefs.getBoolean("health_rosacea", false);
        boolean hasHerpes = prefs.getBoolean("health_herpes", false);
        boolean anyCondition = hasAsma || hasIspa || hasLupus || hasEksim || hasRosacea || hasHerpes;

        if (!anyCondition) {
            if (aqi <= 1) { tvRekoTitle.setText("Udara Bersih Hari Ini"); tvRekoDesc.setText("Kualitas udara sangat baik. Aman untuk semua aktivitas."); }
            else if (aqi <= 2) { tvRekoTitle.setText("Udara Cukup Baik"); tvRekoDesc.setText("Aktivitas luar ruangan tetap aman hari ini."); }
            else { tvRekoTitle.setText("Waspada Kualitas Udara"); tvRekoDesc.setText("Pertimbangkan mengurangi aktivitas luar ruangan."); }
            return;
        }

        if ((hasAsma || hasIspa) && aqi >= 3) {
            tvRekoTitle.setText("⚠️ Risiko Tinggi Pernapasan");
            tvRekoDesc.setText("AQI tinggi berbahaya untuk kondisi Anda. Gunakan masker N95.");
        } else if ((hasLupus || hasRosacea || hasHerpes) && uv >= 6) {
            tvRekoTitle.setText("⚠️ UV Index Tinggi");
            tvRekoDesc.setText("Gunakan tabir surya SPF 50+ dan hindari paparan matahari langsung.");
        } else if (hasEksim && (aqi >= 3 || uv >= 6)) {
            tvRekoTitle.setText("⚠️ Waspada untuk Eksim");
            tvRekoDesc.setText("Jaga kelembaban kulit dan gunakan pelindung hari ini.");
        } else {
            tvRekoTitle.setText("✅ Kondisi Relatif Aman");
            tvRekoDesc.setText("Kualitas udara dan UV index dalam batas aman untuk kondisi Anda.");
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}