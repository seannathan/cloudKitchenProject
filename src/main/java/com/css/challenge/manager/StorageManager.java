package com.css.challenge.manager;

import com.css.challenge.entity.Action;
import com.css.challenge.entity.StoredOrder;
import com.css.challenge.storage.IStorage;
import com.css.challenge.storage.ShelfStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageManager {
    private final ShelfStorage shelf;
    private final Map<String, IStorage> storageMap;

    public StorageManager(IStorage heater, IStorage cooler, ShelfStorage shelf)
    {
        this.storageMap = new HashMap<>();
        this.shelf = shelf;
        this.storageMap.put(Action.HEATER, heater);
        this.storageMap.put(Action.COOLER, cooler);
        this.storageMap.put(Action.SHELF, shelf);
    }

    public ShelfStorage getShelf()
    {
        return shelf;
    }

    public Map<String, IStorage> getStorageMap()
    {
        return this.storageMap;
    }

    public Optional<IStorage> getStorageByLocation(String location)
    {
        return Optional.ofNullable(storageMap.get(location));
    }

    public Optional<StoredOrder> findOrder(String orderId)
    {
        for (IStorage container : storageMap.values())
        {
            Optional<StoredOrder> orderToRemove = container.removeOrder(orderId);
            if (orderToRemove.isPresent()) return orderToRemove;
        }
        return Optional.empty();
    }

}
