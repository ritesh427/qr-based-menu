package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record CouponResponse(
        Long id,
        String code,
        String description,
        BigDecimal discountValue,
        boolean percentage,
        boolean active,
        BigDecimal minimumOrderAmount,
        BigDecimal maxDiscountAmount
) {
}
