package com.v2v.flyyatra;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TripsPagerAdapter extends FragmentStateAdapter {

    public TripsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new UpcomingTripsFragment(); // will load upcoming
        } else {
            return new PastTripsFragment(); // will load past
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
