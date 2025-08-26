package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Gradient layout

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, IntroImageActivity.class));
            finish();
        }, 2000); // 2 seconds
    }
}