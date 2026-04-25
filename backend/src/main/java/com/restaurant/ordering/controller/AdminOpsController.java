package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.AssistanceRequestResponse;
import com.restaurant.ordering.dto.AssistanceRequestStatusUpdateRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.TableSessionResponse;
import com.restaurant.ordering.service.AssistanceRequestService;
import com.restaurant.ordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/ops")
public class AdminOpsController {

    private final AssistanceRequestService assistanceRequestService;
    private final OrderService orderService;

    public AdminOpsController(AssistanceRequestService assistanceRequestService, OrderService orderService) {
        this.assistanceRequestService = assistanceRequestService;
        this.orderService = orderService;
    }

    @GetMapping("/service-requests")
    public List<AssistanceRequestResponse> getServiceRequests(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return assistanceRequestService.getRequestsForRestaurant(restaurantId);
    }

    @PatchMapping("/service-requests/{id}/status")
    public AssistanceRequestResponse updateServiceRequestStatus(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                                                @PathVariable Long id,
                                                                @Valid @RequestBody AssistanceRequestStatusUpdateRequest request) {
        return assistanceRequestService.updateStatus(restaurantId, id, request.status());
    }

    @GetMapping("/kitchen")
    public List<OrderResponse> getKitchenOrders(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return orderService.getKitchenOrders(restaurantId);
    }

    @GetMapping("/table-sessions")
    public List<TableSessionResponse> getTableSessions(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return assistanceRequestService.getTableSessions(restaurantId);
    }
}
