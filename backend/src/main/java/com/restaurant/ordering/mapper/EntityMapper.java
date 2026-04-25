package com.restaurant.ordering.mapper;

import java.util.List;

import com.restaurant.ordering.dto.AssistanceRequestResponse;
import com.restaurant.ordering.dto.CategoryResponse;
import com.restaurant.ordering.dto.CouponResponse;
import com.restaurant.ordering.dto.MenuItemAddonResponse;
import com.restaurant.ordering.dto.MenuItemResponse;
import com.restaurant.ordering.dto.MenuItemVariantResponse;
import com.restaurant.ordering.dto.OrderItemResponse;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.Coupon;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.MenuItemAddon;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.MenuItemVariant;
import com.restaurant.ordering.entity.OrderItem;
import com.restaurant.ordering.entity.TableAssistanceRequest;
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
                item.getStockQuantity(),
                item.getEstimatedPreparationTime(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getVariants().stream().map(this::toMenuItemVariantResponse).toList(),
                item.getAddons().stream().map(this::toMenuItemAddonResponse).toList()
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
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getTotalAmount(),
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getTaxAmount(),
                order.getServiceChargeAmount(),
                order.getAppliedCouponCode(),
                order.getPaymentReference(),
                order.getCustomerName(),
                order.getNotes(),
                order.getEstimatedReadyInMinutes(),
                order.getCreatedAt(),
                items
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getSelectedVariantName(),
                item.getSelectedAddonNames(),
                item.getEstimatedPreparationTime(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private MenuItemVariantResponse toMenuItemVariantResponse(MenuItemVariant variant) {
        return new MenuItemVariantResponse(
                variant.getId(),
                variant.getName(),
                variant.getPriceAdjustment(),
                variant.getStockQuantity(),
                variant.isAvailable(),
                variant.getEstimatedPreparationTime()
        );
    }

    private MenuItemAddonResponse toMenuItemAddonResponse(MenuItemAddon addon) {
        return new MenuItemAddonResponse(
                addon.getId(),
                addon.getName(),
                addon.getPrice(),
                addon.getStockQuantity(),
                addon.isAvailable(),
                addon.getEstimatedPreparationTime()
        );
    }

    public AssistanceRequestResponse toAssistanceRequestResponse(TableAssistanceRequest request) {
        return new AssistanceRequestResponse(
                request.getId(),
                request.getRestaurant().getId(),
                request.getTable().getTableNumber(),
                request.getTable().getQrCodeToken(),
                request.getType(),
                request.getStatus(),
                request.getNote(),
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }

    public CouponResponse toCouponResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.isPercentage(),
                coupon.isActive(),
                coupon.getMinimumOrderAmount(),
                coupon.getMaxDiscountAmount()
        );
    }
}
