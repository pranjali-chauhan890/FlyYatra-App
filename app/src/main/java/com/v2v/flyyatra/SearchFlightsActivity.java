package com.v2v.flyyatra;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFlightsActivity extends AppCompatActivity {

    private EditText etFromCity, etToCity, etSearchDate;
    private Button btnSearchFlights;
    private RecyclerView recyclerFlights;
    private FlightsAdapter adapter;
    private List<FlightModel> flightList;
    private DatabaseReference flightsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flights);

        etFromCity = findViewById(R.id.etFromCity);
        etToCity = findViewById(R.id.etToCity);
        etSearchDate = findViewById(R.id.etSearchDate);
        btnSearchFlights = findViewById(R.id.btnSearchFlights);
        recyclerFlights = findViewById(R.id.recyclerFlights);

        recyclerFlights.setLayoutManager(new LinearLayoutManager(this));
        flightList = new ArrayList<>();
        adapter = new FlightsAdapter(this, flightList); // ✅ Use context-based constructor
        recyclerFlights.setAdapter(adapter);

        flightsRef = FirebaseDatabase.getInstance().getReference("flights");

        btnSearchFlights.setOnClickListener(v -> {
            String from = etFromCity.getText().toString().trim();
            String to = etToCity.getText().toString().trim();
            String date = etSearchDate.getText().toString().trim();

            if (from.isEmpty() || to.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            flightsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    flightList.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        FlightModel flight = ds.getValue(FlightModel.class);
                        if (flight != null &&
                                flight.getDeparture().equalsIgnoreCase(from) &&
                                flight.getArrival().equalsIgnoreCase(to) &&
                                flight.getDate().equals(date)) {
                            flightList.add(flight);
                        }
                    }

                    if (flightList.isEmpty()) {
                        Toast.makeText(SearchFlightsActivity.this, "No flights found", Toast.LENGTH_SHORT).show();
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SearchFlightsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}