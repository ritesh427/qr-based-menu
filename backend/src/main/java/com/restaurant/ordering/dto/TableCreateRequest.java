package com.restaurant.ordering.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TableCreateRequest(
        @NotNull @Min(1) Integer startTableNumber,
        @NotNull @Min(1) Integer count
) {
}
