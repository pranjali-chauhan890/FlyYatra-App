package com.v2v.flyyatra;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class PassengerHomeFragment extends Fragment {

    private EditText etFromCity, etToCity, etDate;
    private Button btnSearchFlights;
    private RecyclerView rvUpcomingFlights;
    private TextView tvNoFlights;
    private ArrayList<FlightModel> flightList;
    private PassengerFlightAdapter passengerFlightAdapter;
    private DatabaseReference flightsRef;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PassengerHomeFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_passenger_home, container, false);

        etFromCity = view.findViewById(R.id.etFromCity);
        etToCity = view.findViewById(R.id.etToCity);
        etDate = view.findViewById(R.id.etDate);
        btnSearchFlights = view.findViewById(R.id.btnSearchFlights);
        rvUpcomingFlights = view.findViewById(R.id.rvUpcomingFlights);
        tvNoFlights = view.findViewById(R.id.tvNoFlights);

        rvUpcomingFlights.setLayoutManager(new LinearLayoutManager(getContext()));
        flightList = new ArrayList<>();
        passengerFlightAdapter = new PassengerFlightAdapter(getContext(), flightList);
        rvUpcomingFlights.setAdapter(passengerFlightAdapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        etDate.setOnClickListener(v -> showDatePicker());

        // Load all upcoming flights initially
        loadFlights();

        btnSearchFlights.setOnClickListener(v -> searchFlights());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (DatePicker view, int year1, int month1, int dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", dayOfMonth, (month1 + 1), year1);
                    etDate.setText(selectedDate);
                }, year, month, day);
        dialog.show();
    }

    private void loadFlights() {
        flightsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flightList.clear();
                Date today = new Date();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FlightModel flight = dataSnapshot.getValue(FlightModel.class);
                    if (flight != null && flight.getDate() != null) {
                        try {
                            Date flightDate = sdf.parse(flight.getDate());
                            if (flightDate != null && !flightDate.before(sdf.parse(sdf.format(today)))) {

                                // ✅ Ensure price and status
                                if (flight.getPrice() == 0) {
                                    flight.setPrice(0); // default ₹0
                                }
                                if (flight.getStatus() == null || flight.getStatus().isEmpty()) {
                                    flight.setStatus("Scheduled");
                                }

                                flightList.add(flight);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                sortFlights();
                passengerFlightAdapter.notifyDataSetChanged();
                toggleNoFlightsView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void searchFlights() {
        String fromCity = etFromCity.getText().toString().trim();
        String toCity = etToCity.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        flightsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flightList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FlightModel flight = dataSnapshot.getValue(FlightModel.class);

                    if (flight != null) {
                        boolean matches = true;

                        String dep = flight.getDeparture() != null ? flight.getDeparture().trim() : "";
                        String arr = flight.getArrival() != null ? flight.getArrival().trim() : "";
                        String flightDateStr = flight.getDate() != null ? flight.getDate().trim() : "";

                        // Match From City
                        if (!TextUtils.isEmpty(fromCity) && !dep.equalsIgnoreCase(fromCity)) {
                            matches = false;
                        }

                        // Match To City
                        if (!TextUtils.isEmpty(toCity) && !arr.equalsIgnoreCase(toCity)) {
                            matches = false;
                        }

                        // Match Date
                        if (!TextUtils.isEmpty(date)) {
                            try {
                                Date inputDate = sdf.parse(date);
                                Date flightDate = sdf.parse(flightDateStr);
                                if (flightDate == null || inputDate == null || !flightDate.equals(inputDate)) {
                                    matches = false;
                                }
                            } catch (Exception e) {
                                matches = false;
                            }
                        }

                        // Skip past flights
                        try {
                            Date today = sdf.parse(sdf.format(new Date()));
                            Date flightDate = sdf.parse(flightDateStr);
                            if (flightDate != null && flightDate.before(today)) {
                                matches = false;
                            }
                        } catch (Exception e) {
                            matches = false;
                        }

                        // ✅ Ensure price and status
                        if (matches) {
                            if (flight.getPrice() == 0) {
                                flight.setPrice(0);
                            }
                            if (flight.getStatus() == null || flight.getStatus().isEmpty()) {
                                flight.setStatus("Scheduled");
                            }

                            flightList.add(flight);
                        }
                    }
                }

                sortFlights();
                passengerFlightAdapter.notifyDataSetChanged();
                toggleNoFlightsView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void sortFlights() {
        Collections.sort(flightList, (f1, f2) -> {
            try {
                Date date1 = sdf.parse(f1.getDate());
                Date date2 = sdf.parse(f2.getDate());
                if (date1 != null && date2 != null) {
                    return date1.compareTo(date2);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    private void toggleNoFlightsView() {
        if (flightList.isEmpty()) {
            tvNoFlights.setVisibility(View.VISIBLE);
            rvUpcomingFlights.setVisibility(View.GONE);
        } else {
            tvNoFlights.setVisibility(View.GONE);
            rvUpcomingFlights.setVisibility(View.VISIBLE);
        }
    }
}