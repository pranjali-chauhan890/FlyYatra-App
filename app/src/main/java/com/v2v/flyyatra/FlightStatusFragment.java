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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FlightStatusFragment extends Fragment {

    private EditText etFlightNumber, etFromCity, etToCity, etDate;
    private Button btnSearchStatus;
    private RecyclerView rvFlightStatus;
    private ArrayList<FlightModel> resultList;
    private PassengerFlightsAdapter adapter;
    private DatabaseReference flightsRef;

    public FlightStatusFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_flight_status, container, false);

        etFlightNumber = view.findViewById(R.id.etFlightNumber);
        etFromCity = view.findViewById(R.id.etFromCity);
        etToCity = view.findViewById(R.id.etToCity);
        etDate = view.findViewById(R.id.etDate);
        btnSearchStatus = view.findViewById(R.id.btnSearchStatus);
        rvFlightStatus = view.findViewById(R.id.rvFlightStatus);

        rvFlightStatus.setLayoutManager(new LinearLayoutManager(getContext()));
        resultList = new ArrayList<>();
        // 👇 PassengerFlightsAdapter updated to show price + status
        adapter = new PassengerFlightsAdapter(getContext(), resultList, true);
        rvFlightStatus.setAdapter(adapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        etDate.setOnClickListener(v -> showDatePicker());
        btnSearchStatus.setOnClickListener(v -> searchFlightStatus());

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", dayOfMonth, (month + 1), year);
                    etDate.setText(selectedDate);
                }, y, m, d);
        dialog.show();
    }

    private void searchFlightStatus() {
        String flightNo = etFlightNumber.getText().toString().trim();
        String from = etFromCity.getText().toString().trim();
        String to = etToCity.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (TextUtils.isEmpty(flightNo) && (TextUtils.isEmpty(from) || TextUtils.isEmpty(to) || TextUtils.isEmpty(date))) {
            Toast.makeText(getContext(), "Enter flight number OR From, To & Date", Toast.LENGTH_SHORT).show();
            return;
        }

        flightsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                resultList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    FlightModel flight = ds.getValue(FlightModel.class);
                    if (flight != null) {
                        boolean match = false;

                        // 🔍 Match by flight number
                        if (!TextUtils.isEmpty(flightNo) &&
                                flight.getFlightNumber() != null &&
                                flight.getFlightNumber().equalsIgnoreCase(flightNo)) {
                            match = true;
                        }

                        // 🔍 Match by route + date
                        if (!TextUtils.isEmpty(from) && !TextUtils.isEmpty(to) && !TextUtils.isEmpty(date)) {
                            if (flight.getDeparture() != null && flight.getArrival() != null && flight.getDate() != null) {
                                if (flight.getDeparture().equalsIgnoreCase(from) &&
                                        flight.getArrival().equalsIgnoreCase(to) &&
                                        flight.getDate().equalsIgnoreCase(date)) {
                                    match = true;
                                }
                            }
                        }

                        if (match) {
                            // ensure status is never null
                            if (flight.getStatus() == null) {
                                flight.setStatus("Confirmed");
                            }
                            resultList.add(flight);
                        }
                    }
                }

                if (resultList.isEmpty()) {
                    Toast.makeText(getContext(), "No flights found", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}