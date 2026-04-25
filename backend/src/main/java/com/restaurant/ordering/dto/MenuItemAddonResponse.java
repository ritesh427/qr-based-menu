package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record MenuItemAddonResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        boolean available,
        Integer estimatedPreparationTime
) {
}
