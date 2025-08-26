package com.v2v.flyyatra;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class UpiPaymentFragment extends Fragment {

    private PaymentPagerAdapter.PaymentListener listener;
    private FlightModel flight;
    private String userId;

    public void setPaymentListener(PaymentPagerAdapter.PaymentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upi_payment, container, false);

        flight = (FlightModel) getArguments().getSerializable("flight");
        userId = getArguments().getString("userId");

        EditText etUpiId = view.findViewById(R.id.etUpiId);
        Button btnPay = view.findViewById(R.id.btnPayUpi);
        Button btnCancel = view.findViewById(R.id.btnCancelUpi);

        btnPay.setOnClickListener(v -> {
            String upi = etUpiId.getText().toString().trim();
            if (TextUtils.isEmpty(upi) || !upi.contains("@")) {
                etUpiId.setError("Enter valid UPI ID");
                return;
            }
            listener.onPaymentSuccess(flight, userId);
        });

        btnCancel.setOnClickListener(v -> listener.onPaymentCancelled());

        return view;
    }
}