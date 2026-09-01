package com.fulfillx.inventory.api;

import com.fulfillx.inventory.domain.InventoryItem;

public record InventoryResponse(String sku, String name, int available, int reserved) {
    public static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(item.getSku(), item.getName(), item.getAvailable(), item.getReserved());
    }
}

