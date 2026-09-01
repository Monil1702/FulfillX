package com.fulfillx.orders.persistence;

import com.fulfillx.orders.domain.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}

