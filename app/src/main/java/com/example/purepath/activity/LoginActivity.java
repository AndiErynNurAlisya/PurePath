package com.example.purepath.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purepath.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRemember;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("PurePathPrefs", 0);

        // Kalau sudah login sebelumnya, langsung ke MainActivity
        if (prefs.getBoolean("is_logged_in", false)) {
            goToMain();
            return;
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        cbRemember = findViewById(R.id.cb_remember);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        TextView tvGoRegister = findViewById(R.id.tv_go_register);

        // Load email jika ingat saya aktif
        if (prefs.getBoolean("remember_me", false)) {
            etEmail.setText(prefs.getString("saved_email", ""));
            cbRemember.setChecked(true);
        }

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Email tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Password tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Cek kredensial dari SharedPreferences
        String savedEmail = prefs.getString("user_email", "");
        String savedPassword = prefs.getString("user_password", "");

        if (email.equals(savedEmail) && password.equals(savedPassword)) {
            // Login berhasil
            prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putBoolean("remember_me", cbRemember.isChecked())
                    .putString("saved_email", cbRemember.isChecked() ? email : "")
                    .apply();
            goToMain();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Email atau password salah", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}