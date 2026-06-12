package com.example.purepath.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.purepath.R;
import com.example.purepath.model.Location;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<Location> locationList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Location location);
        void onBookmarkClick(Location location, int position);
    }

    public LocationAdapter(Context context, List<Location> locationList, OnItemClickListener listener) {
        this.context = context;
        this.locationList = locationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = locationList.get(position);

        holder.tvCityName.setText(location.getCityName());
        holder.tvProvince.setText(location.getProvince());
        holder.tvAqiValue.setText(location.getAqi() + " AQI");
        holder.tvAqiStatus.setText(location.getAqiStatus());

        // Warna berdasarkan AQI
        int aqi = location.getAqi();
        int color;
        if (aqi <= 50) {
            color = context.getResources().getColor(R.color.aqi_good, null);
        } else if (aqi <= 100) {
            color = context.getResources().getColor(R.color.aqi_moderate, null);
        } else if (aqi <= 150) {
            color = context.getResources().getColor(R.color.aqi_sensitive, null);
        } else if (aqi <= 200) {
            color = context.getResources().getColor(R.color.aqi_unhealthy, null);
        } else {
            color = context.getResources().getColor(R.color.aqi_hazardous, null);
        }

        holder.viewAqiColor.setBackgroundColor(color);
        holder.tvAqiValue.setTextColor(color);

        if (location.isBookmarked()) {
            holder.ivBookmark.setImageResource(R.drawable.ic_bookmark_filled);
            holder.ivBookmark.setColorFilter(
                    context.getResources().getColor(R.color.ocean, null));
        } else {
            holder.ivBookmark.setImageResource(R.drawable.ic_bookmark);
            holder.ivBookmark.clearColorFilter();
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(location));
        holder.ivBookmark.setOnClickListener(v -> listener.onBookmarkClick(location, position));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public void updateList(List<Location> newList) {
        this.locationList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewAqiColor;
        TextView tvCityName, tvProvince, tvAqiValue, tvAqiStatus;
        ImageView ivBookmark;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewAqiColor = itemView.findViewById(R.id.view_aqi_color);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvProvince = itemView.findViewById(R.id.tv_province);
            tvAqiValue = itemView.findViewById(R.id.tv_aqi_value);
            tvAqiStatus = itemView.findViewById(R.id.tv_aqi_status);
            ivBookmark = itemView.findViewById(R.id.iv_bookmark);
        }
    }
}