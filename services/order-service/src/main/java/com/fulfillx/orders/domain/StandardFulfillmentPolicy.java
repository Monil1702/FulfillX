package com.fulfillx.orders.domain;

public final class StandardFulfillmentPolicy implements FulfillmentPolicy {
    @Override
    public OrderStatus initialStatus() {
        return OrderStatus.PENDING_INVENTORY;
    }

    @Override
    public String name() {
        return "STANDARD";
    }
}

