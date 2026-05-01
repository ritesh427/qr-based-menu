package com.restaurant.ordering.repository;

import com.restaurant.ordering.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    long countByMenuItemId(Long menuItemId);
}
