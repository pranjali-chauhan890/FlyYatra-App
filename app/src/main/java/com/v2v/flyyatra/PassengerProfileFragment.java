package com.v2v.flyyatra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class PassengerProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail;
    private RecyclerView rvMyBookings;
    private TripsAdapter tripsAdapter;
    private List<FlightModel> bookingList;

    private DatabaseReference userRef, bookingsRef;
    private String userId;

    private Button btnLogout; // ✅ Added logout button reference

    public PassengerProfileFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_passenger_profile, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        rvMyBookings = view.findViewById(R.id.rvMyBookings);
        btnLogout = view.findViewById(R.id.btnLogout); // ✅ Initialize logout button

        rvMyBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingList = new ArrayList<>();
        tripsAdapter = new TripsAdapter(getContext(), bookingList);
        rvMyBookings.setAdapter(tripsAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings").child(userId);

        loadUserDetails();
        loadMyBookings();

        // ✅ Handle Logout Click
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void loadUserDetails() {
        // ✅ Step 1: Show FirebaseAuth values instantly
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String authName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            String authEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            tvUserName.setText(authName != null && !authName.isEmpty() ? authName : "Passenger");
            tvUserEmail.setText(authEmail != null && !authEmail.isEmpty() ? authEmail : "Email not available");
        } else {
            tvUserName.setText("Passenger");
            tvUserEmail.setText("Email not available");
        }

        // ✅ Step 2: Update with Realtime Database values (if available)
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                tvUserName.setText(name != null && !name.isEmpty() ? name : "Passenger");
                tvUserEmail.setText(email != null && !email.isEmpty() ? email : "Email not available");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void loadMyBookings() {
        bookingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FlightModel booking = ds.getValue(FlightModel.class);
                    if (booking != null) {
                        // ✅ set bookingId for Firebase updates
                        booking.setBookingId(ds.getKey());
                        bookingList.add(booking);
                    }
                }
                tripsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
