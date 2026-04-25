package com.restaurant.ordering.dto;

import java.util.List;

import com.restaurant.ordering.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record OrderQuoteRequest(
        @NotBlank String qrToken,
        String couponCode,
        PaymentMethod paymentMethod,
        @Valid @NotEmpty List<OrderItemRequest> items
) {
}
