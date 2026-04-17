package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.restaurant.ordering.enums.OrderStatus;

public record OrderResponse(
        Long id,
        Long restaurantId,
        Integer tableNumber,
        String qrToken,
        OrderStatus status,
        BigDecimal totalAmount,
        String customerName,
        String notes,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
