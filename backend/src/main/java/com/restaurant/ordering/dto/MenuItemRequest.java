package com.restaurant.ordering.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String imageUrl,
        boolean available,
        boolean vegetarian,
        Integer estimatedPreparationTime,
        @NotNull Long categoryId
) {
}
