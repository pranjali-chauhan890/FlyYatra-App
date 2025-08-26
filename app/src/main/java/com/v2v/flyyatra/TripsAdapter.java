package com.v2v.flyyatra;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripVH> {

    private Context context;
    private List<FlightModel> list;
    private DatabaseReference bookingsRef;
    private String userId;

    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public TripsAdapter(Context context, List<FlightModel> list) {
        this.context = context;
        this.list = list;
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        bookingsRef = FirebaseDatabase.getInstance().getReference("bookings").child(userId);
    }

    @NonNull
    @Override
    public TripVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TripVH holder, int position) {
        FlightModel trip = list.get(position);

        String airline = trip.getAirline() != null ? trip.getAirline() : context.getString(R.string.placeholder_airline);
        String flightNo = trip.getFlightNumber() != null ? trip.getFlightNumber() : context.getString(R.string.placeholder_flight);
        String dep = trip.getDeparture() != null ? trip.getDeparture() : "?";
        String arr = trip.getArrival() != null ? trip.getArrival() : "?";
        String date = trip.getDate() != null ? trip.getDate() : "?";
        String time = trip.getTime() != null ? trip.getTime() : "";

        holder.tvAirline.setText(airline);
        holder.tvFlightInfo.setText(context.getString(R.string.flight_info_format, flightNo, dep, arr));
        holder.tvDate.setText(context.getString(R.string.date_time_format, date, time));

        // ✅ Safe price handling
        double price = 0.0;
        try {
            price = trip.getPrice(); // works fine if primitive double
        } catch (Exception ignored) {}
        holder.tvPrice.setText(context.getString(R.string.price_money, price));

        // ✅ Ensure status
        String status = (trip.getStatus() != null) ? trip.getStatus() : "Confirmed";

        // ✅ Auto-mark Departed if in past
        try {
            if (!"?".equals(date) && !time.isEmpty()) {
                Date tripDateTime = sdf.parse(date + " " + time);
                if (tripDateTime != null && tripDateTime.before(new Date()) && !"Cancelled".equals(status)) {
                    status = "Departed";
                    trip.setStatus("Departed");

                    if (trip.getBookingId() != null) {
                        bookingsRef.child(trip.getBookingId()).child("status").setValue("Departed");
                    }
                }
            }
        } catch (ParseException ignored) {}

        holder.tvStatus.setText(status);

        // ✅ Apply background badge dynamically
        if ("Confirmed".equals(status)) {
            holder.tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_badge_confirmed));
        } else if ("Departed".equals(status)) {
            holder.tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_badge_departed));
        } else if ("Cancelled".equals(status)) {
            holder.tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_badge_cancelled));
        } else {
            holder.tvStatus.setBackground(null);
        }

        // ✅ View Details
        String finalStatus = status;
        double finalPrice = price;
        holder.btnDetails.setOnClickListener(v -> {
            String message = context.getString(
                    R.string.trip_details_message,
                    airline,
                    flightNo,
                    dep,
                    arr,
                    date,
                    time,
                    finalPrice,
                    finalStatus
            );

            new AlertDialog.Builder(context)
                    .setTitle(R.string.trip_details_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        });

        // ✅ Cancel button state
        boolean canCancel = !"Cancelled".equals(status) && !"Departed".equals(status);
        holder.btnCancel.setEnabled(canCancel);
        holder.btnCancel.setAlpha(canCancel ? 1f : 0.5f);

        holder.btnCancel.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.cancel_booking_title)
                    .setMessage(R.string.cancel_booking_message)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        if (trip.getBookingId() != null) {
                            bookingsRef.child(trip.getBookingId()).child("status").setValue("Cancelled")
                                    .addOnSuccessListener(aVoid -> {
                                        trip.setStatus("Cancelled");
                                        notifyItemChanged(position);
                                        Toast.makeText(context, R.string.booking_cancelled_toast, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, context.getString(R.string.action_failed_toast, e.getMessage()), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        // ✅ Long press delete
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.delete_booking_title)
                    .setMessage(R.string.delete_booking_message)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        if (trip.getBookingId() != null) {
                            bookingsRef.child(trip.getBookingId()).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        int pos = holder.getAdapterPosition();
                                        if (pos != RecyclerView.NO_POSITION) {
                                            list.remove(pos);
                                            notifyItemRemoved(pos);
                                        }
                                        Toast.makeText(context, R.string.booking_deleted_toast, Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, context.getString(R.string.action_failed_toast, e.getMessage()), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TripVH extends RecyclerView.ViewHolder {
        TextView tvAirline, tvFlightInfo, tvDate, tvStatus, tvPrice;
        Button btnDetails, btnCancel;

        public TripVH(@NonNull View itemView) {
            super(itemView);
            tvAirline = itemView.findViewById(R.id.tvAirline);
            tvFlightInfo = itemView.findViewById(R.id.tvFlightInfo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnDetails = itemView.findViewById(R.id.btnDetails);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}