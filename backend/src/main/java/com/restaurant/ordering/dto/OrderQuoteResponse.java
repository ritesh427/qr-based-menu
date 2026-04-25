package com.restaurant.ordering.dto;

import java.math.BigDecimal;

import com.restaurant.ordering.enums.PaymentMethod;

public record OrderQuoteResponse(
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceChargeAmount,
        BigDecimal payableAmount,
        String appliedCouponCode,
        PaymentMethod paymentMethod
) {
}
