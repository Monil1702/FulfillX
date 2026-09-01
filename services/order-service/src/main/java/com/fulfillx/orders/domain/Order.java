package com.fulfillx.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String fulfillmentPolicy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Order() {
    }

    private Order(UUID id, String customerEmail, String sku, int quantity,
                  BigDecimal unitPrice, FulfillmentPolicy policy) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }
        this.id = id;
        this.customerEmail = customerEmail;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = policy.initialStatus();
        this.fulfillmentPolicy = policy.name();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Order place(String customerEmail, String sku, int quantity,
                              BigDecimal unitPrice, FulfillmentPolicy policy) {
        return new Order(UUID.randomUUID(), customerEmail, sku, quantity, unitPrice, policy);
    }

    public void confirm() {
        requirePending();
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void reject() {
        requirePending();
        this.status = OrderStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    private void requirePending() {
        if (status != OrderStatus.PENDING_INVENTORY && status != OrderStatus.PRIORITY_PENDING) {
            throw new IllegalStateException("Only a pending order can transition");
        }
    }

    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public OrderStatus getStatus() { return status; }
    public String getFulfillmentPolicy() { return fulfillmentPolicy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

