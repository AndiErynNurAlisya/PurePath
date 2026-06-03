package com.example.purepath.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.purepath.R;

public class HomeFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView tvGreeting, tvRekoTitle, tvRekoDesc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("PurePathPrefs", 0);

        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvRekoTitle = view.findViewById(R.id.tv_reko_title);
        tvRekoDesc = view.findViewById(R.id.tv_reko_desc);

        loadUserGreeting();
        updateRekomendasi(78, 3); // nanti diganti dari API

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update rekomendasi setiap kali kembali ke Home
        updateRekomendasi(78, 3);
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

    private void updateRekomendasi(int aqi, int uvIndex) {
        boolean hasAsma = prefs.getBoolean("health_asma", false);
        boolean hasIspa = prefs.getBoolean("health_ispa", false);
        boolean hasLupus = prefs.getBoolean("health_lupus", false);
        boolean hasEksim = prefs.getBoolean("health_eksim", false);
        boolean hasRosacea = prefs.getBoolean("health_rosacea", false);
        boolean hasHerpes = prefs.getBoolean("health_herpes", false);

        boolean anyCondition = hasAsma || hasIspa || hasLupus ||
                hasEksim || hasRosacea || hasHerpes;

        // Logika rekomendasi berdasarkan kondisi + data udara
        if (!anyCondition) {
            if (aqi <= 50) {
                tvRekoTitle.setText("Udara Bersih Hari Ini");
                tvRekoDesc.setText("Kualitas udara sangat baik. Aman untuk semua aktivitas luar ruangan.");
            } else if (aqi <= 100) {
                tvRekoTitle.setText("Udara Cukup Baik");
                tvRekoDesc.setText("Kualitas udara masih dapat diterima. Aktivitas luar ruangan tetap aman.");
            } else {
                tvRekoTitle.setText("Waspada Kualitas Udara");
                tvRekoDesc.setText("Kualitas udara kurang baik. Pertimbangkan untuk mengurangi aktivitas luar ruangan.");
            }
            return;
        }

        // Ada kondisi kesehatan
        StringBuilder title = new StringBuilder();
        StringBuilder desc = new StringBuilder();

        if ((hasAsma || hasIspa) && aqi > 100) {
            title.append("⚠️ Risiko Tinggi untuk Pernapasan");
            desc.append("AQI tinggi berbahaya untuk asma/ISPA Anda. Gunakan masker N95 dan hindari aktivitas luar ruangan.");
        } else if ((hasAsma || hasIspa) && aqi <= 50) {
            title.append("✅ Bebas Alergi Hari Ini");
            desc.append("Tingkat polutan aman untuk kondisi Anda. Anda dapat beraktivitas normal di luar ruangan.");
        } else if ((hasLupus || hasRosacea || hasHerpes) && uvIndex >= 6) {
            title.append("⚠️ UV Index Tinggi");
            desc.append("Indeks UV berbahaya untuk kondisi Anda. Gunakan tabir surya SPF 50+, pakaian pelindung, dan hindari paparan matahari langsung.");
        } else if ((hasEksim) && (aqi > 100 || uvIndex >= 6)) {
            title.append("⚠️ Waspada untuk Eksim");
            desc.append("Kombinasi polusi dan UV tinggi dapat memperburuk eksim Anda. Jaga kelembaban kulit dan gunakan pelindung.");
        } else {
            title.append("✅ Kondisi Relatif Aman");
            desc.append("Kualitas udara dan UV index saat ini masih dalam batas aman untuk kondisi kesehatan Anda.");
        }

        tvRekoTitle.setText(title.toString());
        tvRekoDesc.setText(desc.toString());
    }
}