package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

import com.restaurant.ordering.enums.OrderStatus;

public record TableSessionResponse(
        Integer tableNumber,
        String qrToken,
        int activeOrderCount,
        BigDecimal activeOrderTotal,
        OrderStatus latestOrderStatus,
        List<AssistanceRequestResponse> openRequests
) {
}
