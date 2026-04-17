package com.restaurant.ordering.dto;

public record OrderStatsResponse(
        long created,
        long confirmed,
        long preparing,
        long ready,
        long served
) {
}
