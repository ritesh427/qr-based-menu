package com.restaurant.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsSummaryResponse(
        BigDecimal revenue,
        BigDecimal paidRevenue,
        BigDecimal pendingRevenue,
        long totalOrders,
        long servedOrders,
        long activeOrders,
        long pendingPayments,
        BigDecimal averageTicket,
        List<TopItemResponse> topItems,
        List<HourlyOrderResponse> hourlyOrders
) {
}
