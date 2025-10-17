package com.css.challenge.businessLogic.placement;

import com.css.challenge.logging.ActionLogger;
import com.css.challenge.businessLogic.managers.StorageManager;
import com.css.challenge.businessLogic.storage.IStorage;
import com.css.challenge.businessLogic.storage.ShelfStorage;
import com.css.challenge.entity.Action;
import com.css.challenge.entity.StoredOrder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class DefaultPlacement implements IPlacement
{
    private final StorageManager storageManager;
    private final ActionLogger actionLogger;

    public DefaultPlacement(StorageManager storageManager, ActionLogger actionLogger)
    {
        this.storageManager = storageManager;
        this.actionLogger = actionLogger;
    }

    @Override
    public PlacementStatus storeOrder(StoredOrder order)
    {
        Instant now = Instant.now();
        String correctLocation = order.getOptimalStorageLocation();

        // first we try placing order in correct storage (hot/cold)
        if (attemptPlacementInOptimalLocation(order, correctLocation, now))
        {
            return PlacementStatus.success(correctLocation);
        }

        // if storage not available, we attempt to place order on shelf
        if (attemptPlacementOnShelf(order, now))
        {
            return PlacementStatus.success(Action.SHELF);
        }

        // if shelf space not available, attempt to remove expired orders
        // to make room, and if room is made, then place order
        if (attemptMoveToTempStorageFromShelf(now))
        {
            if (attemptPlacementOnShelf(order, now))
            {
                return PlacementStatus.success(Action.SHELF);
            }

        }

        // if none of these work, we need to remove from the shelf and place the new order afterwards
        return removeFromShelfAndPlace(order, now);
    }

    private boolean attemptPlacementInOptimalLocation(StoredOrder order, String location, Instant now)
    {
        Optional<IStorage> storage = storageManager.getStorageByLocation(location);

        if (storage.isPresent() && storage.get().store(order))
        {
            actionLogger.logAction(now, order.getOrder().getId(), Action.PLACE, location);
            return true;
        }
        return false;
    }

    private boolean attemptPlacementOnShelf(StoredOrder order, Instant now)
    {
        ShelfStorage shelf = storageManager.getShelf();
        if (shelf.store(order))
        {
            actionLogger.logAction(now, order.getOrder().getId(), Action.PLACE, Action.SHELF);
            return true;
        }
        return false;
    }

    private boolean attemptMoveToTempStorageFromShelf(Instant now)
    {
        ShelfStorage shelf = storageManager.getShelf();
        List<StoredOrder> movableStoredOrders = shelf.findImproperlyStoredOrders();

        for (StoredOrder order : movableStoredOrders)
        {
            String correctLocation = order.getOptimalStorageLocation();
            Optional<IStorage> correctContainer = storageManager.getStorageByLocation(correctLocation);

            if (correctContainer.isPresent() && correctContainer.get().hasCapacity())
            {
                String orderId = order.getOrder().getId();
                Optional<StoredOrder> removedOrder = shelf.removeOrder(orderId);

                if (removedOrder.isPresent() && correctContainer.get().store(order))
                {
                    actionLogger.logAction(now, orderId, Action.MOVE, correctLocation);
                    return true;
                }
            }
        }
        return false;
    }

    private PlacementStatus removeFromShelfAndPlace(StoredOrder order, Instant now)
    {
        ShelfStorage shelf = storageManager.getShelf();
        Optional<StoredOrder> removedOrder = shelf.removeLeastFreshOrder();

        if (removedOrder.isPresent())
        {
            actionLogger.logAction(now, removedOrder.get().getOrder().getId(),
                    Action.DISCARD, Action.SHELF);
        }

        if (shelf.store(order))
        {
            actionLogger.logAction(now, order.getOrder().getId(), Action.PLACE, Action.SHELF);
            return PlacementStatus.success(Action.SHELF);
        }

        return PlacementStatus.failure("Order was not able to be stored after attempting every possible storage option");
    }
}
