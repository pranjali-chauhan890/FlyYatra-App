package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageFlightsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddFlight;
    private FlightsAdapter adapter;
    private List<FlightModel> flightsList, filteredList;
    private DatabaseReference flightsRef;
    private ValueEventListener flightsListener;

    private EditText etSearchFlights;
    private ImageButton btnSortFlights;
    private TextView tvTotalFlights;
    private boolean sortAscending = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_flights, container, false);

        recyclerView = view.findViewById(R.id.recyclerFlights);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddFlight = view.findViewById(R.id.fabAddFlight);
        etSearchFlights = view.findViewById(R.id.etSearchFlights);
        btnSortFlights = view.findViewById(R.id.btnSortFlights);
        tvTotalFlights = view.findViewById(R.id.tvTotalFlights);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        flightsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new FlightsAdapter(requireContext(), filteredList);
        recyclerView.setAdapter(adapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        // ✅ Add Flight
        fabAddFlight.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddFlightActivity.class))
        );

        // ✅ Edit / Delete / Change Status
        adapter.setOnItemAction(new FlightsAdapter.OnItemAction() {
            @Override
            public void onEdit(FlightModel flight) {
                Intent intent = new Intent(getContext(), AddFlightActivity.class);
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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFlights();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flightsListener != null) {
            flightsRef.removeEventListener(flightsListener);
        }
    }

    private void loadFlights() {
        progressBar.setVisibility(View.VISIBLE);

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
                        // ✅ Ensure status is never null
                        if (flight.getStatus() == null || flight.getStatus().isEmpty()) {
                            flight.setStatus("Scheduled");
                        }
                        flightsList.add(flight);
                    }
                }
                tvTotalFlights.setText("Total Flights: " + flightsList.size());
                filterFlights(etSearchFlights.getText().toString());
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(FlightModel flight) {
        if (flight.getFlightId() == null || flight.getFlightId().isEmpty()) {
            Toast.makeText(getContext(), "Flight ID missing, cannot delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String flightInfo = (flight.getAirline() != null ? flight.getAirline() : "Flight") +
                " " + (flight.getFlightNumber() != null ? flight.getFlightNumber() : "");

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Flight")
                .setMessage("Are you sure you want to delete " + flightInfo + "?")
                .setPositiveButton("Delete", (dialog, which) ->
                        flightsRef.child(flight.getFlightId()).removeValue()
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(getContext(), "Flight deleted", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                )
                )
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // ✅ Show Status Update Dialog
    private void showStatusDialog(FlightModel flight) {
        String[] statuses = {"Scheduled", "Confirmed", "Departed", "Cancelled"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Status for " + flight.getFlightNumber())
                .setItems(statuses, (dialog, which) -> {
                    String newStatus = statuses[which];
                    flightsRef.child(flight.getFlightId()).child("status").setValue(newStatus)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Status updated to " + newStatus, Toast.LENGTH_SHORT).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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