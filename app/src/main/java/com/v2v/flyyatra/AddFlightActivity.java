package com.v2v.flyyatra;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddFlightActivity extends AppCompatActivity {

    private Spinner spAirline, spStatus;
    private EditText etFlightNumber, etDepartureCity, etArrivalCity, etFlightDate, etFlightTime, etPrice;
    private Button btnAddFlight;
    private DatabaseReference flightsRef;
    private FlightModel editFlight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_flight);

        spAirline = findViewById(R.id.spAirline);
        spStatus = findViewById(R.id.spStatus);
        etFlightNumber = findViewById(R.id.etFlightNumber);
        etDepartureCity = findViewById(R.id.etDepartureCity);
        etArrivalCity = findViewById(R.id.etArrivalCity);
        etFlightDate = findViewById(R.id.etFlightDate);
        etFlightTime = findViewById(R.id.etDepartureTime);
        etPrice = findViewById(R.id.etPrice);
        btnAddFlight = findViewById(R.id.btnAddFlight);

        // ✅ Airline Spinner
        String[] airlines = {"Air India", "IndiGo", "SpiceJet", "Go First", "Vistara"};
        ArrayAdapter<String> airlineAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, airlines);
        spAirline.setAdapter(airlineAdapter);

        // ✅ Status Spinner
        String[] statuses = {"Scheduled", "Confirmed", "Departed", "Cancelled"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spStatus.setAdapter(statusAdapter);

        // Firebase reference
        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        // Date picker
        etFlightDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            DatePickerDialog dp = new DatePickerDialog(this,
                    (view, year, month, day) -> etFlightDate.setText(day + "/" + (month + 1) + "/" + year),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            dp.show();
        });

        // Time picker
        etFlightTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            TimePickerDialog tp = new TimePickerDialog(this,
                    (view, hour, minute) -> etFlightTime.setText(String.format("%02d:%02d", hour, minute)),
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
            tp.show();
        });

        // Check if editing
        if (getIntent().hasExtra("flight")) {
            editFlight = (FlightModel) getIntent().getSerializableExtra("flight");
            fillEditData();
            btnAddFlight.setText("Update Flight");
        }

        btnAddFlight.setOnClickListener(v -> saveFlight());
    }

    private void fillEditData() {
        if (editFlight != null) {
            // Airline
            spAirline.setSelection(((ArrayAdapter<String>) spAirline.getAdapter()).getPosition(editFlight.getAirline()));

            // Status
            if (editFlight.getStatus() != null) {
                int statusPos = ((ArrayAdapter<String>) spStatus.getAdapter()).getPosition(editFlight.getStatus());
                if (statusPos >= 0) spStatus.setSelection(statusPos);
            }

            etFlightNumber.setText(editFlight.getFlightNumber());
            etDepartureCity.setText(editFlight.getDeparture());
            etArrivalCity.setText(editFlight.getArrival());
            etFlightDate.setText(editFlight.getDate());
            etFlightTime.setText(editFlight.getTime());
            etPrice.setText(String.valueOf(editFlight.getPrice()));
        }
    }

    private void saveFlight() {
        String airline = spAirline.getSelectedItem().toString();
        String number = etFlightNumber.getText().toString().trim();
        String dep = etDepartureCity.getText().toString().trim();
        String arr = etArrivalCity.getText().toString().trim();
        String date = etFlightDate.getText().toString().trim();
        String time = etFlightTime.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String status = spStatus.getSelectedItem().toString();

        if (airline.isEmpty() || number.isEmpty() || dep.isEmpty() || arr.isEmpty() || date.isEmpty() || time.isEmpty() || priceStr.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price = Double.parseDouble(priceStr);

        if (editFlight != null) {
            // UPDATE EXISTING
            Map<String, Object> updates = new HashMap<>();
            updates.put("airline", airline);
            updates.put("flightNumber", number);
            updates.put("departure", dep);
            updates.put("arrival", arr);
            updates.put("date", date);
            updates.put("time", time);
            updates.put("price", price);
            updates.put("status", status);

            flightsRef.child(editFlight.getFlightId()).updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Flight updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // ADD NEW
            String id = flightsRef.push().getKey();

            FlightModel newFlight = new FlightModel(
                    id,
                    null,
                    airline,
                    number,
                    dep,
                    arr,
                    date,
                    time,
                    status,   // ✅ take from spinner
                    price
            );

            flightsRef.child(id).setValue(newFlight)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Flight added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}