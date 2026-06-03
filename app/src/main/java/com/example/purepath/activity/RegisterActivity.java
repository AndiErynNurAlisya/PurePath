package com.example.purepath.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purepath.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prefs = getSharedPreferences("PurePathPrefs", 0);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        MaterialButton btnRegister = findViewById(R.id.btn_register);
        TextView tvGoLogin = findViewById(R.id.tv_go_login);

        btnRegister.setOnClickListener(v -> attemptRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), "Nama tidak boleh kosong", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(findViewById(android.R.id.content), "Email tidak valid", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 8) {
            Snackbar.make(findViewById(android.R.id.content), "Password minimal 8 karakter", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Snackbar.make(findViewById(android.R.id.content), "Password tidak cocok", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Simpan ke SharedPreferences
        prefs.edit()
                .putString("user_name", name)
                .putString("user_email", email)
                .putString("user_password", password)
                .apply();

        // Lanjut ke Onboarding
        Intent intent = new Intent(this, OnboardingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}