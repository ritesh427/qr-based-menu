package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {
}
