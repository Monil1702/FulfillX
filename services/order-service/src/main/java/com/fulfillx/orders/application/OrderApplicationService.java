package com.fulfillx.orders.application;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.fulfillx.orders.api.CreateOrderRequest;
import com.fulfillx.orders.api.OrderResponse;
import com.fulfillx.orders.domain.FulfillmentPolicy;
import com.fulfillx.orders.domain.Order;
import com.fulfillx.orders.domain.OutboxEvent;
import com.fulfillx.orders.domain.PriorityFulfillmentPolicy;
import com.fulfillx.orders.domain.StandardFulfillmentPolicy;
import com.fulfillx.orders.persistence.OrderRepository;
import com.fulfillx.orders.persistence.OutboxEventRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderApplicationService {
    private final OrderRepository orders;
    private final OutboxEventRepository outbox;
    private final ObjectMapper objectMapper;
    private final String orderEventsTopic;

    public OrderApplicationService(
            OrderRepository orders,
            OutboxEventRepository outbox,
            ObjectMapper objectMapper,
            @Value("${fulfillx.topics.order-events}") String orderEventsTopic) {
        this.orders = orders;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
        this.orderEventsTopic = orderEventsTopic;
    }

    @Transactional
    public OrderResponse place(CreateOrderRequest request) {
        FulfillmentPolicy policy = request.priority()
                ? new PriorityFulfillmentPolicy()
                : new StandardFulfillmentPolicy();
        Order order = orders.save(Order.place(
                request.customerEmail(), request.sku().toUpperCase(), request.quantity(),
                request.unitPrice(), policy));

        Map<String, Object> event = Map.of(
                "eventId", UUID.randomUUID().toString(),
                "eventType", "order.created",
                "orderId", order.getId().toString(),
                "customerEmail", order.getCustomerEmail(),
                "sku", order.getSku(),
                "quantity", order.getQuantity(),
                "total", order.total(),
                "priority", request.priority(),
                "occurredAt", Instant.now().toString()
        );
        outbox.save(new OutboxEvent(order.getId().toString(), orderEventsTopic, toJson(event)));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list() {
        return orders.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .map(OrderResponse::from)
                .toList();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize order event", exception);
        }
    }
}
