package com.example.purepath.fragment;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.purepath.R;
import com.example.purepath.activity.MainActivity;
import com.example.purepath.network.AirPollutionResponse;
import com.example.purepath.network.ApiClient;
import com.example.purepath.network.MeteoResponse;
import com.example.purepath.network.WeatherResponse;
import com.example.purepath.database.DiaryDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.ArrayList;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView tvGreeting, tvLocation, tvAqiValue, tvAqiLabel, tvAqiDesc;
    private TextView tvTemp, tvWeatherDesc, tvHumidity, tvWind, tvUv;
    private TextView tvBreathIndex, tvBreathDesc, tvRekoTitle, tvRekoDesc;
    private ProgressBar progressBreath;

    private BottomNavigationView bottomNav;

    // Koordinat default Makassar
    private double lat = -5.1477;
    private double lon = 119.4327;

    private int currentAqi = 0;
    private double currentUv = 0;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

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

        view.findViewById(R.id.btn_lihat_profil).setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToSettings();
        });

        loadUserGreeting();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocationAndFetch();

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

    private void requestLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
            return;
        }
        getLocationAndFetch();
    }

    private void getLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Pakai koordinat default Makassar
            fetchAllData();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lat = location.getLatitude();
                lon = location.getLongitude();
            }
            // Fetch data dengan koordinat GPS atau default
            fetchAllData();
        }).addOnFailureListener(e -> {
            // Pakai koordinat default
            fetchAllData();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetch();
            } else {
                // Permission ditolak, pakai koordinat default
                fetchAllData();
            }
        }
    }

    private void fetchAllData() {
        fetchWeatherData();
        fetchAirQuality();
        fetchUvIndex();
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
                    if (response.isSuccessful() && response.body() != null && isAdded()) {
                        WeatherResponse data = response.body();
                        getActivity().runOnUiThread(() -> {
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
                    if (isAdded()) {
                        getActivity().runOnUiThread(() ->
                                tvWeatherDesc.setText("Gagal memuat data"));
                    }
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
                        && !response.body().list.isEmpty() && isAdded()) {

                    AirPollutionResponse.AqiData data = response.body().list.get(0);
                    currentAqi = data.main.aqi;
                    int aqiDisplay = convertOwmAqi(currentAqi);
                    double pm25 = data.components.pm25;

                    getActivity().runOnUiThread(() -> {
                        tvAqiValue.setText(String.valueOf(aqiDisplay));
                        updateAqiUI(currentAqi, pm25);
                        updateBreathIndex(currentAqi);
                        updateRekomendasi(currentAqi, currentUv);

                        // Simpan ke database setelah data AQI didapat
                        saveToDiary();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<AirPollutionResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> tvAqiLabel.setText("Gagal memuat"));
                }
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
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    currentUv = response.body().current.uvIndex;
                    getActivity().runOnUiThread(() -> {
                        String uvLabel = getUvLabel(currentUv);
                        tvUv.setText((int) currentUv + " (" + uvLabel + ")");
                        updateRekomendasi(currentAqi, currentUv);

                        // Update database dengan nilai UV terbaru
                        saveToDiary();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeteoResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> tvUv.setText("N/A"));
                }
            }
        });
    }

    // Fungsi helper untuk menyimpan data ke SQLite
    private void saveToDiary() {
        if (!isAdded()) return;

        // Ganti format ke yyyy-MM-dd untuk sorting yang benar
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault()).format(new java.util.Date());

        String aqiLabel = getAqiLabel(currentAqi);
        String desc = getAqiDesc(currentAqi);

        DiaryDao dao = new DiaryDao(requireContext());
        dao.insertOrUpdate(today, convertOwmAqi(currentAqi), aqiLabel, desc, 0, currentUv);
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

    private String getAqiLabel(int owmAqi) {
        switch (owmAqi) {
            case 1: return "BAIK";
            case 2: return "SEDANG";
            case 3: return "TIDAK SEHAT";
            case 4: return "BURUK";
            default: return "BERBAHAYA";
        }
    }

    private String getAqiDesc(int owmAqi) {
        switch (owmAqi) {
            case 1: return "Udara Segar Sepanjang Hari";
            case 2: return "Kualitas Udara Cukup Baik";
            case 3: return "Sensitif Terhadap Polusi";
            case 4: return "Polusi Tinggi Hari Ini";
            default: return "Kondisi Udara Berbahaya";
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
        if (uv < 3.0) {
            return "Rendah";
        } else if (uv < 6.0) {
            return "Sedang";
        } else if (uv < 8.0) {
            return "Tinggi";
        } else if (uv < 11.0) {
            return "Sangat Tinggi";
        } else {
            return "Ekstrem";
        }
    }

    private void updateRekomendasi(int aqi, double uv) {
        if (tvRekoTitle == null) return;

        boolean hasAsma = prefs.getBoolean("health_asma", false);
        boolean hasIspa = prefs.getBoolean("health_ispa", false);
        boolean hasLupus = prefs.getBoolean("health_lupus", false);
        boolean hasEksim = prefs.getBoolean("health_eksim", false);
        boolean hasRosacea = prefs.getBoolean("health_rosacea", false);
        boolean hasHerpes = prefs.getBoolean("health_herpes", false);
        boolean anyCondition = hasAsma || hasIspa || hasLupus ||
                hasEksim || hasRosacea || hasHerpes;

        if (!anyCondition) {
            if (aqi <= 1) {
                tvRekoTitle.setText("Udara Bersih Hari Ini");
                tvRekoDesc.setText("Kualitas udara sangat baik. Aman untuk semua aktivitas.");
            } else if (aqi <= 2) {
                tvRekoTitle.setText("Udara Cukup Baik");
                tvRekoDesc.setText("Aktivitas luar ruangan tetap aman hari ini.");
            } else {
                tvRekoTitle.setText("Waspada Kualitas Udara");
                tvRekoDesc.setText("Pertimbangkan mengurangi aktivitas luar ruangan.");
            }
            return;
        }

        // Kumpulkan SEMUA peringatan sekaligus (fix komorbid)
        List<String> warnings = new ArrayList<>();

        // Cek AQI untuk penyakit pernapasan
        if ((hasAsma || hasIspa) && aqi >= 3) {
            warnings.add("⚠️ AQI tinggi berbahaya untuk pernapasan Anda. Gunakan masker N95.");
        } else if ((hasAsma || hasIspa) && aqi >= 2) {
            warnings.add("⚡ Kualitas udara sedang. Batasi aktivitas luar ruangan yang berat.");
        }

        // Cek UV untuk penyakit kulit/autoimun
        if ((hasLupus || hasRosacea || hasHerpes) && uv >= 6) {
            warnings.add("⚠️ UV " + (int)uv + " berbahaya. Gunakan sunscreen SPF 50+ dan pakaian pelindung.");
        } else if ((hasLupus || hasRosacea || hasHerpes) && uv >= 3) {
            warnings.add("☀️ UV sedang. Disarankan memakai sunscreen SPF 30+.");
        }

        // Cek keduanya untuk Eksim
        if (hasEksim) {
            if (aqi >= 3 && uv >= 6) {
                warnings.add("⚠️ Polusi + UV tinggi berbahaya untuk eksim. Jaga kelembaban kulit.");
            } else if (aqi >= 3) {
                warnings.add("⚡ Polusi tinggi dapat memperburuk eksim Anda.");
            } else if (uv >= 6) {
                warnings.add("☀️ UV tinggi dapat memperburuk eksim. Hindari paparan langsung.");
            }
        }

        if (warnings.isEmpty()) {
            tvRekoTitle.setText("✅ Kondisi Relatif Aman");
            tvRekoDesc.setText("Kualitas udara dan UV index dalam batas aman untuk kondisi Anda.");
        } else {
            tvRekoTitle.setText(warnings.size() > 1 ?
                    "⚠️ " + warnings.size() + " Peringatan Hari Ini" : "Peringatan Kesehatan");
            tvRekoDesc.setText(String.join("\n\n", warnings));
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
