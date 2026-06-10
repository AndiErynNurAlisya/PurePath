package com.example.purepath.fragment;

import android.content.SharedPreferences;
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
import com.example.purepath.network.AirPollutionResponse;
import com.example.purepath.network.ApiClient;
import com.example.purepath.network.GeocodingResponse;
import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private RecyclerView rvLocations;
    private LocationAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private List<Location> locationList = new ArrayList<>();
    private android.widget.EditText etSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        rvLocations = view.findViewById(R.id.rv_locations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        etSearch = view.findViewById(R.id.et_search);

        setupRecyclerView();
        loadRealData();
        setupSearch();
        setupChipFilter(view);

        swipeRefresh.setOnRefreshListener(() -> {
            loadRealData();
            swipeRefresh.setRefreshing(false);
        });

        return view;
    }

    private void setupRecyclerView() {
        adapter = new LocationAdapter(getContext(), locationList, new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Location location) {
                saveToRecentSearch(location);

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
                saveBookmark(location);
            }
        });

        rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLocations.setAdapter(adapter);
    }

    private void loadRealData() {
        // Daftar kota dengan koordinatnya
        List<double[]> cities = new ArrayList<>();
        String[][] cityInfo = {
                {"Jakarta Pusat", "DKI Jakarta, Indonesia", "-6.1751", "106.8650"},
                {"Bandung", "Jawa Barat, Indonesia", "-6.9175", "107.6191"},
                {"Surabaya", "Jawa Timur, Indonesia", "-7.2575", "112.7521"},
                {"Yogyakarta", "DI Yogyakarta, Indonesia", "-7.7956", "110.3695"},
                {"Medan", "Sumatera Utara, Indonesia", "3.5952", "98.6722"},
        };

        locationList.clear();
        adapter.notifyDataSetChanged();

        List<Location> results = new ArrayList<>();

        for (String[] city : cityInfo) {
            double lat = Double.parseDouble(city[2]);
            double lon = Double.parseDouble(city[3]);
            String name = city[0];
            String province = city[1];

            ApiClient.getWeatherService().getAirPollution(lat, lon, ApiClient.OWM_API_KEY)
                    .enqueue(new retrofit2.Callback<AirPollutionResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<AirPollutionResponse> call,
                                               retrofit2.Response<AirPollutionResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && !response.body().list.isEmpty() && isAdded()) {
                                int owmAqi = response.body().list.get(0).main.aqi;
                                int aqiDisplay = convertAqi(owmAqi);
                                String status = getAqiStatus(owmAqi);

                                Location loc = new Location(name, province, aqiDisplay,
                                        status, false, lat, lon);

                                synchronized (results) {
                                    results.add(loc);
                                    results.sort((a, b) -> a.getCityName().compareTo(b.getCityName()));
                                    requireActivity().runOnUiThread(() ->
                                            adapter.updateList(new ArrayList<>(results)));
                                }
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<AirPollutionResponse> call,
                                              Throwable t) {}
                    });
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadRealData();
                } else if (query.length() >= 3) {
                    searchFromApi(query);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupChipFilter(View view) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("PurePathPrefs", 0);

        com.google.android.material.chip.Chip chipFavorite =
                view.findViewById(R.id.chip_favorite);
        com.google.android.material.chip.Chip chipRecent =
                view.findViewById(R.id.chip_recent);

        chipFavorite.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                chipRecent.setChecked(false);
                String saved = prefs.getString("bookmarks", "");
                loadFromSaved(saved);
            } else {
                if (!chipRecent.isChecked()) {
                    loadRealData();
                }
            }
        });

        chipRecent.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                chipFavorite.setChecked(false);
                String saved = prefs.getString("recent_searches", "");
                loadFromSaved(saved);
            } else {
                if (!chipFavorite.isChecked()) {
                    loadRealData();
                }
            }
        });
    }

    private void searchFromApi(String query) {
        retrofit2.Call<List<GeocodingResponse>> call =
                ApiClient.getWeatherService().searchCity(query, 5, ApiClient.OWM_API_KEY);

        call.enqueue(new retrofit2.Callback<List<GeocodingResponse>>() {
            @Override
            public void onResponse(retrofit2.Call<List<GeocodingResponse>> call,
                                   retrofit2.Response<List<GeocodingResponse>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    List<GeocodingResponse> geoList = response.body();
                    List<Location> results = new ArrayList<>();
                    for (GeocodingResponse geo : geoList) {
                        fetchAqiForCity(geo, results, geoList.size());
                    }
                } else {
                    requireActivity().runOnUiThread(() -> {
                        locationList.clear();
                        adapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(retrofit2.Call<List<GeocodingResponse>> call,
                                  Throwable t) {}
        });
    }

    private void fetchAqiForCity(GeocodingResponse geo, List<Location> results, int total) {
        ApiClient.getWeatherService().getAirPollution(geo.lat, geo.lon, ApiClient.OWM_API_KEY)
                .enqueue(new retrofit2.Callback<AirPollutionResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<AirPollutionResponse> call,
                                           retrofit2.Response<AirPollutionResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && !response.body().list.isEmpty()) {
                            int owmAqi = response.body().list.get(0).main.aqi;
                            int aqiDisplay = convertAqi(owmAqi);
                            String status = getAqiStatus(owmAqi);
                            String province = geo.state != null ?
                                    geo.state + ", " + geo.country : geo.country;

                            String displayName = (geo.localNames != null && geo.localNames.id != null)
                                    ? geo.localNames.id : geo.name;

                            Location loc = new Location(displayName, province, aqiDisplay,
                                    status, false, geo.lat, geo.lon);

                            synchronized (results) {
                                results.add(loc);
                                if (results.size() == total) {
                                    requireActivity().runOnUiThread(() ->
                                            adapter.updateList(results));
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<AirPollutionResponse> call,
                                          Throwable t) {}
                });
    }

    private void loadFromSaved(String saved) {
        locationList.clear();

        if (saved.isEmpty()) {
            adapter.updateList(new ArrayList<>());

            android.widget.Toast.makeText(getContext(),
                    "Belum ada data tersimpan",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String[] entries = saved.split("##");
        List<Location> savedLocations = new ArrayList<>();

        for (String entry : entries) {
            String[] parts = entry.split("\\|");
            if (parts.length >= 6) {
                savedLocations.add(new Location(
                        parts[0], parts[1],
                        Integer.parseInt(parts[2]),
                        parts[3], false,
                        Double.parseDouble(parts[4]),
                        Double.parseDouble(parts[5])
                ));
            }
        }

        locationList.addAll(savedLocations);
        adapter.updateList(new ArrayList<>(locationList));
    }

    private void saveToRecentSearch(Location location) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("PurePathPrefs", 0);

        String existing = prefs.getString("recent_searches", "");
        String newEntry = location.getCityName() + "|" +
                location.getProvince() + "|" +
                location.getAqi() + "|" +
                location.getAqiStatus() + "|" +
                location.getLat() + "|" +
                location.getLon();

        List<String> recentList = new ArrayList<>();
        if (!existing.isEmpty()) {
            recentList = new ArrayList<>(java.util.Arrays.asList(existing.split("##")));
        }

        recentList.removeIf(s -> s.startsWith(location.getCityName() + "|"));
        recentList.add(0, newEntry);

        if (recentList.size() > 5) {
            recentList = recentList.subList(0, 5);
        }

        prefs.edit().putString("recent_searches",
                String.join("##", recentList)).apply();
    }

    private void saveBookmark(Location location) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("PurePathPrefs", 0);

        String existing = prefs.getString("bookmarks", "");
        String entry = location.getCityName() + "|" +
                location.getProvince() + "|" +
                location.getAqi() + "|" +
                location.getAqiStatus() + "|" +
                location.getLat() + "|" +
                location.getLon();

        List<String> bookmarkList = new ArrayList<>();
        if (!existing.isEmpty()) {
            bookmarkList = new ArrayList<>(java.util.Arrays.asList(existing.split("##")));
        }

        if (location.isBookmarked()) {
            bookmarkList.removeIf(s -> s.startsWith(location.getCityName() + "|"));
            bookmarkList.add(0, entry);
        } else {
            bookmarkList.removeIf(s -> s.startsWith(location.getCityName() + "|"));
        }

        prefs.edit().putString("bookmarks",
                String.join("##", bookmarkList)).apply();
    }

    private int convertAqi(int owmAqi) {
        switch (owmAqi) {
            case 1: return 25;
            case 2: return 75;
            case 3: return 125;
            case 4: return 175;
            default: return 250;
        }
    }

    private String getAqiStatus(int owmAqi) {
        switch (owmAqi) {
            case 1: return "Baik";
            case 2: return "Sedang";
            case 3: return "Tidak Sehat";
            case 4: return "Buruk";
            default: return "Berbahaya";
        }
    }
}