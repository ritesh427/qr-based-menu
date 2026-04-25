package com.restaurant.ordering.dto;

import java.time.LocalDateTime;

import com.restaurant.ordering.enums.AssistanceRequestStatus;
import com.restaurant.ordering.enums.AssistanceRequestType;

public record AssistanceRequestResponse(
        Long id,
        Long restaurantId,
        Integer tableNumber,
        String qrToken,
        AssistanceRequestType type,
        AssistanceRequestStatus status,
        String note,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
