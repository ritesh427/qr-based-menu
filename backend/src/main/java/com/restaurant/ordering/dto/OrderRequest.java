package com.restaurant.ordering.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        @NotBlank String qrToken,
        String customerName,
        String notes,
        @Valid @NotEmpty List<OrderItemRequest> items
) {
}
