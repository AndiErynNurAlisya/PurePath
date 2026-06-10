package com.example.purepath.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.purepath.R;
import com.example.purepath.model.DiaryEntry;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    private List<DiaryEntry> entries;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(DiaryEntry entry);
    }

    public DiaryAdapter(Context context, List<DiaryEntry> entries, OnItemClickListener listener) {
        this.context = context;
        this.entries = entries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = entries.get(position);

        // Jika string tanggal sudah terformat dari DB (e.g. "Rabu, 10 Jun"), 
        // kita tampilkan langsung agar tidak error saat parsing yyyy-MM-dd.
        String dateStr = entry.getDate();
        try {
            // Jika suatu saat kita simpan dalam format yyyy-MM-dd:
            if (dateStr.contains("-") && dateStr.length() == 10) {
                java.text.SimpleDateFormat inputFormat =
                    new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                java.text.SimpleDateFormat outputFormat =
                    new java.text.SimpleDateFormat("EEEE, dd MMM", new java.util.Locale("id"));
                java.util.Date date = inputFormat.parse(dateStr);
                holder.tvDate.setText(outputFormat.format(date));
            } else {
                holder.tvDate.setText(dateStr);
            }
        } catch (Exception e) {
            holder.tvDate.setText(dateStr);
        }

        holder.tvDesc.setText(entry.getDescription());
        holder.tvAqiBadge.setText(entry.getAqi() + " " + entry.getAqiLabel());

        // Warna badge berdasarkan AQI
        int aqi = entry.getAqi();
        int color;
        if (aqi <= 50) {
            color = context.getResources().getColor(R.color.aqi_good, null);
        } else if (aqi <= 100) {
            color = context.getResources().getColor(R.color.aqi_moderate, null);
        } else if (aqi <= 150) {
            color = context.getResources().getColor(R.color.aqi_sensitive, null);
        } else {
            color = context.getResources().getColor(R.color.aqi_unhealthy, null);
        }
        holder.tvAqiBadge.setBackgroundColor(color);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(entry));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvDesc, tvAqiBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            tvAqiBadge = itemView.findViewById(R.id.tv_aqi_badge);
        }
    }
}
