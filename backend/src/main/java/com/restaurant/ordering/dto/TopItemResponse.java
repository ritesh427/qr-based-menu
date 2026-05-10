package com.restaurant.ordering.dto;

import java.math.BigDecimal;

public record TopItemResponse(
        Long menuItemId,
        String itemName,
        long quantitySold,
        BigDecimal revenue
) {
}
