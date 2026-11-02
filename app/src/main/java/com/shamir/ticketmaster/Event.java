package com.shamir.ticketmaster;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private int availableTickets;
    private List<String> buyers;

    // Required empty constructor for Firestore deserialization
    public Event() {
        this.buyers = new ArrayList<>();
    }

    public Event(int availableTickets) {
        this.availableTickets = availableTickets;
        this.buyers = new ArrayList<>();
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public List<String> getBuyers() {
        return buyers;
    }

    public void setBuyers(List<String> buyers) {
        this.buyers = buyers;
    }
}