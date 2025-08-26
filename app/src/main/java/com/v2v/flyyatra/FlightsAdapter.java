package com.v2v.flyyatra;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FlightsAdapter extends RecyclerView.Adapter<FlightsAdapter.FlightVH> {

    public interface OnItemAction {
        void onEdit(FlightModel flight);
        void onDelete(FlightModel flight);
        void onChangeStatus(FlightModel flight); // ✅ NEW
    }

    private Context context;
    private final List<FlightModel> list;
    private OnItemAction action;

    public FlightsAdapter(Context context, List<FlightModel> list) {
        this.context = context;
        this.list = list;
    }

    public void setOnItemAction(OnItemAction action) {
        this.action = action;
    }

    @NonNull
    @Override
    public FlightVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (context == null) {
            context = parent.getContext();
        }
        View v = LayoutInflater.from(context).inflate(R.layout.item_flight, parent, false);
        return new FlightVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightVH holder, int position) {
        FlightModel f = list.get(position);

        // Airline name & flight number
        holder.tvAirlineName.setText(f.getAirline() != null ? f.getAirline() : "");
        holder.tvFlightNumber.setText(f.getFlightNumber() != null ? f.getFlightNumber() : "");

        // Departure & arrival cities
        holder.tvDepartureCity.setText(f.getDeparture() != null ? f.getDeparture() : "");
        holder.tvArrivalCity.setText(f.getArrival() != null ? f.getArrival() : "");

        // Date & time
        String dateTime = (f.getDate() != null) ? f.getDate() : "";
        if (f.getTime() != null && !f.getTime().isEmpty()) {
            dateTime += " • " + f.getTime();
        }
        holder.tvDateTime.setText(dateTime);

        // ✅ Price
        holder.tvPrice.setText("₹" + f.getPrice());

        // ✅ Status with colors
        String status = f.getStatus() != null ? f.getStatus() : "Scheduled";
        holder.tvStatus.setText(status);

        if ("Confirmed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Departed".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        } else {
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // EDIT click
        holder.btnEdit.setOnClickListener(v -> {
            if (action != null) action.onEdit(f);
            else {
                Intent intent = new Intent(context, AddFlightActivity.class);
                intent.putExtra("flight", f);
                context.startActivity(intent);
            }
        });

        // DELETE click
        holder.btnDelete.setOnClickListener(v -> {
            if (action != null) action.onDelete(f);
        });

        // ✅ STATUS click
        holder.btnStatus.setOnClickListener(v -> {
            if (action != null) action.onChangeStatus(f);
        });

        // CARD click (quick edit)
        holder.itemView.setOnClickListener(v -> {
            if (action != null) action.onEdit(f);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class FlightVH extends RecyclerView.ViewHolder {
        TextView tvAirlineName, tvFlightNumber, tvDepartureCity, tvArrivalCity, tvDateTime, tvPrice, tvStatus;
        ImageButton btnEdit, btnDelete, btnStatus; // ✅ new
        ImageView imgAirlineLogo;

        public FlightVH(@NonNull View itemView) {
            super(itemView);
            tvAirlineName = itemView.findViewById(R.id.tvAirlineName);
            tvFlightNumber = itemView.findViewById(R.id.tvFlightNumber);
            tvDepartureCity = itemView.findViewById(R.id.tvDepartureCity);
            tvArrivalCity = itemView.findViewById(R.id.tvArrivalCity);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnStatus = itemView.findViewById(R.id.btnStatus); // ✅ bind
            imgAirlineLogo = itemView.findViewById(R.id.imgAirlineLogo);
        }
    }
}