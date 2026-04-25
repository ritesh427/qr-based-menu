package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.AssistanceRequestCreateRequest;
import com.restaurant.ordering.dto.AssistanceRequestResponse;
import com.restaurant.ordering.dto.FinalBillResponse;
import com.restaurant.ordering.dto.OrderQuoteRequest;
import com.restaurant.ordering.dto.OrderQuoteResponse;
import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.QrMenuResponse;
import com.restaurant.ordering.service.AssistanceRequestService;
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
    private final AssistanceRequestService assistanceRequestService;

    public PublicMenuController(MenuService menuService,
                                OrderService orderService,
                                AssistanceRequestService assistanceRequestService) {
        this.menuService = menuService;
        this.orderService = orderService;
        this.assistanceRequestService = assistanceRequestService;
    }

    @GetMapping("/menu/{qrToken}")
    public QrMenuResponse getMenu(@PathVariable String qrToken) {
        return menuService.getMenuByQr(qrToken);
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @PostMapping("/orders/quote")
    public OrderQuoteResponse quoteOrder(@Valid @RequestBody OrderQuoteRequest request) {
        return orderService.quoteOrder(request);
    }

    @GetMapping("/orders/{qrToken}")
    public List<OrderResponse> getOrders(@PathVariable String qrToken) {
        return orderService.getOrdersByQr(qrToken);
    }

    @GetMapping("/bill/{qrToken}")
    public FinalBillResponse getFinalBill(@PathVariable String qrToken) {
        return orderService.getFinalBill(qrToken);
    }

    @PostMapping("/service-requests")
    public AssistanceRequestResponse createServiceRequest(@Valid @RequestBody AssistanceRequestCreateRequest request) {
        return assistanceRequestService.createRequest(request);
    }
}
