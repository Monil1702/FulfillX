package com.fulfillx.inventory.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private String sku;
    private String name;
    private int available;
    private int reserved;

    @Version
    private long version;

    protected InventoryItem() {
    }

    public InventoryItem(String sku, String name, int available) {
        if (available < 0) {
            throw new IllegalArgumentException("Available stock cannot be negative");
        }
        this.sku = sku;
        this.name = name;
        this.available = available;
    }

    public boolean reserve(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Reservation quantity must be positive");
        }
        if (available < quantity) {
            return false;
        }
        available -= quantity;
        reserved += quantity;
        return true;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public int getAvailable() { return available; }
    public int getReserved() { return reserved; }
    public long getVersion() { return version; }
}

