package com.restaurant.ordering.dto;

import jakarta.validation.constraints.NotBlank;

public record MenuItemTranslationRequest(
        @NotBlank String languageCode,
        @NotBlank String name,
        String description
) {
}
