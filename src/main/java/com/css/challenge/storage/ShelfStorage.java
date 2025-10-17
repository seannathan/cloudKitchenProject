package com.css.challenge.storage;

import com.css.challenge.entity.StoredOrder;
import org.apache.log4j.Logger;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ShelfStorage implements IStorage {

    private final String location;
    private final int capacity;
    private final AtomicInteger size;
    private final ConcurrentHashMap<String, StoredOrder> storedOrders;
    private final ConcurrentSkipListSet<ShelfEntry> shelfRemovalQueue;
    public ShelfStorage(String location, int capacity)
    {
        this.location = location;
        this.capacity = capacity;
        this.size = new AtomicInteger(0);
        this.storedOrders = new ConcurrentHashMap<>();
        this.shelfRemovalQueue = new ConcurrentSkipListSet<>(
                Comparator.comparingDouble(ShelfEntry::getFreshness)
                        .thenComparing(ShelfEntry::getOrderId)
        );
    }

    @Override
    public boolean store(StoredOrder storedOrder)
    {
        // thread safe concurrency control with atomic integer compare and set
        int currentSize;
        do {
            currentSize = size.get();
            if (currentSize >= capacity) return false;
        }
        while (!size.compareAndSet(currentSize, currentSize + 1));

        // store order in map
        storedOrder.setCurrentLocation(this.location);
        StoredOrder previousOrder = storedOrders.put(storedOrder.getOrder().getId(), storedOrder);

        //

        return true;

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

            // remove from shelf queue
            ShelfEntry entry = new ShelfEntry(
                    orderId,
                    orderToRemove.calculateFreshnessMetric(Instant.now())
            );
            shelfRemovalQueue.remove(entry);
        }
        return Optional.ofNullable(orderToRemove);
    }

    public Optional<StoredOrder> removeLeastFreshOrder()
    {
        // first remove any orders that have actually expired
        removeAllExpiredOrders();

        while(true)
        {
            ShelfEntry entry = shelfRemovalQueue.pollFirst();

            if (entry == null)
            {
                // check for desync due to potential race condition
                if (!storedOrders.isEmpty())
                {
                    Logger.getRootLogger().warn(
                            String.format("Queue and map are not in sync. Orders exist but queue is empty. Size is: %d",
                                    storedOrders.size()));

                    // num orders should be relatively small since queue is empty so for
                    // accuracy's sake in this condition we can find the least fresh order in map
                    StoredOrder leastFreshOrder = null;
                    double minFreshness = Double.MAX_VALUE;
                    Instant now = Instant.now();

                    for (StoredOrder order : storedOrders.values()) {
                        double freshness = order.calculateFreshnessMetric(now);
                        if (freshness < minFreshness) {
                            minFreshness = freshness;
                            leastFreshOrder = order;
                        }
                    }

                    if (leastFreshOrder != null) {
                        storedOrders.remove(leastFreshOrder.getOrder().getId());
                        size.decrementAndGet();
                        return Optional.of(leastFreshOrder);
                    }

                    return Optional.empty();
                }

            }

            // remove order and decrement size
            StoredOrder orderToRemove = storedOrders.remove(entry.getOrderId());

            if (orderToRemove != null)
            {
                size.decrementAndGet();
                return Optional.of(orderToRemove);
            }
        }
    }

    private void removeAllExpiredOrders()
    {
        Instant now = Instant.now();
        for (Map.Entry<String, StoredOrder> entry : storedOrders.entrySet())
        {
            String id = entry.getKey();
            StoredOrder order = entry.getValue();
            // if order is no longer fresh, remove it
            if (!order.isFresh(now))
            {
                removeOrder(id);
            }
        }
    }

    public List<StoredOrder> findImproperlyStoredOrders()
    {
        return storedOrders.values().stream()
                .filter(order -> !order.isTemperatureInCorrectRange())
                .collect(Collectors.toList());
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

    private record ShelfEntry(String orderId, double freshness) {

        public String getOrderId() {
            return orderId;
        }

        public double getFreshness() {
            return freshness;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ShelfEntry)) return false;
            ShelfEntry that = (ShelfEntry) o;
            return orderId.equals(that.orderId);
        }

        @Override
        public int hashCode() {
            return orderId.hashCode();
        }
    }
}
