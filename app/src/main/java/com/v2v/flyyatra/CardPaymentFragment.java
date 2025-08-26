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

public class CardPaymentFragment extends Fragment {

    private PaymentPagerAdapter.PaymentListener listener;
    private FlightModel flight;
    private String userId;

    public void setPaymentListener(PaymentPagerAdapter.PaymentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_payment, container, false);

        flight = (FlightModel) getArguments().getSerializable("flight");
        userId = getArguments().getString("userId");

        EditText etCardNumber = view.findViewById(R.id.etCardNumber);
        EditText etExpiry = view.findViewById(R.id.etExpiry);
        EditText etCVV = view.findViewById(R.id.etCVV);
        EditText etName = view.findViewById(R.id.etName);
        Button btnPay = view.findViewById(R.id.btnPay);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnPay.setOnClickListener(v -> {
            String card = etCardNumber.getText().toString().trim();
            String expiry = etExpiry.getText().toString().trim();
            String cvv = etCVV.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (TextUtils.isEmpty(card) || TextUtils.isEmpty(expiry) || TextUtils.isEmpty(cvv) || TextUtils.isEmpty(name)) {
                etName.setError("Fill all details");
                return;
            }

            if (card.length() < 12 || cvv.length() < 3) {
                etCardNumber.setError("Invalid details");
                return;
            }

            listener.onPaymentSuccess(flight, userId);
        });

        btnCancel.setOnClickListener(v -> listener.onPaymentCancelled());

        return view;
    }
}