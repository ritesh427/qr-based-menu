package com.restaurant.ordering.dto;

import com.restaurant.ordering.enums.AssistanceRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssistanceRequestCreateRequest(
        @NotBlank String qrToken,
        @NotNull AssistanceRequestType type,
        String note
) {
}
