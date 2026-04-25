package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.AssistanceRequestStatus;
import jakarta.validation.constraints.NotNull;

public record AssistanceRequestStatusUpdateRequest(@NotNull AssistanceRequestStatus status) {
}
