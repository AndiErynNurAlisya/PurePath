package com.example.purepath.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.purepath.R;
import com.example.purepath.adapter.LocationAdapter;
import com.example.purepath.model.Location;
import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private RecyclerView rvLocations;
    private LocationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<Location> locationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        rvLocations = view.findViewById(R.id.rv_locations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        setupRecyclerView();
        loadDummyData();

        swipeRefresh.setOnRefreshListener(() -> {
            loadDummyData();
            swipeRefresh.setRefreshing(false);
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new LocationAdapter(getContext(), locationList, new LocationAdapter.OnItemClickListener() {
            // Ganti bagian onItemClick di setupRecyclerView()
            @Override
            public void onItemClick(Location location) {
                Bundle args = new Bundle();
                args.putString("cityName", location.getCityName());
                args.putDouble("lat", location.getLat());
                args.putDouble("lon", location.getLon());

                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_explore_to_detail, args);
            }

            @Override
            public void onBookmarkClick(Location location, int position) {
                location.setBookmarked(!location.isBookmarked());
                adapter.notifyItemChanged(position);
            }
        });

        rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLocations.setAdapter(adapter);
    }

    private void loadDummyData() {
        locationList.clear();
        locationList.add(new Location("Jakarta Pusat", "DKI Jakarta, Indonesia", 78, "Sedang", false, -6.1751, 106.8650));
        locationList.add(new Location("Bandung", "Jawa Barat, Indonesia", 32, "Baik", true, -6.9175, 107.6191));
        locationList.add(new Location("Surabaya", "Jawa Timur, Indonesia", 154, "Tidak Sehat", false, -7.2575, 112.7521));
        locationList.add(new Location("Yogyakarta", "DI Yogyakarta, Indonesia", 62, "Sedang", false, -7.7956, 110.3695));
        locationList.add(new Location("Medan", "Sumatera Utara, Indonesia", 45, "Baik", false, 3.5952, 98.6722));
        adapter.notifyDataSetChanged();
    }
}