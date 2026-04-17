package com.restaurant.ordering.mapper;

import java.util.List;

import com.restaurant.ordering.dto.CategoryResponse;
import com.restaurant.ordering.dto.MenuItemResponse;
import com.restaurant.ordering.dto.OrderItemResponse;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public MenuItemResponse toMenuItemResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getImageUrl(),
                item.isAvailable(),
                item.isVegetarian(),
                item.getEstimatedPreparationTime(),
                item.getCategory().getId(),
                item.getCategory().getName()
        );
    }

    public OrderResponse toOrderResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream().map(this::toOrderItemResponse).toList();
        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getTable().getTableNumber(),
                order.getTable().getQrCodeToken(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCustomerName(),
                order.getNotes(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
