package com.restaurant.ordering.dto;

import java.util.List;

import com.restaurant.ordering.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderRequest(
        @NotBlank String qrToken,
        String customerName,
        String notes,
        Integer desiredReadyInMinutes,
        String couponCode,
        PaymentMethod paymentMethod,
        boolean payNow,
        @Valid @NotEmpty List<OrderItemRequest> items
) {
}
