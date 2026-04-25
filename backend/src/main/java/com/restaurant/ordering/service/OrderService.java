package com.restaurant.ordering.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.restaurant.ordering.dto.FinalBillResponse;
import com.restaurant.ordering.dto.OrderItemRequest;
import com.restaurant.ordering.dto.OrderPaymentUpdateRequest;
import com.restaurant.ordering.dto.OrderQuoteRequest;
import com.restaurant.ordering.dto.OrderQuoteResponse;
import com.restaurant.ordering.dto.OrderRequest;
import com.restaurant.ordering.dto.OrderResponse;
import com.restaurant.ordering.dto.OrderStatsResponse;
import com.restaurant.ordering.entity.Coupon;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.MenuItemAddon;
import com.restaurant.ordering.entity.MenuItemVariant;
import com.restaurant.ordering.entity.OrderItem;
import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.enums.PaymentMethod;
import com.restaurant.ordering.enums.PaymentStatus;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.repository.OrderRepository;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal SERVICE_CHARGE_RATE = new BigDecimal("0.10");

    private final OrderRepository orderRepository;
    private final DiningTableRepository diningTableRepository;
    private final MenuItemRepository menuItemRepository;
    private final EntityMapper entityMapper;
    private final OrderEventPublisher eventPublisher;
    private final AssistanceRequestService assistanceRequestService;
    private final CacheManager cacheManager;
    private final CouponService couponService;

    public OrderService(OrderRepository orderRepository,
                        DiningTableRepository diningTableRepository,
                        MenuItemRepository menuItemRepository,
                        EntityMapper entityMapper,
                        OrderEventPublisher eventPublisher,
                        AssistanceRequestService assistanceRequestService,
                        CacheManager cacheManager,
                        CouponService couponService) {
        this.orderRepository = orderRepository;
        this.diningTableRepository = diningTableRepository;
        this.menuItemRepository = menuItemRepository;
        this.entityMapper = entityMapper;
        this.eventPublisher = eventPublisher;
        this.assistanceRequestService = assistanceRequestService;
        this.cacheManager = cacheManager;
        this.couponService = couponService;
    }

    public OrderQuoteResponse quoteOrder(OrderQuoteRequest request) {
        DiningTable table = findTable(request.qrToken());
        PricingResult pricing = buildPricing(table, request.items(), request.couponCode(), request.paymentMethod(), false);
        return toQuoteResponse(pricing);
    }

    public OrderResponse createOrder(OrderRequest request) {
        DiningTable table = findTable(request.qrToken());
        PricingResult pricing = buildPricing(table, request.items(), request.couponCode(), request.paymentMethod(), true);

        CustomerOrder order = new CustomerOrder();
        order.setRestaurant(table.getRestaurant());
        order.setTable(table);
        order.setCustomerName(request.customerName());
        order.setNotes(request.notes());
        order.setItems(pricing.orderItems());
        order.setSubtotalAmount(pricing.subtotalAmount());
        order.setDiscountAmount(pricing.discountAmount());
        order.setTaxAmount(pricing.taxAmount());
        order.setServiceChargeAmount(pricing.serviceChargeAmount());
        order.setTotalAmount(pricing.payableAmount());
        order.setAppliedCouponCode(pricing.appliedCouponCode());
        order.setPaymentMethod(pricing.paymentMethod());
        order.setPaymentStatus(request.payNow() ? PaymentStatus.PAID : PaymentStatus.PENDING);
        order.setPaymentReference(request.payNow() ? generatePaymentReference(pricing.paymentMethod()) : null);
        order.setEstimatedReadyInMinutes(Math.max(pricing.estimatedReadyInMinutes(), defaultZero(request.desiredReadyInMinutes())));

        for (OrderItem item : pricing.orderItems()) {
            item.setOrder(order);
        }

        CustomerOrder saved = orderRepository.save(order);
        clearMenuCache();
        publishOrderAndSession(saved);
        return entityMapper.toOrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public FinalBillResponse getFinalBill(String qrToken) {
        DiningTable table = findTable(qrToken);
        List<CustomerOrder> orders = orderRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(qrToken);
        BigDecimal subtotal = sumOrders(orders, CustomerOrder::getSubtotalAmount);
        BigDecimal discount = sumOrders(orders, CustomerOrder::getDiscountAmount);
        BigDecimal tax = sumOrders(orders, CustomerOrder::getTaxAmount);
        BigDecimal serviceCharge = sumOrders(orders, CustomerOrder::getServiceChargeAmount);
        BigDecimal payable = sumOrders(orders, CustomerOrder::getTotalAmount);
        BigDecimal paid = orders.stream()
                .filter(order -> order.getPaymentStatus() == PaymentStatus.PAID)
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FinalBillResponse(
                qrToken,
                table.getTableNumber(),
                scale(subtotal),
                scale(discount),
                scale(tax),
                scale(serviceCharge),
                scale(payable),
                scale(paid),
                scale(payable.subtract(paid)),
                orders.stream().map(entityMapper::toOrderResponse).toList()
        );
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
        CustomerOrder order = findRestaurantOrder(restaurantId, orderId);
        validateTransition(order.getStatus(), status);
        order.setStatus(status);
        publishOrderAndSession(order);
        return entityMapper.toOrderResponse(order);
    }

    public OrderResponse updatePayment(Long restaurantId, Long orderId, OrderPaymentUpdateRequest request) {
        CustomerOrder order = findRestaurantOrder(restaurantId, orderId);
        order.setPaymentStatus(request.paymentStatus());
        if (request.paymentMethod() != null) {
            order.setPaymentMethod(request.paymentMethod());
        }
        if (request.paymentStatus() == PaymentStatus.PAID) {
            order.setPaymentReference(
                    request.paymentReference() == null || request.paymentReference().isBlank()
                            ? generatePaymentReference(order.getPaymentMethod())
                            : request.paymentReference()
            );
        } else {
            order.setPaymentReference(request.paymentReference());
        }
        publishOrderAndSession(order);
        return entityMapper.toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getKitchenOrders(Long restaurantId) {
        return orderRepository.findByRestaurantIdAndStatusInOrderByCreatedAtAsc(
                        restaurantId,
                        List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY))
                .stream()
                .map(entityMapper::toOrderResponse)
                .toList();
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

    private PricingResult buildPricing(DiningTable table,
                                       List<OrderItemRequest> itemRequests,
                                       String couponCode,
                                       PaymentMethod paymentMethod,
                                       boolean decrementStock) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int computedEta = 0;

        for (OrderItemRequest itemRequest : itemRequests) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.menuItemId())
                    .filter(MenuItem::isAvailable)
                    .orElseThrow(() -> new BadRequestException("Menu item unavailable: " + itemRequest.menuItemId()));
            if (menuItem.getStockQuantity() < itemRequest.quantity()) {
                throw new BadRequestException("Only " + menuItem.getStockQuantity() + " item(s) left for " + menuItem.getName());
            }

            MenuItemVariant selectedVariant = resolveVariant(menuItem, itemRequest.variantId());
            List<MenuItemAddon> selectedAddons = resolveAddons(menuItem, itemRequest.addonIds());
            validateSelectionStocks(itemRequest, selectedVariant, selectedAddons);

            int itemEta = calculateEstimatedPreparationTime(menuItem, selectedVariant, selectedAddons);
            BigDecimal unitPrice = calculateUnitPrice(menuItem, selectedVariant, selectedAddons);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setSelectedVariantName(selectedVariant != null ? selectedVariant.getName() : null);
            orderItem.setSelectedAddonNames(selectedAddons.isEmpty() ? null : String.join(", ", selectedAddons.stream().map(MenuItemAddon::getName).toList()));
            orderItem.setEstimatedPreparationTime(itemEta);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);
            orderItems.add(orderItem);

            subtotal = subtotal.add(lineTotal);
            computedEta = Math.max(computedEta, itemEta);

            if (decrementStock) {
                decrementStocks(menuItem, selectedVariant, selectedAddons, itemRequest.quantity());
            }
        }

        Coupon coupon = couponService.findActiveCouponOrNull(table.getRestaurant().getId(), couponCode);
        BigDecimal discount = calculateDiscount(subtotal, coupon);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tax = scale(afterDiscount.multiply(TAX_RATE));
        BigDecimal serviceCharge = scale(afterDiscount.multiply(SERVICE_CHARGE_RATE));
        BigDecimal payable = scale(afterDiscount.add(tax).add(serviceCharge));
        return new PricingResult(
                orderItems,
                scale(subtotal),
                scale(discount),
                tax,
                serviceCharge,
                payable,
                coupon != null ? coupon.getCode() : null,
                paymentMethod == null ? PaymentMethod.PAY_AT_COUNTER : paymentMethod,
                computedEta
        );
    }

    private OrderQuoteResponse toQuoteResponse(PricingResult pricing) {
        return new OrderQuoteResponse(
                pricing.subtotalAmount(),
                pricing.discountAmount(),
                pricing.taxAmount(),
                pricing.serviceChargeAmount(),
                pricing.payableAmount(),
                pricing.appliedCouponCode(),
                pricing.paymentMethod()
        );
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, Coupon coupon) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        if (subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new BadRequestException("Coupon minimum order is Rs. " + coupon.getMinimumOrderAmount());
        }
        BigDecimal discount = coupon.isPercentage()
                ? subtotal.multiply(coupon.getDiscountValue()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountValue();
        if (coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(coupon.getMaxDiscountAmount());
        }
        return scale(discount.min(subtotal));
    }

    private BigDecimal calculateUnitPrice(MenuItem menuItem,
                                          MenuItemVariant selectedVariant,
                                          List<MenuItemAddon> selectedAddons) {
        BigDecimal unitPrice = menuItem.getPrice();
        if (selectedVariant != null) {
            unitPrice = unitPrice.add(selectedVariant.getPriceAdjustment());
        }
        for (MenuItemAddon addon : selectedAddons) {
            unitPrice = unitPrice.add(addon.getPrice());
        }
        return scale(unitPrice);
    }

    private void validateSelectionStocks(OrderItemRequest itemRequest,
                                         MenuItemVariant selectedVariant,
                                         List<MenuItemAddon> selectedAddons) {
        if (selectedVariant != null && selectedVariant.getStockQuantity() < itemRequest.quantity()) {
            throw new BadRequestException("Only " + selectedVariant.getStockQuantity() + " variant(s) left for " + selectedVariant.getName());
        }
        for (MenuItemAddon addon : selectedAddons) {
            if (addon.getStockQuantity() < itemRequest.quantity()) {
                throw new BadRequestException("Only " + addon.getStockQuantity() + " add-on(s) left for " + addon.getName());
            }
        }
    }

    private void decrementStocks(MenuItem menuItem,
                                 MenuItemVariant selectedVariant,
                                 List<MenuItemAddon> selectedAddons,
                                 Integer quantity) {
        menuItem.setStockQuantity(menuItem.getStockQuantity() - quantity);
        if (menuItem.getStockQuantity() <= 0) {
            menuItem.setStockQuantity(0);
            menuItem.setAvailable(false);
        }
        if (selectedVariant != null) {
            selectedVariant.setStockQuantity(selectedVariant.getStockQuantity() - quantity);
            if (selectedVariant.getStockQuantity() <= 0) {
                selectedVariant.setStockQuantity(0);
                selectedVariant.setAvailable(false);
            }
        }
        for (MenuItemAddon addon : selectedAddons) {
            addon.setStockQuantity(addon.getStockQuantity() - quantity);
            if (addon.getStockQuantity() <= 0) {
                addon.setStockQuantity(0);
                addon.setAvailable(false);
            }
        }
    }

    private DiningTable findTable(String qrToken) {
        return diningTableRepository.findByQrCodeToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found"));
    }

    private CustomerOrder findRestaurantOrder(Long restaurantId, Long orderId) {
        return orderRepository.findById(orderId)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private MenuItemVariant resolveVariant(MenuItem menuItem, Long variantId) {
        if (variantId == null) {
            return null;
        }
        MenuItemVariant variant = menuItem.getVariants().stream()
                .filter(entry -> Objects.equals(entry.getId(), variantId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Invalid variant selected for " + menuItem.getName()));
        if (!variant.isAvailable()) {
            throw new BadRequestException("Variant unavailable: " + variant.getName());
        }
        return variant;
    }

    private List<MenuItemAddon> resolveAddons(MenuItem menuItem, List<Long> addonIds) {
        if (addonIds == null || addonIds.isEmpty()) {
            return List.of();
        }
        List<Long> uniqueIds = new ArrayList<>(new LinkedHashSet<>(addonIds));
        List<MenuItemAddon> addons = uniqueIds.stream()
                .map(addonId -> menuItem.getAddons().stream()
                        .filter(entry -> Objects.equals(entry.getId(), addonId))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("Invalid add-on selected for " + menuItem.getName())))
                .toList();
        for (MenuItemAddon addon : addons) {
            if (!addon.isAvailable()) {
                throw new BadRequestException("Add-on unavailable: " + addon.getName());
            }
        }
        return addons;
    }

    private int calculateEstimatedPreparationTime(MenuItem menuItem,
                                                  MenuItemVariant selectedVariant,
                                                  List<MenuItemAddon> selectedAddons) {
        int eta = menuItem.getEstimatedPreparationTime() == null ? 10 : menuItem.getEstimatedPreparationTime();
        if (selectedVariant != null && selectedVariant.getEstimatedPreparationTime() != null) {
            eta = Math.max(eta, selectedVariant.getEstimatedPreparationTime());
        }
        for (MenuItemAddon addon : selectedAddons) {
            if (addon.getEstimatedPreparationTime() != null) {
                eta = Math.max(eta, addon.getEstimatedPreparationTime());
            }
        }
        return eta;
    }

    private void publishOrderAndSession(CustomerOrder order) {
        OrderResponse response = entityMapper.toOrderResponse(order);
        eventPublisher.publishOrderUpdate(response);
        eventPublisher.publishTableSessionUpdate(order.getRestaurant().getId(),
                assistanceRequestService.getTableSessions(order.getRestaurant().getId()).stream()
                        .filter(session -> session.qrToken().equals(order.getTable().getQrCodeToken()))
                        .findFirst()
                        .orElse(null));
    }

    private void clearMenuCache() {
        if (cacheManager.getCache("menuByQr") != null) {
            cacheManager.getCache("menuByQr").clear();
        }
    }

    private String generatePaymentReference(PaymentMethod paymentMethod) {
        return (paymentMethod == null ? PaymentMethod.PAY_AT_COUNTER : paymentMethod).name() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private BigDecimal sumOrders(List<CustomerOrder> orders, java.util.function.Function<CustomerOrder, BigDecimal> mapper) {
        return orders.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private int defaultZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void validateTransition(OrderStatus current, OrderStatus target) {
        if (current == OrderStatus.SERVED && target != OrderStatus.SERVED) {
            throw new BadRequestException("Served orders cannot move backward");
        }
        if (target.ordinal() < current.ordinal()) {
            throw new BadRequestException("Order status cannot move backward");
        }
    }

    private record PricingResult(
            List<OrderItem> orderItems,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal serviceChargeAmount,
            BigDecimal payableAmount,
            String appliedCouponCode,
            PaymentMethod paymentMethod,
            Integer estimatedReadyInMinutes
    ) {
    }
}
