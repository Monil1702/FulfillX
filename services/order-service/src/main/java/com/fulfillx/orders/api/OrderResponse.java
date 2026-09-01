package com.fulfillx.orders.api;

import com.fulfillx.orders.domain.Order;
import com.fulfillx.orders.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerEmail,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal total,
        OrderStatus status,
        String fulfillmentPolicy,
        Instant createdAt,
        Instant updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(), order.getCustomerEmail(), order.getSku(), order.getQuantity(),
                order.getUnitPrice(), order.total(), order.getStatus(),
                order.getFulfillmentPolicy(), order.getCreatedAt(), order.getUpdatedAt()
        );
    }
}

