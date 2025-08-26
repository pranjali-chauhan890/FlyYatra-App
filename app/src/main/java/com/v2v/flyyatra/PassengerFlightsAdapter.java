package com.v2v.flyyatra;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PassengerFlightsAdapter extends RecyclerView.Adapter<PassengerFlightsAdapter.FlightVH> {

    private Context context;
    private ArrayList<FlightModel> list;
    private boolean showStatus; // 👈 control if we display status
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // Callback interface
    public interface OnFlightClickListener {
        void onFlightClick(FlightModel flight);
    }

    private OnFlightClickListener clickListener;

    // Pass "showStatus = true" for FlightStatusFragment, else false
    public PassengerFlightsAdapter(Context context, ArrayList<FlightModel> list, boolean showStatus) {
        this.context = context;
        this.list = list;
        this.showStatus = showStatus;
    }

    public void setOnFlightClickListener(OnFlightClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public FlightVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_passenger_flight, parent, false);
        return new FlightVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightVH holder, int position) {
        FlightModel f = list.get(position);

        // Airline & flight number
        String airline = (f.getAirline() != null ? f.getAirline() : "Airline");
        String flightNo = (f.getFlightNumber() != null ? f.getFlightNumber() : "");
        holder.tvAirline.setText(airline + (flightNo.isEmpty() ? "" : " • " + flightNo));

        // Route
        String departure = (f.getDeparture() != null ? f.getDeparture() : "N/A");
        String arrival = (f.getArrival() != null ? f.getArrival() : "N/A");
        holder.tvRoute.setText(departure + " → " + arrival);

        // Date + Time
        String date = (f.getDate() != null ? f.getDate() : "");
        String time = (f.getTime() != null ? f.getTime() : "");
        holder.tvDateTime.setText(date + " " + time);

        // Always same default logo
        holder.imgAirlineLogo.setImageResource(R.drawable.ic_flight);

        // STATUS (only if enabled)
        if (showStatus) {
            holder.tvStatus.setVisibility(View.VISIBLE);

            String status = "On Time";
            int color = context.getResources().getColor(android.R.color.holo_green_dark);

            try {
                Date flightDateTime = sdf.parse(date + " " + time);
                Date now = new Date();

                if (flightDateTime != null) {
                    if (flightDateTime.before(now)) {
                        status = "Departed";
                        color = context.getResources().getColor(android.R.color.holo_blue_dark);
                    } else {
                        long diff = flightDateTime.getTime() - now.getTime();
                        if (diff < 30 * 60 * 1000) { // less than 30 mins
                            status = "Boarding Soon";
                            color = context.getResources().getColor(android.R.color.holo_orange_dark);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvStatus.setText(status);
            holder.tvStatus.setTextColor(color);
        } else {
            holder.tvStatus.setVisibility(View.GONE); // hide in BookFlight/MyTrips
        }

        // 🔹 Click listener on card
        holder.card.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onFlightClick(f);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FlightVH extends RecyclerView.ViewHolder {
        TextView tvAirline, tvRoute, tvDateTime, tvStatus;
        ImageView imgAirlineLogo;
        CardView card;

        public FlightVH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardFlight);
            tvAirline = itemView.findViewById(R.id.tvAirline);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgAirlineLogo = itemView.findViewById(R.id.imgAirlineLogo);
        }
    }
}