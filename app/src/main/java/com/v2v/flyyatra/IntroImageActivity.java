package com.v2v.flyyatra;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_image);

        ImageView imageView = findViewById(R.id.introImage);
        imageView.setAlpha(0f);
        imageView.animate().alpha(1f).setDuration(500);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(IntroImageActivity.this, OnboardingActivity.class));
            finish();
        }, 1000); // 1 second display
    }
}
