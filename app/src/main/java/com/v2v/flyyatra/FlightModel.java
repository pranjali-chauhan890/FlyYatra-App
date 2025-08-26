package com.v2v.flyyatra;

import java.io.Serializable;

public class FlightModel implements Serializable {

    private String flightId;   // Firebase key for flight
    private String bookingId;  // Firebase key for passenger's booking
    private String airline;
    private String flightNumber;
    private String departure;   // From city
    private String arrival;     // To city
    private String date;
    private String time;
    private String status;      // Confirmed / Cancelled / Departed
    private double price;       // ✅ Added price field

    public FlightModel() { }

    public FlightModel(String flightId, String bookingId, String airline,
                       String flightNumber, String departure, String arrival,
                       String date, String time, String status, double price) {
        this.flightId = flightId;
        this.bookingId = bookingId;
        this.airline = airline;
        this.flightNumber = flightNumber;
        this.departure = departure;
        this.arrival = arrival;
        this.date = date;
        this.time = time;
        this.status = status;
        this.price = price;
    }

    // ✅ Main Getters & Setters
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getDeparture() { return departure; }
    public void setDeparture(String departure) { this.departure = departure; }

    public String getArrival() { return arrival; }
    public void setArrival(String arrival) { this.arrival = arrival; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    // ✅ Aliases for passenger search (optional)
    public String getFrom() { return departure; }
    public void setFrom(String from) { this.departure = from; }

    public String getTo() { return arrival; }
    public void setTo(String to) { this.arrival = to; }

    // ✅ Aliases for backward compatibility with old code
    public String getId() { return flightId; }
    public void setId(String id) { this.flightId = id; }
}