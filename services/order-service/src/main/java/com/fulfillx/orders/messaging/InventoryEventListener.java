package com.fulfillx.orders.messaging;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fulfillx.orders.domain.Order;
import com.fulfillx.orders.domain.OrderStatus;
import com.fulfillx.orders.persistence.OrderRepository;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryEventListener {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private final ObjectMapper objectMapper;
    private final OrderRepository orders;
    private final RabbitTemplate rabbit;
    private final String notificationExchange;

    public InventoryEventListener(
            ObjectMapper objectMapper,
            OrderRepository orders,
            RabbitTemplate rabbit,
            @Value("${fulfillx.notifications.exchange}") String notificationExchange) {
        this.objectMapper = objectMapper;
        this.orders = orders;
        this.rabbit = rabbit;
        this.notificationExchange = notificationExchange;
    }

    @KafkaListener(topics = "${fulfillx.topics.inventory-events}")
    @Transactional
    public void handle(String payload) throws Exception {
        Map<String, Object> event = objectMapper.readValue(payload, new TypeReference<>() {});
        UUID orderId = UUID.fromString((String) event.get("orderId"));
        Order order = orders.findById(orderId).orElseThrow();
        String eventType = (String) event.get("eventType");
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.REJECTED) {
            log.info("Ignoring duplicate inventory outcome for finalized order {}", orderId);
            return;
        }

        if ("inventory.reserved".equals(eventType)) {
            order.confirm();
            notify(order, "confirmed", "Inventory reserved. Order confirmed.");
        } else if ("inventory.rejected".equals(eventType)) {
            order.reject();
            notify(order, "rejected", "Insufficient inventory. Order rejected.");
        } else {
            log.warn("Ignoring unsupported inventory event type {}", eventType);
        }
    }

    private void notify(Order order, String outcome, String message) {
        Map<String, Object> command = Map.of(
                "id", UUID.randomUUID().toString(),
                "orderId", order.getId().toString(),
                "customerEmail", order.getCustomerEmail(),
                "outcome", outcome,
                "message", message
        );
        rabbit.convertAndSend(notificationExchange, "notification.order." + outcome, command);
    }
}
