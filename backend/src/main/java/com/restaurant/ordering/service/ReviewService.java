package com.restaurant.ordering.service;

import java.util.List;

import com.restaurant.ordering.dto.ReviewRequest;
import com.restaurant.ordering.dto.ReviewResponse;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.entity.MenuItemReview;
import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.repository.MenuItemReviewRepository;
import com.restaurant.ordering.repository.OrderRepository;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final MenuItemReviewRepository reviewRepository;
    private final DiningTableRepository diningTableRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final EntityMapper entityMapper;
    private final CacheManager cacheManager;

    public ReviewService(MenuItemReviewRepository reviewRepository,
                         DiningTableRepository diningTableRepository,
                         MenuItemRepository menuItemRepository,
                         OrderRepository orderRepository,
                         EntityMapper entityMapper,
                         CacheManager cacheManager) {
        this.reviewRepository = reviewRepository;
        this.diningTableRepository = diningTableRepository;
        this.menuItemRepository = menuItemRepository;
        this.orderRepository = orderRepository;
        this.entityMapper = entityMapper;
        this.cacheManager = cacheManager;
    }

    public ReviewResponse createReview(ReviewRequest request) {
        DiningTable table = diningTableRepository.findByQrCodeToken(request.qrToken())
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found"));
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .filter(item -> item.getCategory().getRestaurant().getId().equals(table.getRestaurant().getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        boolean orderedAndServed = orderRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(request.qrToken()).stream()
                .filter(order -> order.getStatus() == OrderStatus.SERVED)
                .map(CustomerOrder::getItems)
                .flatMap(List::stream)
                .anyMatch(item -> item.getMenuItem().getId().equals(request.menuItemId()));
        if (!orderedAndServed) {
            throw new BadRequestException("Reviews can be added after the item has been served");
        }

        MenuItemReview review = new MenuItemReview();
        review.setRestaurant(table.getRestaurant());
        review.setTable(table);
        review.setMenuItem(menuItem);
        review.setCustomerName(request.customerName());
        review.setRating(request.rating());
        review.setComment(request.comment());
        MenuItemReview saved = reviewRepository.save(review);
        clearMenuCache();
        return entityMapper.toReviewResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByQr(String qrToken) {
        return reviewRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(qrToken).stream()
                .map(entityMapper::toReviewResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByRestaurant(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(entityMapper::toReviewResponse)
                .toList();
    }

    private void clearMenuCache() {
        if (cacheManager.getCache("menuByQr") != null) {
            cacheManager.getCache("menuByQr").clear();
        }
    }
}
