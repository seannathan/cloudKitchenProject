package com.css.challenge.entity;

import java.time.Instant;

public class StoredOrder {

    private final Order order;
    private final Instant timePlaced;
    private String currentLocation;

    public StoredOrder(Order order, Instant timePlaced)
    {
        this.order = order;
        this.timePlaced = timePlaced;
    }

    public Order getOrder()
    {
        return this.order;
    }

    public Instant getTimePlaced()
    {
        return this.timePlaced;
    }

    public String getCurrentLocation()
    {
        return this.currentLocation;
    }

    public void setCurrentLocation(String currentLocation)
    {
        this.currentLocation = currentLocation;
    }

    public String getOptimalStorageLocation()
    {
        switch (order.getTemp())
        {
            case "hot": return Action.HEATER;
            case "cold": return Action.COOLER;
            default: return Action.SHELF;
        }
    }

}
