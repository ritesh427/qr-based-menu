package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record MenuItemVariantResponse(
        Long id,
        String name,
        BigDecimal priceAdjustment,
        Integer stockQuantity,
        boolean available,
        Integer estimatedPreparationTime
) {
}
