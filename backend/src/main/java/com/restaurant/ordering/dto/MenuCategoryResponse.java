package com.restaurant.ordering.dto;

import java.util.List;

public record MenuCategoryResponse(
        Long id,
        String name,
        String description,
        List<MenuItemResponse> items
) {
}
