package com.restaurant.ordering.dto;

import java.util.List;

public record QrMenuResponse(
        Long restaurantId,
        String restaurantName,
        String restaurantSlug,
        Integer tableNumber,
        String qrToken,
        List<MenuCategoryResponse> categories
) {
}
