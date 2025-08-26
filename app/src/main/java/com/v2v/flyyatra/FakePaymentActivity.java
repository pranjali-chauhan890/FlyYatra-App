package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FakePaymentActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PaymentPagerAdapter pagerAdapter;

    private String userId;
    private FlightModel flight;
    private DatabaseReference bookingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_payment);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // ✅ Fare summary views
        TextView tvAirline = findViewById(R.id.tvAirline);
        TextView tvRoute = findViewById(R.id.tvRoute);
        TextView tvDate = findViewById(R.id.tvDate);
        TextView tvPrice = findViewById(R.id.tvPrice);

        // Get data
        flight = (FlightModel) getIntent().getSerializableExtra("flight");
        userId = getIntent().getStringExtra("userId");

        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        // ✅ Set flight details in summary
        if (flight != null) {
            tvAirline.setText(flight.getAirline() != null ? flight.getAirline() : "Unknown Airline");
            tvRoute.setText((flight.getDeparture() != null ? flight.getDeparture() : "") +
                    " → " + (flight.getArrival() != null ? flight.getArrival() : ""));
            tvDate.setText("Date: " + (flight.getDate() != null ? flight.getDate() : ""));
            tvPrice.setText("₹" + flight.getPrice());
        }

        // Setup adapter
        pagerAdapter = new PaymentPagerAdapter(this, flight, userId, new PaymentPagerAdapter.PaymentListener() {
            @Override
            public void onPaymentSuccess(@NonNull FlightModel flight, @NonNull String userId) {
                // ✅ Set booking status & bookingId
                flight.setStatus("Confirmed");
                String bookingId = bookingsRef.child(userId).push().getKey();
                flight.setBookingId(bookingId);

                // ✅ Save booking in Firebase
                bookingsRef.child(userId).child(bookingId).setValue(flight)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FakePaymentActivity.this, "Payment Successful ✅", Toast.LENGTH_SHORT).show();

                            // ✅ Move to Payment Success screen
                            Intent intent = new Intent(FakePaymentActivity.this, PaymentSuccessActivity.class);
                            intent.putExtra("flight", flight);
                            intent.putExtra("bookingId", bookingId);
                            startActivity(intent);

                            setResult(RESULT_OK); // send back success to BookFlightFragment
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(FakePaymentActivity.this, "Booking failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onPaymentCancelled() {
                Toast.makeText(FakePaymentActivity.this, "Payment Cancelled ❌", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        viewPager.setAdapter(pagerAdapter);

        // Tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Card");
            else tab.setText("UPI");
        }).attach();
    }
}