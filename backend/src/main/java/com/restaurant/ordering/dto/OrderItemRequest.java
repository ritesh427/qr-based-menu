package com.restaurant.ordering.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull Long menuItemId,
        Long variantId,
        List<Long> addonIds,
        @NotNull @Min(1) Integer quantity
) {
}
