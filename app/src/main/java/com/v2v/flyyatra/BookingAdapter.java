package com.v2v.flyyatra;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingVH> {

    private Context context;
    private ArrayList<FlightModel> list; // ✅ now uses FlightModel

    public BookingAdapter(Context context, ArrayList<FlightModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public BookingVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingVH holder, int position) {
        FlightModel flight = list.get(position);

        holder.tvBookingId.setText("Booking: " + (flight.getBookingId() != null ? flight.getBookingId() : "N/A"));
        holder.tvPassenger.setText("Passenger: " + (flight.getFlightId() != null ? flight.getFlightId() : "N/A"));
        // ⚠ Replace flightId with passengerName if you add it to FlightModel later

        holder.tvFlight.setText(
                (flight.getAirline() != null ? flight.getAirline() : "Unknown") +
                        " • " + (flight.getFlightNumber() != null ? flight.getFlightNumber() : "-")
        );

        holder.tvRoute.setText(
                (flight.getDeparture() != null ? flight.getDeparture() : "-") +
                        " → " + (flight.getArrival() != null ? flight.getArrival() : "-")
        );

        holder.tvDate.setText(
                (flight.getDate() != null ? flight.getDate() : "-") +
                        " " + (flight.getTime() != null ? flight.getTime() : "-")
        );

        holder.tvPrice.setText("₹" + flight.getPrice());

        String status = (flight.getStatus() != null) ? flight.getStatus() : "Confirmed";
        holder.tvStatus.setText(status);

        switch (status) {
            case "Confirmed":
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // green
                break;
            case "Departed":
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3")); // blue
                break;
            case "Cancelled":
                holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // red
                break;
            default:
                holder.tvStatus.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // 🔄 refresh list
    public void updateList(ArrayList<FlightModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public static class BookingVH extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvPassenger, tvFlight, tvRoute, tvDate, tvPrice, tvStatus;

        public BookingVH(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvPassenger = itemView.findViewById(R.id.tvPassenger);
            tvFlight = itemView.findViewById(R.id.tvFlight);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}