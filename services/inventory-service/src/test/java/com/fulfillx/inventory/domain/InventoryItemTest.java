package com.fulfillx.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InventoryItemTest {
    @Test
    void reservationUpdatesAvailableAndReservedCounts() {
        InventoryItem item = new InventoryItem("SKU-1", "Test item", 10);

        assertThat(item.reserve(4)).isTrue();
        assertThat(item.getAvailable()).isEqualTo(6);
        assertThat(item.getReserved()).isEqualTo(4);
    }

    @Test
    void reservationRejectsWhenStockIsInsufficient() {
        InventoryItem item = new InventoryItem("SKU-1", "Test item", 2);

        assertThat(item.reserve(3)).isFalse();
        assertThat(item.getAvailable()).isEqualTo(2);
        assertThat(item.getReserved()).isZero();
    }
}

