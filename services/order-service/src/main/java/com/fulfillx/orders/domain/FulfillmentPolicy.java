package com.fulfillx.orders.domain;

public interface FulfillmentPolicy {
    OrderStatus initialStatus();
    String name();
}

