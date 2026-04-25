package com.restaurant.ordering.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CouponRequest(
        @NotBlank String code,
        @NotBlank String description,
        @NotNull @DecimalMin("0.0") BigDecimal discountValue,
        boolean percentage,
        boolean active,
        @NotNull @DecimalMin("0.0") BigDecimal minimumOrderAmount,
        @NotNull @DecimalMin("0.0") BigDecimal maxDiscountAmount
) {
}
