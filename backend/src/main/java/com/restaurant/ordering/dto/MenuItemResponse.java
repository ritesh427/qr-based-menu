package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

public record MenuItemResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean vegetarian,
        Integer stockQuantity,
        Integer estimatedPreparationTime,
        Long categoryId,
        String categoryName,
        Double averageRating,
        Long reviewCount,
        Long orderCount,
        List<MenuItemVariantResponse> variants,
        List<MenuItemAddonResponse> addons,
        List<MenuItemTranslationResponse> translations
) {
}
