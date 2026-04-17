package com.restaurant.ordering.repository;

import java.util.List;

import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<CustomerOrder> findByTableQrCodeTokenOrderByCreatedAtDesc(String qrCodeToken);
    long countByRestaurantIdAndStatus(Long restaurantId, OrderStatus status);
}
