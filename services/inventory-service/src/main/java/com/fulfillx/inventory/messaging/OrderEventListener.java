package com.fulfillx.inventory.messaging;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.fulfillx.inventory.application.InventoryApplicationService;
import com.fulfillx.inventory.application.InventoryApplicationService.ReservationResult;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final ObjectMapper objectMapper;
    private final InventoryApplicationService inventory;
    private final KafkaTemplate<String, String> kafka;
    private final String inventoryEventsTopic;

    public OrderEventListener(
            ObjectMapper objectMapper,
            InventoryApplicationService inventory,
            KafkaTemplate<String, String> kafka,
            @Value("${fulfillx.topics.inventory-events}") String inventoryEventsTopic) {
        this.objectMapper = objectMapper;
        this.inventory = inventory;
        this.kafka = kafka;
        this.inventoryEventsTopic = inventoryEventsTopic;
    }

    @KafkaListener(topics = "${fulfillx.topics.order-events}")
    public void handle(String payload) throws Exception {
        Map<String, Object> orderEvent = objectMapper.readValue(payload, new TypeReference<>() {});
        if (!"order.created".equals(orderEvent.get("eventType"))) {
            return;
        }

        String eventId = (String) orderEvent.get("eventId");
        String orderId = (String) orderEvent.get("orderId");
        String sku = (String) orderEvent.get("sku");
        int quantity = ((Number) orderEvent.get("quantity")).intValue();
        ReservationResult result = inventory.reserve(eventId, sku, quantity);
        if (result == ReservationResult.DUPLICATE) {
            log.info("Ignoring duplicate order event {}", eventId);
            return;
        }

        Map<String, Object> inventoryEvent = new LinkedHashMap<>();
        inventoryEvent.put("eventId", UUID.randomUUID().toString());
        inventoryEvent.put("eventType", result == ReservationResult.RESERVED
                ? "inventory.reserved" : "inventory.rejected");
        inventoryEvent.put("orderId", orderId);
        inventoryEvent.put("sku", sku);
        inventoryEvent.put("quantity", quantity);
        inventoryEvent.put("occurredAt", Instant.now().toString());
        kafka.send(inventoryEventsTopic, orderId, objectMapper.writeValueAsString(inventoryEvent));
    }
}
