package com.restaurant.ordering.dto;

public record AuthResponse(
        String token,
        String username,
        String role,
        Long restaurantId
) {
}
