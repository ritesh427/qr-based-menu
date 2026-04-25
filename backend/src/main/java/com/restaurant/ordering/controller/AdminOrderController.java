package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.FinalBillResponse;
import com.restaurant.ordering.dto.OrderPaymentUpdateRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.OrderStatsResponse;
import com.restaurant.ordering.dto.OrderStatusUpdateRequest;
import com.restaurant.ordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> getOrders(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return orderService.getOrdersForRestaurant(restaurantId);
    }

    @GetMapping("/stats")
    public OrderStatsResponse getStats(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return orderService.getStats(restaurantId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                      @PathVariable Long id,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.updateStatus(restaurantId, id, request.status());
    }

    @PatchMapping("/{id}/payment")
    public OrderResponse updatePayment(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                       @PathVariable Long id,
                                       @Valid @RequestBody OrderPaymentUpdateRequest request) {
        return orderService.updatePayment(restaurantId, id, request);
    }

    @GetMapping("/bill/{qrToken}")
    public FinalBillResponse getFinalBill(@PathVariable String qrToken) {
        return orderService.getFinalBill(qrToken);
    }
}
