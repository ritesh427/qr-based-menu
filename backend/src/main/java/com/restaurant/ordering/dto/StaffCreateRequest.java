package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StaffCreateRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 6) String password,
        @NotNull Role role
) {
}
