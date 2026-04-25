package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.PaymentMethod;
import com.restaurant.ordering.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record OrderPaymentUpdateRequest(
        @NotNull PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        String paymentReference
) {
}
