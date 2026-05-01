package com.restaurant.ordering.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long restaurantId,
        String qrToken,
        Long menuItemId,
        String itemName,
        String customerName,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
