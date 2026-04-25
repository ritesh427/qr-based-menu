package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.enums.PaymentMethod;
import com.restaurant.ordering.enums.PaymentStatus;

public record OrderResponse(
        Long id,
        Long restaurantId,
        Integer tableNumber,
        String qrToken,
        OrderStatus status,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        BigDecimal serviceChargeAmount,
        String appliedCouponCode,
        String paymentReference,
        String customerName,
        String notes,
        Integer estimatedReadyInMinutes,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
}
