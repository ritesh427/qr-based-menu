package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinalBillResponse(
        String qrToken,
        Integer tableNumber,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceChargeAmount,
        BigDecimal payableAmount,
        BigDecimal paidAmount,
        BigDecimal pendingAmount,
        List<OrderResponse> orders
) {
}
