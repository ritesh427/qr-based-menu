package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long menuItemId,
        String itemName,
        String variantName,
        String addonNames,
        Integer estimatedPreparationTime,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
