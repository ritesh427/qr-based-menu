package com.restaurant.ordering.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.restaurant.ordering.dto.AnalyticsSummaryResponse;
import com.restaurant.ordering.dto.HourlyOrderResponse;
import com.restaurant.ordering.dto.TopItemResponse;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.OrderItem;
import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.enums.PaymentStatus;
import com.restaurant.ordering.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final DateTimeFormatter HOUR_FORMATTER = DateTimeFormatter.ofPattern("HH:00");

    private final OrderRepository orderRepository;

    public AnalyticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public AnalyticsSummaryResponse getSummary(Long restaurantId) {
        List<CustomerOrder> orders = orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        BigDecimal revenue = sumOrderTotals(orders);
        BigDecimal paidRevenue = orders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long servedOrders = orders.stream().filter(order -> order.getStatus() == OrderStatus.SERVED).count();
        long activeOrders = orders.stream().filter(order -> order.getStatus() != OrderStatus.SERVED).count();
        long pendingPayments = orders.stream().filter(order -> order.getPaymentStatus() == PaymentStatus.PENDING).count();

        return new AnalyticsSummaryResponse(
                money(revenue),
                money(paidRevenue),
                money(revenue.subtract(paidRevenue)),
                orders.size(),
                servedOrders,
                activeOrders,
                pendingPayments,
                orders.isEmpty() ? BigDecimal.ZERO : money(revenue.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP)),
                topItems(orders),
                hourlyOrders(orders)
        );
    }

    private List<TopItemResponse> topItems(List<CustomerOrder> orders) {
        Map<Long, ItemTotals> totals = new LinkedHashMap<>();
        for (CustomerOrder order : orders) {
            for (OrderItem item : order.getItems()) {
                Long menuItemId = item.getMenuItem().getId();
                ItemTotals itemTotals = totals.computeIfAbsent(
                        menuItemId,
                        ignored -> new ItemTotals(menuItemId, item.getMenuItem().getName())
                );
                itemTotals.quantitySold += item.getQuantity();
                itemTotals.revenue = itemTotals.revenue.add(nullSafe(item.getLineTotal()));
            }
        }

        return totals.values().stream()
                .sorted(Comparator.comparingLong(ItemTotals::quantitySold).reversed())
                .limit(8)
                .map(total -> new TopItemResponse(total.menuItemId, total.itemName, total.quantitySold, money(total.revenue)))
                .toList();
    }

    private List<HourlyOrderResponse> hourlyOrders(List<CustomerOrder> orders) {
        Map<String, HourTotals> totals = new TreeMap<>();
        for (CustomerOrder order : orders) {
            String hour = order.getCreatedAt().format(HOUR_FORMATTER);
            HourTotals hourTotals = totals.computeIfAbsent(hour, ignored -> new HourTotals());
            hourTotals.orderCount++;
            hourTotals.revenue = hourTotals.revenue.add(nullSafe(order.getTotalAmount()));
        }
        return totals.entrySet().stream()
                .map(entry -> new HourlyOrderResponse(entry.getKey(), entry.getValue().orderCount, money(entry.getValue().revenue)))
                .toList();
    }

    private BigDecimal sumOrderTotals(List<CustomerOrder> orders) {
        return orders.stream()
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal money(BigDecimal value) {
        return nullSafe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static class ItemTotals {
        private final Long menuItemId;
        private final String itemName;
        private long quantitySold;
        private BigDecimal revenue = BigDecimal.ZERO;

        private ItemTotals(Long menuItemId, String itemName) {
            this.menuItemId = menuItemId;
            this.itemName = itemName;
        }

        private long quantitySold() {
            return quantitySold;
        }
    }

    private static class HourTotals {
        private long orderCount;
        private BigDecimal revenue = BigDecimal.ZERO;
    }
}
