package com.v2v.flyyatra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PastTripsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvNoTrips;
    private TripsAdapter adapter;
    private List<FlightModel> tripList;
    private DatabaseReference bookingsRef;
    private String userId;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PastTripsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_past_trips, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPast);
        tvNoTrips = view.findViewById(R.id.tvNoPast);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tripList = new ArrayList<>();
        adapter = new TripsAdapter(getContext(), tripList);
        recyclerView.setAdapter(adapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings").child(userId);

        loadPastTrips();

        return view;
    }

    private void loadPastTrips() {
        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tripList.clear();
                Date today = new Date();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    FlightModel trip = ds.getValue(FlightModel.class);

                    if (trip != null && trip.getDate() != null) {
                        trip.setBookingId(ds.getKey());

                        try {
                            Date tripDate = sdf.parse(trip.getDate());
                            if (tripDate != null && tripDate.before(sdf.parse(sdf.format(today)))) {
                                // mark as Departed if not Cancelled
                                if (trip.getStatus() == null || trip.getStatus().equals("Confirmed")) {
                                    trip.setStatus("Departed");
                                }
                                tripList.add(trip);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Sort past trips (latest first)
                Collections.sort(tripList, (a, b) -> {
                    try {
                        return sdf.parse(b.getDate()).compareTo(sdf.parse(a.getDate()));
                    } catch (Exception e) {
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
                tvNoTrips.setVisibility(tripList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
