package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean vegetarian,
        Integer estimatedPreparationTime,
        Long categoryId,
        String categoryName
) {
}
