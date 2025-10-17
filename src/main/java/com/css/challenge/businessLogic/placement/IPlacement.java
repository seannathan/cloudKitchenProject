package com.css.challenge.businessLogic.placement;

import com.css.challenge.entity.StoredOrder;

public interface IPlacement {

    PlacementStatus storeOrder(StoredOrder order);

    class PlacementStatus
    {
        private final boolean isSuccess;
        private final String location;
        private final String message;

        public PlacementStatus(boolean isSuccess, String location, String message)
        {
            this.isSuccess = isSuccess;
            this.location = location;
            this.message = message;
        }

        public static PlacementStatus success(String location) {
            return new PlacementStatus(true, location, "Order placed successfully");
        }

        public static PlacementStatus failure(String message) {
            return new PlacementStatus(false, null, message);
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }
    }
}
