package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.Role;

public record StaffResponse(
        Long id,
        String username,
        Role role,
        Long restaurantId
) {
}
