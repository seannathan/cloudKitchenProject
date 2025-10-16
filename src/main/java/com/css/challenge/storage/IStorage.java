package com.css.challenge.storage;

import com.css.challenge.entity.StoredOrder;

import java.util.Optional;

public interface IStorage {

    boolean store(StoredOrder storedOrder);

    Optional<StoredOrder> findOrder(String orderId);

    Optional<StoredOrder> removeOrder(String orderId);

    int getSize();

    int getCapacity();

    boolean hasCapacity();

    String getLocation();
}
