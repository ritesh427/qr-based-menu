package com.restaurant.ordering.dto;

public record AuthResponse(
        String token,
        String username,
        Long restaurantId
) {
}
