package com.css.challenge.storage;

import com.css.challenge.entity.StoredOrder;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShelfStorage implements IStorage {

    private final String location;
    private final int capacity;
    private final AtomicInteger size;
    private final ConcurrentHashMap<String, StoredOrder> storedOrders;
    public ShelfStorage(String location, int capacity)
    {
        this.location = location;
        this.capacity = capacity;
        this.size = new AtomicInteger(0);
        this.storedOrders = new ConcurrentHashMap<>();
    }

    @Override
    public boolean store(StoredOrder storedOrder)
    {
        return false;

    }

    @Override
    public Optional<StoredOrder> findOrder(String orderId)
    {
        return Optional.ofNullable(storedOrders.get(orderId));
    }

    @Override
    public Optional<StoredOrder> removeOrder(String orderId)
    {
        StoredOrder orderToRemove = storedOrders.remove(orderId);

        if (orderToRemove != null)
        {
            size.decrementAndGet();
        }
        return Optional.ofNullable(orderToRemove);
    }

    @Override
    public int getSize()
    {
        return size.get();
    }

    @Override
    public int getCapacity()
    {
        return capacity;
    }

    @Override
    public boolean hasCapacity()
    {
        return capacity > size.get();
    }

    @Override
    public String getLocation()
    {
        return location;
    }
}
