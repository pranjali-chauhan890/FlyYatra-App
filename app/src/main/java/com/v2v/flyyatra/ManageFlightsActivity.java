package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageFlightsActivity extends AppCompatActivity {

    private RecyclerView recyclerFlights;
    private FlightsAdapter adapter;
    private List<FlightModel> flightsList, filteredList;
    private DatabaseReference flightsRef;
    private ValueEventListener flightsListener;

    private ProgressBar progressBar;
    private EditText etSearchFlights;
    private ImageButton btnSortFlights;
    private TextView tvTotalFlights;
    private Button btnAddFlight;

    private boolean sortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_flights);

        recyclerFlights = findViewById(R.id.recyclerFlights);
        progressBar = findViewById(R.id.progressBar);
        etSearchFlights = findViewById(R.id.etSearchFlights);
        btnSortFlights = findViewById(R.id.btnSortFlights);
        tvTotalFlights = findViewById(R.id.tvTotalFlights);
        btnAddFlight = findViewById(R.id.btnAddFlight);

        recyclerFlights.setLayoutManager(new LinearLayoutManager(this));
        flightsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new FlightsAdapter(this, filteredList);
        recyclerFlights.setAdapter(adapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        // ✅ Add new flight
        btnAddFlight.setOnClickListener(v ->
                startActivity(new Intent(ManageFlightsActivity.this, AddFlightActivity.class))
        );

        // ✅ Edit / Delete / Change Status
        adapter.setOnItemAction(new FlightsAdapter.OnItemAction() {
            @Override
            public void onEdit(FlightModel flight) {
                Intent intent = new Intent(ManageFlightsActivity.this, AddFlightActivity.class);
                intent.putExtra("flight", flight);
                startActivity(intent);
            }

            @Override
            public void onDelete(FlightModel flight) {
                confirmDelete(flight);
            }

            @Override
            public void onChangeStatus(FlightModel flight) {
                showStatusDialog(flight);
            }
        });

        // ✅ Search
        etSearchFlights.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFlights(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ✅ Sort
        btnSortFlights.setOnClickListener(v -> {
            sortAscending = !sortAscending;
            sortFlights();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFlights();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (flightsListener != null) {
            flightsRef.removeEventListener(flightsListener);
        }
    }

    private void loadFlights() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        if (flightsListener != null) {
            flightsRef.removeEventListener(flightsListener);
        }

        flightsListener = flightsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                flightsList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FlightModel flight = ds.getValue(FlightModel.class);
                    if (flight != null && flight.getFlightId() != null) {
                        if (flight.getStatus() == null || flight.getStatus().isEmpty()) {
                            flight.setStatus("Scheduled"); // ✅ default
                        }
                        flightsList.add(flight);
                    }
                }
                tvTotalFlights.setText("Total Flights: " + flightsList.size());
                filterFlights(etSearchFlights.getText().toString());
                progressBar.setVisibility(android.view.View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(ManageFlightsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(FlightModel flight) {
        if (flight.getFlightId() == null || flight.getFlightId().isEmpty()) {
            Toast.makeText(this, "Flight ID missing, cannot delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String flightInfo = (flight.getAirline() != null ? flight.getAirline() : "Flight") +
                " " + (flight.getFlightNumber() != null ? flight.getFlightNumber() : "");

        new AlertDialog.Builder(this)
                .setTitle("Delete Flight")
                .setMessage("Are you sure you want to delete " + flightInfo + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    flightsRef.child(flight.getFlightId()).removeValue()
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(ManageFlightsActivity.this, "Flight deleted", Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(ManageFlightsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ✅ Show Status Update Dialog
    private void showStatusDialog(FlightModel flight) {
        String[] statuses = {"Scheduled", "Confirmed", "Departed", "Cancelled"};

        new AlertDialog.Builder(this)
                .setTitle("Update Status for " + flight.getFlightNumber())
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    flightsRef.child(flight.getFlightId()).child("status").setValue(newStatus)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(ManageFlightsActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(ManageFlightsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void filterFlights(String query) {
        filteredList.clear();
        for (FlightModel flight : flightsList) {
            if ((flight.getFlightNumber() != null && flight.getFlightNumber().toLowerCase().contains(query.toLowerCase())) ||
                    (flight.getDeparture() != null && flight.getDeparture().toLowerCase().contains(query.toLowerCase())) ||
                    (flight.getArrival() != null && flight.getArrival().toLowerCase().contains(query.toLowerCase())) ||
                    (flight.getAirline() != null && flight.getAirline().toLowerCase().contains(query.toLowerCase())) ||
                    (String.valueOf(flight.getPrice()).contains(query)) ||
                    (flight.getStatus() != null && flight.getStatus().toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(flight);
            }
        }
        sortFlights();
    }

    private void sortFlights() {
        Collections.sort(filteredList, (f1, f2) -> {
            if (f1.getDate() == null || f2.getDate() == null) return 0;
            return sortAscending ? f1.getDate().compareTo(f2.getDate()) : f2.getDate().compareTo(f1.getDate());
        });
        adapter.notifyDataSetChanged();
    }
}