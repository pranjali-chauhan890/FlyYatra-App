package com.v2v.flyyatra;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PassengerFlightAdapter extends RecyclerView.Adapter<PassengerFlightAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<FlightModel> flightList;

    public PassengerFlightAdapter(Context context, ArrayList<FlightModel> flightList) {
        this.context = context;
        this.flightList = flightList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_passengers_flight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FlightModel flight = flightList.get(position);

        if (flight != null) {
            holder.tvAirline.setText(flight.getAirline() != null ? flight.getAirline() : "Unknown Airline");
            holder.tvFlightNumber.setText("Flight No: " + (flight.getFlightNumber() != null ? flight.getFlightNumber() : ""));
            holder.tvRoute.setText((flight.getDeparture() != null ? flight.getDeparture() : "")
                    + " → " + (flight.getArrival() != null ? flight.getArrival() : ""));
            holder.tvDate.setText("Date: " + (flight.getDate() != null ? flight.getDate() : ""));
            holder.tvTime.setText("Time: " + (flight.getTime() != null ? flight.getTime() : ""));
            holder.tvPrice.setText("₹" + flight.getPrice());

            // ✅ Flight status with colored badge
            String status = flight.getStatus() != null ? flight.getStatus() : "Scheduled";
            holder.tvStatus.setText(status);

            // Badge background with rounded corners
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(30); // rounded pill
            bg.setColor(getStatusColor(status));

            holder.tvStatus.setBackground(bg);
            holder.tvStatus.setTextColor(Color.WHITE);
            holder.tvStatus.setPadding(24, 10, 24, 10);
        }
    }

    @Override
    public int getItemCount() {
        return (flightList != null) ? flightList.size() : 0;
    }

    // ✅ Helper method to update the list dynamically
    public void updateFlights(ArrayList<FlightModel> newFlights) {
        flightList.clear();
        flightList.addAll(newFlights);
        notifyDataSetChanged();
    }

    // ✅ Status colors
    private int getStatusColor(String status) {
        switch (status) {
            case "Confirmed":
                return Color.parseColor("#2E7D32"); // green
            case "Departed":
                return Color.parseColor("#1565C0"); // blue
            case "Cancelled":
                return Color.parseColor("#C62828"); // red
            default:
                return Color.parseColor("#F9A825"); // yellow for Scheduled
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAirline, tvFlightNumber, tvRoute, tvDate, tvTime, tvPrice, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirline = itemView.findViewById(R.id.tvAirline);
            tvFlightNumber = itemView.findViewById(R.id.tvFlightNumber);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}