package com.css.challenge.logging;

import com.css.challenge.entity.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ActionLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionLogger.class);

    ConcurrentLinkedDeque<Action> actions = new ConcurrentLinkedDeque<>();

    public void logAction(Instant time, String orderId, String action, String storageTarget)
    {
        Action actionFormatted = new Action(time, orderId, action, storageTarget);

        actions.add(actionFormatted);

        LOGGER.info(String.format(
                "Action: %s, OrderId: %s, StorageTarget: %s",
                action.toUpperCase(), orderId, storageTarget));
    }

    public List<Action> getAllActions()
    {
        return new ArrayList<>(actions);
    }

    public void deleteAllActions()
    {
        actions.clear();
    }
}
