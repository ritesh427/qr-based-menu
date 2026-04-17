package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.QrMenuResponse;
import com.restaurant.ordering.service.MenuService;
import com.restaurant.ordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicMenuController {

    private final MenuService menuService;
    private final OrderService orderService;

    public PublicMenuController(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
    }

    @GetMapping("/menu/{qrToken}")
    public QrMenuResponse getMenu(@PathVariable String qrToken) {
        return menuService.getMenuByQr(qrToken);
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/orders/{qrToken}")
    public List<OrderResponse> getOrders(@PathVariable String qrToken) {
        return orderService.getOrdersByQr(qrToken);
    }
}
