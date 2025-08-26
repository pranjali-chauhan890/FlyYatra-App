package com.v2v.flyyatra;

import android.Manifest;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder> {
    private int[] images;
    private String[] titles;
    private String[] descriptions;
    private OnboardingActivity activity; // Safe direct reference

    public OnboardingAdapter(OnboardingActivity activity, int[] images, String[] titles, String[] descriptions) {
        this.activity = activity;
        this.images = images;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    @NonNull
    @Override
    public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.onboarding_item, parent, false);
        return new OnboardingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
        holder.image.setImageResource(images[position]);
        holder.title.setText(titles[position]);
        holder.description.setText(descriptions[position]);

        holder.actionButton.setVisibility(View.GONE);
        holder.skipText.setVisibility(View.GONE);

        // ✅ Location Permission
        if (titles[position].contains("Enable Location")) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText("Enable Location Services");
            holder.actionButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
                } else {
                    Toast.makeText(activity, "Location already enabled", Toast.LENGTH_SHORT).show();
                }
            });

            holder.skipText.setVisibility(View.VISIBLE);
            holder.skipText.setOnClickListener(v -> activity.goToPage(2)); // skip location → next slide
        }
        // ✅ Notifications Permission
        else if (titles[position].contains("Allow Notifications")) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText("Allow Notifications");

            holder.actionButton.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS}, 102);
                    } else {
                        activity.sendTestNotification(activity);
                        activity.goToGetStarted(); // ✅ go to GetStarted after notification
                    }
                } else {
                    activity.sendTestNotification(activity);
                    activity.goToGetStarted(); // ✅ go to GetStarted after notification
                }
            });

            holder.skipText.setVisibility(View.VISIBLE);
            holder.skipText.setOnClickListener(v -> activity.goToGetStarted()); // ✅ skip → GetStarted
        }
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, description;
        Button actionButton;
        TextView skipText;

        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.onboardingImage);
            title = itemView.findViewById(R.id.onboardingTitle);
            description = itemView.findViewById(R.id.onboardingDescription);
            actionButton = itemView.findViewById(R.id.onboardingButton);
            skipText = itemView.findViewById(R.id.skipText);
        }
    }
}