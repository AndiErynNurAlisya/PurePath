package com.example.purepath.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.purepath.R;
import com.example.purepath.adapter.DiaryAdapter;
import com.example.purepath.database.DiaryDao;
import com.example.purepath.model.DiaryEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiaryFragment extends Fragment {

    private BarChart barChart;
    private RecyclerView rvDiary;
    private DiaryAdapter adapter;
    private TextView tvWeeklyAvg, tvInsight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        barChart = view.findViewById(R.id.bar_chart);
        rvDiary = view.findViewById(R.id.rv_diary);
        tvWeeklyAvg = view.findViewById(R.id.tv_weekly_avg);
        tvInsight = view.findViewById(R.id.tv_insight);

        setupChart();
        setupRecyclerView();
        calculateInsight();

        return view;
    }

    private void setupChart() {
        DiaryDao dao = new DiaryDao(requireContext());
        List<DiaryEntry> last7 = dao.getLast7Entries();

        if (last7.isEmpty()) {
            barChart.setVisibility(android.view.View.GONE);
            tvWeeklyAvg.setText("Belum ada data minggu ini");
            return;
        }

        java.util.Collections.reverse(last7);

        List<BarEntry> entries = new ArrayList<>();
        int[] colors = new int[last7.size()];
        String[] labels = new String[last7.size()];

        int total = 0;
        for (int i = 0; i < last7.size(); i++) {
            DiaryEntry entry = last7.get(i);
            entries.add(new BarEntry(i, entry.getAqi()));
            total += entry.getAqi();

            // Warna berdasarkan AQI
            int aqi = entry.getAqi();
            if (aqi <= 50) colors[i] = Color.parseColor("#52B788");
            else if (aqi <= 100) colors[i] = Color.parseColor("#FFB703");
            else if (aqi <= 150) colors[i] = Color.parseColor("#F4845F");
            else colors[i] = Color.parseColor("#E63946");

            String dateStr = entry.getDate();
            if (dateStr.contains("-") && dateStr.length() >= 10) {
                String[] p = dateStr.split("-");
                labels[i] = p[2] + "/" + p[1];
            } else if (dateStr.contains(",")) {
                String[] p = dateStr.split(", ");
                labels[i] = p.length > 1 ? p[1] : dateStr;
            } else {
                labels[i] = dateStr.length() > 5 ? dateStr.substring(0, 5) : dateStr;
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "AQI");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setTextColor(Color.GRAY);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.length);

        barChart.invalidate();

        int avg = total / last7.size();
        String avgLabel = avg <= 50 ? "Baik" : avg <= 100 ? "Sedang" : "Buruk";
        tvWeeklyAvg.setText("Rata-rata AQI minggu ini: " + avg + " (" + avgLabel + ")");
    }

    private void setupRecyclerView() {
        DiaryDao dao = new DiaryDao(requireContext());
        List<DiaryEntry> diaryList = dao.getAllEntries();
        adapter = new DiaryAdapter(getContext(), diaryList, entry -> {});
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiary.setAdapter(adapter);
    }

    private void calculateInsight() {
        DiaryDao dao = new DiaryDao(requireContext());
        List<DiaryEntry> last7 = dao.getLast7Entries();
        if (last7.isEmpty()) return;

        int goodDays = 0, moderateDays = 0, badDays = 0;
        for (DiaryEntry entry : last7) {
            if (entry.getAqi() <= 50) goodDays++;
            else if (entry.getAqi() <= 100) moderateDays++;
            else badDays++;
        }

        String saran;
        if (badDays == 0 && moderateDays == 0) {
            saran = "Kerja bagus menjaga kesehatan paru-parumu!";
        } else if (badDays > goodDays) {
            saran = "Minggu ini lebih banyak udara buruk. Kurangi aktivitas luar ruangan "
                    + "dan gunakan masker saat keluar.";
        } else if (badDays > 0) {
            saran = "Masih ada beberapa hari berudara buruk. Tetap waspada dan siapkan masker.";
        } else {
            saran = "Kualitasnya cukup terjaga. Pertahankan kebiasaan baikmu!";
        }

        tvInsight.setText("Minggu ini kamu terpapar udara bersih " + goodDays
                + " hari, sedang " + moderateDays + " hari, buruk " + badDays + " hari. " + saran);
    }
}
