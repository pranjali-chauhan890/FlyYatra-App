package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class PaymentSuccessActivity extends AppCompatActivity {

    private LottieAnimationView successAnimation;
    private Button btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        TextView tvBookingId = findViewById(R.id.tvBookingId);
        TextView tvAirline = findViewById(R.id.tvAirline);
        TextView tvRoute = findViewById(R.id.tvRoute);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvPrice = findViewById(R.id.tvPrice);
        btnDone = findViewById(R.id.btnDone);
        successAnimation = findViewById(R.id.successAnimation);

        // ✅ Hide Done button initially
        btnDone.setVisibility(View.GONE);

        // ✅ Get flight + bookingId from intent
        FlightModel flight = (FlightModel) getIntent().getSerializableExtra("flight");
        String bookingId = getIntent().getStringExtra("bookingId");

        if (flight != null) {
            tvAirline.setText(flight.getAirline() != null ? flight.getAirline() : "Unknown Airline");

            String route = (flight.getDeparture() != null ? flight.getDeparture() : "Unknown")
                    + " → " + (flight.getArrival() != null ? flight.getArrival() : "Unknown");
            tvRoute.setText(route);

            tvDate.setText("Date: " + (flight.getDate() != null ? flight.getDate() : "N/A"));
            tvPrice.setText("₹" + (flight.getPrice() > 0 ? flight.getPrice() : 0));
        } else {
            tvAirline.setText("Unknown Airline");
            tvRoute.setText("N/A");
            tvDate.setText("Date: N/A");
            tvPrice.setText("₹0");
        }

        if (bookingId != null) {
            tvBookingId.setText("Booking ID: " + bookingId);
        } else {
            tvBookingId.setText("Booking ID: N/A");
        }

        // ✅ Show Done button only when animation finishes
        successAnimation.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animator) {}

            @Override
            public void onAnimationEnd(android.animation.Animator animator) {
                btnDone.setVisibility(View.VISIBLE);
                // ✅ Fade-in effect
                AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(600);
                btnDone.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationCancel(android.animation.Animator animator) {}

            @Override
            public void onAnimationRepeat(android.animation.Animator animator) {}
        });

        // ✅ Done button -> Go back to Passenger Dashboard
        btnDone.setOnClickListener(v -> {
            Intent i = new Intent(PaymentSuccessActivity.this, PassengerDashboardActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}