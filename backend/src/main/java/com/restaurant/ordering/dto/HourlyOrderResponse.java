package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record HourlyOrderResponse(
        String hour,
        long orderCount,
        BigDecimal revenue
) {
}
