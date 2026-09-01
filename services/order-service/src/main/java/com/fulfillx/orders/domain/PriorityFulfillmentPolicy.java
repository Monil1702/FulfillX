package com.fulfillx.orders.domain;

public final class PriorityFulfillmentPolicy implements FulfillmentPolicy {
    @Override
    public OrderStatus initialStatus() {
        return OrderStatus.PRIORITY_PENDING;
    }

    @Override
    public String name() {
        return "PRIORITY";
    }
}

