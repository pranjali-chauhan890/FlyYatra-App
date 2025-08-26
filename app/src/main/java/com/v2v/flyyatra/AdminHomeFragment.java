package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminHomeFragment extends Fragment {

    private LinearLayout cardAddFlight, cardManageFlights;
    private TextView tvTotalBookings;
    private EditText etSearchBooking;
    private ImageButton btnSearchBooking;
    private RecyclerView rvBookings;

    private ArrayList<FlightModel> bookingList;
    private BookingAdapter bookingAdapter;

    private DatabaseReference bookingsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        cardAddFlight = view.findViewById(R.id.cardAddFlight);
        cardManageFlights = view.findViewById(R.id.cardManageFlights);
        tvTotalBookings = view.findViewById(R.id.tvTotalBookings);
        etSearchBooking = view.findViewById(R.id.etSearchBooking);
        btnSearchBooking = view.findViewById(R.id.btnSearchBooking);
        rvBookings = view.findViewById(R.id.rvBookings);

        cardAddFlight.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddFlightActivity.class)));

        cardManageFlights.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), ManageFlightsActivity.class)));

        rvBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(getContext(), bookingList); // ✅ BookingAdapter now uses FlightModel
        rvBookings.setAdapter(bookingAdapter);

        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings");

        // load all bookings on start
        loadBookings();

        // handle search
        btnSearchBooking.setOnClickListener(v -> searchBooking());

        return view;
    }

    private void loadBookings() {
        bookingsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();

                for (DataSnapshot userNode : snapshot.getChildren()) { // ✅ userId node
                    for (DataSnapshot bookingNode : userNode.getChildren()) { // ✅ bookingId node
                        FlightModel flight = bookingNode.getValue(FlightModel.class);
                        if (flight != null) {
                            bookingList.add(flight);
                        }
                    }
                }

                tvTotalBookings.setText(String.valueOf(bookingList.size()));
                bookingAdapter.updateList(bookingList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void searchBooking() {
        String query = etSearchBooking.getText().toString().trim();
        if (TextUtils.isEmpty(query)) {
            // reset to all bookings
            bookingAdapter.updateList(bookingList);
            return;
        }

        ArrayList<FlightModel> filtered = new ArrayList<>();
        for (FlightModel f : bookingList) {
            if ((f.getBookingId() != null && f.getBookingId().toLowerCase().contains(query.toLowerCase())) ||
                    (f.getAirline() != null && f.getAirline().toLowerCase().contains(query.toLowerCase())) ||
                    (f.getFlightNumber() != null && f.getFlightNumber().toLowerCase().contains(query.toLowerCase())) ||
                    (f.getDeparture() != null && f.getDeparture().toLowerCase().contains(query.toLowerCase())) ||
                    (f.getArrival() != null && f.getArrival().toLowerCase().contains(query.toLowerCase()))) {
                filtered.add(f);
            }
        }

        bookingAdapter.updateList(filtered);
    }
}