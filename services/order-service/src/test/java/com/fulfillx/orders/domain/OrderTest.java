package com.fulfillx.orders.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderTest {
    @Test
    void priorityOrderUsesPriorityStrategyAndCalculatesTotal() {
        Order order = Order.place("buyer@example.com", "SKU-1", 3, new BigDecimal("19.99"),
                new PriorityFulfillmentPolicy());

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PRIORITY_PENDING);
        assertThat(order.getFulfillmentPolicy()).isEqualTo("PRIORITY");
        assertThat(order.total()).isEqualByComparingTo("59.97");
    }

    @Test
    void confirmedOrderCannotTransitionTwice() {
        Order order = Order.place("buyer@example.com", "SKU-1", 1, BigDecimal.TEN,
                new StandardFulfillmentPolicy());
        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThatThrownBy(order::reject).isInstanceOf(IllegalStateException.class);
    }
}

