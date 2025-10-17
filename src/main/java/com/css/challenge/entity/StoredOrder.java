package com.css.challenge.entity;

import java.time.Duration;
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

    public double calculateFreshnessMetric(Instant now)
    {
        double originalFreshness = order.getFreshness();
        double timePassed = Duration.between(timePlaced, now).toMillis();

        boolean atCorrectTemp = isTemperatureInCorrectRange();

        // variable to fulfill requirement that orders degrade twice as quickly when not stored at the right temp
        double tempBasedFreshness = atCorrectTemp ? originalFreshness : originalFreshness/2.0;
        return Math.max(0, (tempBasedFreshness - timePassed)/originalFreshness);
    }

    public boolean isTemperatureInCorrectRange()
    {
        String curTemp = order.getTemp();

        if ("hot".equals(curTemp) && Action.HEATER.equals(currentLocation)) return true;
        if ("cold".equals(curTemp) && Action.COOLER.equals(currentLocation)) return true;
        if ("room".equals(curTemp) && Action.SHELF.equals(currentLocation)) return true;
        return false;

    }

    public boolean isFresh(Instant now)
    {
        double timePassed = Duration.between(timePlaced, now).toMillis();
        double currentFreshness = order.getFreshness();

        if (!isTemperatureInCorrectRange())
        {
            currentFreshness /= 2.0;
        }

        return timePassed < currentFreshness;
    }

    public String getProperStorageLocation()
    {
        switch (order.getTemp()) {
            case "hot": return Action.HEATER;
            case "cold": return Action.COOLER;
            default: return Action.SHELF;
        }
    }

}
