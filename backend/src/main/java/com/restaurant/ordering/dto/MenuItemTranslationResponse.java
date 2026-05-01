package com.restaurant.ordering.dto;

public record MenuItemTranslationResponse(
        Long id,
        String languageCode,
        String name,
        String description
) {
}
