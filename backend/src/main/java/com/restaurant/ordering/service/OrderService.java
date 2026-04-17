package com.restaurant.ordering.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.ordering.dto.OrderItemRequest;
import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.OrderStatsResponse;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.OrderItem;
import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final DiningTableRepository diningTableRepository;
    private final MenuItemRepository menuItemRepository;
    private final EntityMapper entityMapper;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        DiningTableRepository diningTableRepository,
                        MenuItemRepository menuItemRepository,
                        EntityMapper entityMapper,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.diningTableRepository = diningTableRepository;
        this.menuItemRepository = menuItemRepository;
        this.entityMapper = entityMapper;
        this.eventPublisher = eventPublisher;
    }

    public OrderResponse createOrder(OrderRequest request) {
        DiningTable table = diningTableRepository.findByQrCodeToken(request.qrToken())
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found"));

        CustomerOrder order = new CustomerOrder();
        order.setRestaurant(table.getRestaurant());
        order.setTable(table);
        order.setCustomerName(request.customerName());
        order.setNotes(request.notes());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .filter(MenuItem::isAvailable)
                    .orElseThrow(() -> new BadRequestException("Menu item unavailable: " + itemRequest.menuItemId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(menuItem.getPrice());
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            orderItem.setLineTotal(lineTotal);
            total = total.add(lineTotal);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);
        CustomerOrder saved = orderRepository.save(order);
        OrderResponse response = entityMapper.toOrderResponse(saved);
        eventPublisher.publishOrderUpdate(response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(entityMapper::toOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByQr(String qrToken) {
        return orderRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(qrToken).stream()
                .map(entityMapper::toOrderResponse)
                .toList();
    }

    public OrderResponse updateStatus(Long restaurantId, Long orderId, OrderStatus status) {
        CustomerOrder order = orderRepository.findById(orderId)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        validateTransition(order.getStatus(), status);
        order.setStatus(status);
        OrderResponse response = entityMapper.toOrderResponse(order);
        eventPublisher.publishOrderUpdate(response);
        return response;
    }

    @Transactional(readOnly = true)
    public OrderStatsResponse getStats(Long restaurantId) {
        return new OrderStatsResponse(
                orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.CREATED),
                orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.CONFIRMED),
                orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.PREPARING),
                orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.READY),
                orderRepository.countByRestaurantIdAndStatus(restaurantId, OrderStatus.SERVED)
        );
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.SERVED && target != OrderStatus.SERVED) {
            throw new BadRequestException("Served orders cannot move backward");
        }
        if (target.ordinal() < current.ordinal()) {
            throw new BadRequestException("Order status cannot move backward");
        }
    }
}
