package com.example.purepath.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.purepath.R;
import com.example.purepath.activity.LoginActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private CheckBox cbAsma, cbIspa, cbLupus, cbEksim, cbRosacea, cbHerpes;
    private SwitchMaterial switchDarkMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireActivity().getSharedPreferences("PurePathPrefs", 0);

        cbAsma = view.findViewById(R.id.cb_asma);
        cbIspa = view.findViewById(R.id.cb_ispa);
        cbLupus = view.findViewById(R.id.cb_lupus);
        cbEksim = view.findViewById(R.id.cb_eksim);
        cbRosacea = view.findViewById(R.id.cb_rosacea);
        cbHerpes = view.findViewById(R.id.cb_herpes);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);

        TextView tvName = view.findViewById(R.id.tv_user_name);
        TextView tvEmail = view.findViewById(R.id.tv_user_email);
        MaterialButton btnLogout = view.findViewById(R.id.btn_logout);

        tvName.setText(prefs.getString("user_name", "Pengguna"));
        tvEmail.setText(prefs.getString("user_email", "email@purepath.ai"));

        cbAsma.setChecked(prefs.getBoolean("health_asma", false));
        cbIspa.setChecked(prefs.getBoolean("health_ispa", false));
        cbLupus.setChecked(prefs.getBoolean("health_lupus", false));
        cbEksim.setChecked(prefs.getBoolean("health_eksim", false));
        cbRosacea.setChecked(prefs.getBoolean("health_rosacea", false));
        cbHerpes.setChecked(prefs.getBoolean("health_herpes", false));

        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
        //auto-save chechkboc
        cbAsma.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_asma", isChecked).apply());

        cbIspa.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_ispa", isChecked).apply());

        cbLupus.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_lupus", isChecked).apply());

        cbEksim.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_eksim", isChecked).apply());

        cbRosacea.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_rosacea", isChecked).apply());

        cbHerpes.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean("health_herpes", isChecked).apply());

        // Logout
        btnLogout.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("is_logged_in", false)
                    .apply();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            
            startActivity(intent);

            requireActivity().finish();
        });

        return view;
    }
}
