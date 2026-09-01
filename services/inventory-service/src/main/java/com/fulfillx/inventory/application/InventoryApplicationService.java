package com.fulfillx.inventory.application;

import com.fulfillx.inventory.api.InventoryResponse;
import com.fulfillx.inventory.domain.InventoryItem;
import com.fulfillx.inventory.domain.ProcessedEvent;
import com.fulfillx.inventory.persistence.InventoryRepository;
import com.fulfillx.inventory.persistence.ProcessedEventRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryApplicationService {
    public enum ReservationResult { RESERVED, REJECTED, DUPLICATE }

    private final InventoryRepository inventory;
    private final ProcessedEventRepository processedEvents;

    public InventoryApplicationService(InventoryRepository inventory, ProcessedEventRepository processedEvents) {
        this.inventory = inventory;
        this.processedEvents = processedEvents;
    }

    @Transactional
    public ReservationResult reserve(String eventId, String sku, int quantity) {
        if (processedEvents.existsById(eventId)) {
            return ReservationResult.DUPLICATE;
        }
        InventoryItem item = inventory.findById(sku).orElse(null);
        boolean reserved = item != null && item.reserve(quantity);
        processedEvents.save(new ProcessedEvent(eventId));
        return reserved ? ReservationResult.RESERVED : ReservationResult.REJECTED;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> list() {
        return inventory.findAll().stream()
                .sorted(Comparator.comparing(InventoryItem::getSku))
                .map(InventoryResponse::from)
                .toList();
    }
}

