package com.example.purepath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.purepath.activity.LoginActivity;
import com.example.purepath.activity.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("PurePathPrefs", 0);
            boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

            Intent intent;
            if (isLoggedIn) {
                // Sudah pernah login → langsung ke MainActivity
                intent = new Intent(this, MainActivity.class);
            } else {
                // Belum login → ke LoginActivity
                intent = new Intent(this, LoginActivity.class);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }
}