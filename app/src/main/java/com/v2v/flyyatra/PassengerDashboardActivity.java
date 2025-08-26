package com.v2v.flyyatra;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PassengerDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long backPressedTime;   // To track double back press
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_dashboard);

        bottomNavigationView = findViewById(R.id.passenger_bottom_nav);

        // Load Home fragment first
        loadFragment(new PassengerHomeFragment());
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new PassengerHomeFragment();
            } else if (itemId == R.id.nav_book_flight) {
                selectedFragment = new BookFlightFragment();
            } else if (itemId == R.id.nav_my_trips) {
                selectedFragment = new MyTripsFragment();
            } else if (itemId == R.id.nav_flight_status) {
                selectedFragment = new FlightStatusFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new PassengerProfileFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.passenger_fragment_container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // If not on Home tab, go back to Home first
        if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            loadFragment(new PassengerHomeFragment());
        } else {
            // Double-tap back to exit
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                if (backToast != null) backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }
    }
}