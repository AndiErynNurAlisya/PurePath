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
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.ArrayList;


public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView tvGreeting, tvLocation, tvAqiValue, tvAqiLabel, tvAqiDesc;
    private TextView tvTemp, tvWeatherDesc, tvHumidity, tvWind, tvUv;
    private TextView tvBreathIndex, tvBreathDesc, tvRekoTitle, tvRekoDesc;
    private ProgressBar progressBreath;
    private double lat = 0;
    private double lon = 0;
    private boolean isRetrySnackbarShown = false;
    private CancellationTokenSource cancellationSource = new CancellationTokenSource();

    private int currentAqi = 0;

    private int currentIspu = 0;
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
            showLocationError("Izin lokasi diperlukan untuk menampilkan data area Anda.");
            return;
        }

        cancellationSource = new CancellationTokenSource();

        isRetrySnackbarShown = false;  // reset agar Snackbar retry bisa muncul lagi

        // Tampilkan status loading
        tvLocation.setText("📍 Mencari lokasi Anda...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        useLocation(location);
                    } else {
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> requestFreshLocation());
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showLocationError("Izin lokasi diperlukan untuk menampilkan data area Anda.");
            return;
        }

        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationSource.getToken()
        ).addOnSuccessListener(location -> {
            if (location != null) {
                useLocation(location);
            } else {
                showLocationError("Lokasi tidak ditemukan. Pastikan GPS aktif, lalu coba lagi.");
            }
        }).addOnFailureListener(e ->
                showLocationError("Gagal mengambil lokasi. Periksa koneksi & GPS Anda."));
    }

    private void useLocation(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
        fetchAllData();
    }


    private void showLocationError(String message) {
        if (!isAdded()) return;

        tvLocation.setText("📍 Lokasi tidak tersedia");
        tvAqiLabel.setText("—");
        tvAqiValue.setText("--");
        tvAqiDesc.setText(message);
        tvWeatherDesc.setText("Data tidak tersedia");

        com.google.android.material.snackbar.Snackbar.make(
                requireView(), message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setAction("Coba Lagi", v -> requestLocationAndFetch()).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetch();
            } else {
                showLocationError("Izin lokasi ditolak. Aktifkan izin lokasi untuk melihat data area Anda.");
            }
        }
    }

    private void fetchAllData() {
        fetchWeatherData();
        fetchAirQuality();
        fetchUvIndex();
    }

    // Snackbar "Coba Lagi" tunggal saat data API gagal dimuat
    private void showRetrySnackbar(String message) {
        if (!isAdded() || isRetrySnackbarShown) return;
        isRetrySnackbarShown = true;
        com.google.android.material.snackbar.Snackbar.make(
                requireView(), message,
                com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("Coba Lagi", v -> {
            isRetrySnackbarShown = false;
            requestLocationAndFetch();
        }).show();
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
                        getActivity().runOnUiThread(() -> {
                            tvWeatherDesc.setText("Gagal memuat data");
                            showRetrySnackbar("Tidak ada koneksi. Periksa internet Anda.");
                        });
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
                    double pm25 = data.components.pm25;
                    currentIspu = computeIspuFromPm25(pm25);   // ISPU asli dari PM2.5
                    currentAqi = ispuToLevel(currentIspu);     // level 1-5 dari ISPU
                    getActivity().runOnUiThread(() -> {
                        tvAqiValue.setText(String.valueOf(currentIspu));  // tampilkan ISPU asli
                        updateAqiUI(currentAqi, pm25);
                        updateBreathIndex(currentAqi);
                        updateRekomendasi(currentAqi, currentUv);
                        saveToDiary();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<AirPollutionResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        tvAqiLabel.setText("Gagal memuat");
                        showRetrySnackbar("Tidak ada koneksi. Periksa internet Anda.");
                    });
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
                        saveToDiary();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeteoResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        tvUv.setText("N/A");
                        showRetrySnackbar("Tidak ada koneksi. Periksa internet Anda.");
                    });
                }
            }
        });
    }

    // Fungsi helper untuk menyimpan data ke SQLite
    private void saveToDiary() {
        if (!isAdded()) return;
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault()).format(new java.util.Date());

        String aqiLabel = getAqiLabel(currentAqi);
        String desc = getAqiDesc(currentAqi);

        DiaryDao dao = new DiaryDao(requireContext());
        dao.insertOrUpdate(today, currentIspu, aqiLabel, desc, 0, currentUv);
    }


    /**
     * Hitung ISPU dari konsentrasi PM2.5 (µg/m³) sesuai formula resmi KLHK:
     * I = ((Ia - Ib) / (Xa - Xb)) * (Xx - Xb) + Ib
     */
    private int computeIspuFromPm25(double c) {
        double xb, xa;
        int ib, ia;
        if (c <= 15.5)       { xb = 0;     xa = 15.5;  ib = 0;   ia = 50;  }
        else if (c <= 55.4)  { xb = 15.5;  xa = 55.4;  ib = 50;  ia = 100; }
        else if (c <= 150.4) { xb = 55.4;  xa = 150.4; ib = 100; ia = 200; }
        else if (c <= 250.4) { xb = 150.4; xa = 250.4; ib = 200; ia = 300; }
        else if (c <= 500.0) { xb = 250.4; xa = 500.0; ib = 300; ia = 500; }
        else return 500;
        double ispu = ((ia - ib) / (xa - xb)) * (c - xb) + ib;
        return (int) Math.round(ispu);
    }

    /** Konversi nilai ISPU → level kategori 1–5 (untuk breath index & rekomendasi). */
    private int ispuToLevel(int ispu) {
        if (ispu <= 50) return 1;
        else if (ispu <= 100) return 2;
        else if (ispu <= 200) return 3;
        else if (ispu <= 300) return 4;
        else return 5;
    }


    private String getAqiLabel(int level) {
        switch (level) {
            case 1: return "BAIK";
            case 2: return "SEDANG";
            case 3: return "TIDAK SEHAT";
            case 4: return "SANGAT TIDAK SEHAT";
            default: return "BERBAHAYA";
        }
    }

    private String getAqiDesc(int level) {
        switch (level) {
            case 1: return "Tingkat mutu udara sangat baik";
            case 2: return "Kualitas udara masih dapat diterima";
            case 3: return "Bersifat merugikan bagi kelompok sensitif";
            case 4: return "Meningkatkan risiko kesehatan pada populasi terpapar";
            default: return "Berbahaya bagi kesehatan, perlu penanganan cepat";
        }
    }

    private void updateAqiUI(int level, double pm25) {
        String label, desc;
        int color;
        switch (level) {
            case 1:
                label = "Baik";
                desc = "Udara sangat bersih. Sempurna untuk aktivitas luar ruangan.";
                color = getResources().getColor(R.color.aqi_good, null); break;
            case 2:
                label = "Sedang";
                desc = "Kualitas udara masih dapat diterima untuk aktivitas luar.";
                color = getResources().getColor(R.color.aqi_moderate, null); break;
            case 3:
                label = "Tidak Sehat";
                desc = "Merugikan bagi kelompok sensitif. Batasi aktivitas luar.";
                color = getResources().getColor(R.color.aqi_sensitive, null); break;
            case 4:
                label = "Sangat Tidak Sehat";
                desc = "Meningkatkan risiko kesehatan. Hindari aktivitas luar ruangan.";
                color = getResources().getColor(R.color.aqi_unhealthy, null); break;
            default:
                label = "Berbahaya";
                desc = "Kondisi darurat kesehatan. Tetap di dalam ruangan.";
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

    private void updateRekomendasi(int level, double uv) {
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
            List<String> umum = new ArrayList<>();

            // -- Polusi udara --
            if (level == 3) {
                umum.add("🫁 Udara tidak sehat. Kurangi aktivitas luar ruangan yang berat.");
            } else if (level == 4) {
                umum.add("🫁 Udara sangat tidak sehat. Hindari aktivitas luar ruangan.");
            } else if (level >= 5) {
                umum.add("🫁 Udara berbahaya. Tetap di dalam ruangan & tutup ventilasi.");
            }

            // -- Sinar UV --
            if (uv >= 8) {
                umum.add("☀️ UV " + (int) uv + " sangat tinggi. Gunakan sunscreen SPF 30+ & hindari paparan tengah hari (10.00–16.00).");
            } else if (uv >= 6) {
                umum.add("☀️ UV " + (int) uv + " tinggi. Gunakan sunscreen saat beraktivitas di luar.");
            }

            if (umum.isEmpty()) {
                if (level <= 1 && uv < 3) {
                    tvRekoTitle.setText("Kondisi Sangat Baik");
                    tvRekoDesc.setText("Kualitas udara bersih dan UV rendah. Aman untuk semua aktivitas.");
                } else {
                    tvRekoTitle.setText("Kondisi Baik");
                    tvRekoDesc.setText("Udara dan UV dalam batas aman. Aktivitas luar ruangan tetap nyaman.");
                }
            } else {
                tvRekoTitle.setText(umum.size() > 1 ?
                        "⚠️ " + umum.size() + " Peringatan Hari Ini" : "Perhatian Hari Ini");
                tvRekoDesc.setText(String.join("\n\n", umum));
            }
            return;
        }


        List<String> warnings = new ArrayList<>();

        // ---- FAKTOR 1: POLUSI UDARA (Asma, ISPA, Eksim) ----
        List<String> polusiDiseases = new ArrayList<>();
        if (hasAsma) polusiDiseases.add("Asma");
        if (hasIspa) polusiDiseases.add("ISPA");
        if (hasEksim) polusiDiseases.add("Eksim");

        if (!polusiDiseases.isEmpty()) {
            String who = String.join(", ", polusiDiseases);
            if (level >= 4) {
                warnings.add("🫁 " + who + ": Udara sangat berbahaya. Tetap di dalam ruangan & siapkan obat/inhaler.");
            } else if (level == 3) {
                warnings.add("🫁 " + who + ": Udara tidak sehat. Gunakan masker N95 & batasi aktivitas luar.");
            } else if (level == 2) {
                warnings.add("🫁 " + who + ": Kualitas udara sedang. Batasi aktivitas berat di luar ruangan.");
            }
        }

        // ---- FAKTOR 2: SINAR UV (Lupus, Rosacea, Herpes, Eksim) ----
        List<String> uvDiseases = new ArrayList<>();
        if (hasLupus) uvDiseases.add("Lupus");
        if (hasRosacea) uvDiseases.add("Rosacea");
        if (hasHerpes) uvDiseases.add("Herpes");
        if (hasEksim) uvDiseases.add("Eksim");

        if (!uvDiseases.isEmpty()) {
            String who = String.join(", ", uvDiseases);
            if (uv >= 8) {
                warnings.add("☀️ " + who + ": UV " + (int) uv + " sangat tinggi. Hindari matahari, pakai SPF 50+ & pakaian tertutup.");
            } else if (uv >= 6) {
                warnings.add("☀️ " + who + ": UV " + (int) uv + " tinggi. Gunakan sunscreen SPF 50+ & pakaian pelindung.");
            } else if (uv >= 3) {
                warnings.add("☀️ " + who + ": UV sedang. Disarankan memakai sunscreen SPF 30+.");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancellationSource.cancel();
    }
}
