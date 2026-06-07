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

    private final String[] days = {"S", "S", "R", "K", "J", "S", "M"};
    private final int[] aqiData = {45, 38, 152, 48, 82, 31, 24};

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
        List<BarEntry> entries = new ArrayList<>();
        int[] colors = new int[aqiData.length];

        for (int i = 0; i < aqiData.length; i++) {
            entries.add(new BarEntry(i, aqiData[i]));
            if (aqiData[i] <= 50) colors[i] = Color.parseColor("#52B788");
            else if (aqiData[i] <= 100) colors[i] = Color.parseColor("#FFB703");
            else if (aqiData[i] <= 150) colors[i] = Color.parseColor("#F4845F");
            else colors[i] = Color.parseColor("#E63946");
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
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.GRAY);
        xAxis.setGranularity(1f);

        barChart.invalidate();
    }

    private void setupRecyclerView() {
        DiaryDao dao = new DiaryDao(requireContext());
        List<DiaryEntry> diaryList = dao.getAllEntries();

        if (diaryList.isEmpty()) {
            // Tampilkan dummy kalau belum ada data
            diaryList.add(new DiaryEntry("Hari ini", "Belum ada data tercatat", 0, "N/A"));
        }

        adapter = new DiaryAdapter(getContext(), diaryList, entry -> {});
        rvDiary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDiary.setAdapter(adapter);
    }

    private void calculateInsight() {
        int goodDays = 0, badDays = 0;
        int total = 0;

        for (int aqi : aqiData) {
            total += aqi;
            if (aqi <= 50) goodDays++;
            else if (aqi > 100) badDays++;
        }

        int avg = total / aqiData.length;
        String avgLabel = avg <= 50 ? "Baik" : avg <= 100 ? "Sedang" : "Buruk";
        tvWeeklyAvg.setText("Rata-rata AQI minggu ini: " + avg + " (" + avgLabel + ")");

        tvInsight.setText("Minggu ini kamu terpapar udara bersih " + goodDays +
                " hari, udara buruk " + badDays + " hari. " +
                (badDays == 0 ? "Kerja bagus menjaga kesehatan paru-parumu!" :
                        "Pertimbangkan menggunakan masker di hari berikutnya."));
    }
}