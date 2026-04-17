package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long menuItemId,
        String itemName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
