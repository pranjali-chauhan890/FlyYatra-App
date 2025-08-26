package com.v2v.flyyatra;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BookFlightFragment extends Fragment {

    private EditText etFromCity, etToCity, etDate;
    private Button btnSearchFlights;
    private RecyclerView rvAvailableFlights;
    private ArrayList<FlightModel> flightList;
    private PassengerFlightsAdapter bookFlightAdapter;
    private DatabaseReference flightsRef;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final int PAYMENT_REQUEST_CODE = 1001;

    public BookFlightFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_book_flight, container, false);

        etFromCity = view.findViewById(R.id.etFromCity);
        etToCity = view.findViewById(R.id.etToCity);
        etDate = view.findViewById(R.id.etDate);
        btnSearchFlights = view.findViewById(R.id.btnSearchFlights);
        rvAvailableFlights = view.findViewById(R.id.rvAvailableFlights);

        rvAvailableFlights.setLayoutManager(new LinearLayoutManager(getContext()));
        flightList = new ArrayList<>();
        bookFlightAdapter = new PassengerFlightsAdapter(getContext(), flightList, false);
        rvAvailableFlights.setAdapter(bookFlightAdapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        etDate.setOnClickListener(v -> showDatePicker());

        btnSearchFlights.setOnClickListener(v -> searchFlights());

        // Handle booking when user taps a flight card
        bookFlightAdapter.setOnFlightClickListener(this::openFakePayment);

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%d", dayOfMonth, (month + 1), year);
                    etDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void searchFlights() {
        String fromCity = etFromCity.getText().toString().trim();
        String toCity = etToCity.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (TextUtils.isEmpty(fromCity) || TextUtils.isEmpty(toCity) || TextUtils.isEmpty(date)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        flightsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flightList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    FlightModel flight = ds.getValue(FlightModel.class);
                    if (flight != null) {
                        flight.setFlightId(ds.getKey());

                        String dbFrom = flight.getDeparture() != null ? flight.getDeparture().trim() : "";
                        String dbTo = flight.getArrival() != null ? flight.getArrival().trim() : "";
                        String dbDate = normalizeDate(flight.getDate());

                        String userFrom = fromCity.trim();
                        String userTo = toCity.trim();
                        String userDate = normalizeDate(date);

                        boolean matches = dbFrom.equalsIgnoreCase(userFrom)
                                && dbTo.equalsIgnoreCase(userTo)
                                && dbDate.equals(userDate);

                        if (matches) {
                            try {
                                Date today = sdf.parse(sdf.format(new Date()));
                                Date flightDate = sdf.parse(dbDate);
                                if (flightDate != null && !flightDate.before(today)) {
                                    flightList.add(flight);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                bookFlightAdapter.notifyDataSetChanged();

                if (flightList.isEmpty()) {
                    Toast.makeText(getContext(), "No flights found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private String normalizeDate(String inputDate) {
        try {
            Date d = sdf.parse(inputDate.trim());
            return sdf.format(d);
        } catch (Exception e) {
            return inputDate.trim();
        }
    }

    // ✅ Instead of direct booking → Open FakePaymentActivity
    private void openFakePayment(FlightModel flight) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = new Intent(getContext(), FakePaymentActivity.class);
        intent.putExtra("flight", flight);
        intent.putExtra("userId", userId);
        startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    // ✅ Handle result from FakePaymentActivity
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            FlightModel paidFlight = (FlightModel) data.getSerializableExtra("flight");

            if (paidFlight != null) {
                Toast.makeText(getContext(), "Booking Confirmed ✅", Toast.LENGTH_SHORT).show();
            }
        }
    }
}