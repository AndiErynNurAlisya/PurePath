package com.example.purepath.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purepath.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class OnboardingActivity extends AppCompatActivity {

    private CheckBox cbAsma, cbIspa, cbLupus, cbEksim, cbRosacea, cbHerpes, cbNone;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        prefs = getSharedPreferences("PurePathPrefs", 0);

        cbAsma = findViewById(R.id.cb_asma);
        cbIspa = findViewById(R.id.cb_ispa);
        cbLupus = findViewById(R.id.cb_lupus);
        cbEksim = findViewById(R.id.cb_eksim);
        cbRosacea = findViewById(R.id.cb_rosacea);
        cbHerpes = findViewById(R.id.cb_herpes);
        cbNone = findViewById(R.id.cb_none);
        MaterialButton btnLanjut = findViewById(R.id.btn_lanjut);

        // Jika pilih "Tidak Ada", uncheck semua yang lain
        cbNone.setOnCheckedChangeListener((btn, isChecked) -> {
            if (isChecked) {
                cbAsma.setChecked(false);
                cbIspa.setChecked(false);
                cbLupus.setChecked(false);
                cbEksim.setChecked(false);
                cbRosacea.setChecked(false);
                cbHerpes.setChecked(false);
            }
        });

        // Jika pilih penyakit lain, uncheck "Tidak Ada"
        cbAsma.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });
        cbIspa.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });
        cbLupus.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });
        cbEksim.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });
        cbRosacea.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });
        cbHerpes.setOnCheckedChangeListener((btn, isChecked) -> { if (isChecked) cbNone.setChecked(false); });

        btnLanjut.setOnClickListener(v -> {
            if (!cbAsma.isChecked() && !cbIspa.isChecked() && !cbLupus.isChecked()
                    && !cbEksim.isChecked() && !cbRosacea.isChecked()
                    && !cbHerpes.isChecked() && !cbNone.isChecked()) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Pilih minimal satu kondisi", Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Simpan health profile
            prefs.edit()
                    .putBoolean("health_asma", cbAsma.isChecked())
                    .putBoolean("health_ispa", cbIspa.isChecked())
                    .putBoolean("health_lupus", cbLupus.isChecked())
                    .putBoolean("health_eksim", cbEksim.isChecked())
                    .putBoolean("health_rosacea", cbRosacea.isChecked())
                    .putBoolean("health_herpes", cbHerpes.isChecked())
                    .putBoolean("is_logged_in", true)
                    .apply();

            // Ke MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}