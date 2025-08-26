package com.v2v.flyyatra;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private ImageView[] dots;
    private boolean nextButtonShownOnce = false;

    int[] images = {R.drawable.image1, R.drawable.image2, R.drawable.image3, R.drawable.image4};
    String[] titles = {
            "Explore The World",
            "Enable Location Services",
            "Flying Experience",
            "Allow Notifications"
    };
    String[] descriptions = {
            "Your journey begins here. Discover breathtaking destinations and curated experiences, all at your fingertips.",
            "Let FlyYatra guide you. Unlock tailored offers and the nearest airport details based on your location.",
            "From check-in to landing — enjoy a smooth, stress-free travel experience with real-time updates and assistance.",
            "Stay connected to your journey. Receive instant alerts on flight status, gate changes, and exclusive offers."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        ImageButton nextButton = findViewById(R.id.nextButton);
        TextView nextText = findViewById(R.id.nextText);

        // Adapter setup
        OnboardingAdapter adapter = new OnboardingAdapter(this, images, titles, descriptions);
        viewPager.setAdapter(adapter);

        addDots(0);

        // Next button click → move to next slide or finish onboarding
        View.OnClickListener nextClickListener = v -> {
            int currentPage = viewPager.getCurrentItem();
            if (currentPage < images.length - 1) {
                viewPager.setCurrentItem(currentPage + 1, true);
                nextButtonShownOnce = true;
            } else {
                goToGetStarted(); // ✅ always go to GetStartedActivity
            }
        };

        nextButton.setOnClickListener(nextClickListener);
        nextText.setOnClickListener(nextClickListener);

        // Dots & controls
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                addDots(position);
                if (position == 0 && !nextButtonShownOnce) {
                    showNextControls(nextButton, nextText);
                } else {
                    hideNextControls(nextButton, nextText);
                }
            }
        });
    }

    public void goToPage(int pageIndex) {
        viewPager.setCurrentItem(pageIndex, true);
    }

    // ✅ Open GetStartedActivity (instead of Signup directly)
    public void goToGetStarted() {
        startActivity(new Intent(OnboardingActivity.this, GetStartedActivity.class));
        finish();
    }

    // ✅ Only used if you want to jump to signup manually
    public void goToSignup() {
        startActivity(new Intent(OnboardingActivity.this, SignupActivity.class));
        finish();
    }

    private void showNextControls(ImageButton nextButton, TextView nextText) {
        nextButton.setVisibility(View.VISIBLE);
        nextText.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(500);
        nextButton.startAnimation(fadeIn);
        nextText.startAnimation(fadeIn);
    }

    private void hideNextControls(ImageButton nextButton, TextView nextText) {
        nextButton.setVisibility(View.GONE);
        nextText.setVisibility(View.GONE);
    }

    private void addDots(int position) {
        dotsLayout.removeAllViews();
        dots = new ImageView[images.length];

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(i == position ? R.drawable.active_dot : R.drawable.inactive_dot);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            dotsLayout.addView(dots[i], params);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) { // Location
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location enabled successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) { // Notifications
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendTestNotification(this);
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
            // ✅ After permission → go to GetStarted (not signup)
            goToGetStarted();
        }
    }

    public void sendTestNotification(Context context) {
        String channelId = "flyyatra_channel";
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "FlyYatra Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("FlyYatra")
                .setContentText("Notifications are now enabled!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(1, builder.build());
    }
}