package com.v2v.flyyatra;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PaymentPagerAdapter extends FragmentStateAdapter {

    public interface PaymentListener {
        void onPaymentSuccess(@NonNull FlightModel flight, @NonNull String userId);
        void onPaymentCancelled();
    }

    private final FlightModel flight;
    private final String userId;
    private final PaymentListener listener;

    public PaymentPagerAdapter(@NonNull FragmentActivity fa, FlightModel flight, String userId, PaymentListener listener) {
        super(fa);
        this.flight = flight;
        this.userId = userId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        args.putSerializable("flight", flight);
        args.putString("userId", userId);

        if (position == 0) {
            CardPaymentFragment cardFragment = new CardPaymentFragment();
            cardFragment.setPaymentListener(listener);
            cardFragment.setArguments(args);
            return cardFragment;
        } else {
            UpiPaymentFragment upiFragment = new UpiPaymentFragment();
            upiFragment.setPaymentListener(listener);
            upiFragment.setArguments(args);
            return upiFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Card + UPI
    }
}