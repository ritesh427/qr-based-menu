package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MenuItemRequest(
        @NotBlank String name,
        String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        String imageUrl,
        boolean available,
        boolean vegetarian,
        @NotNull @Min(0) Integer stockQuantity,
        Integer estimatedPreparationTime,
        @NotNull Long categoryId,
        @Valid List<MenuItemVariantRequest> variants,
        @Valid List<MenuItemAddonRequest> addons,
        @Valid List<MenuItemTranslationRequest> translations
) {
}
