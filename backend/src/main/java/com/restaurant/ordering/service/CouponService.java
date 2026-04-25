package com.restaurant.ordering.service;

import java.util.List;

import com.restaurant.ordering.dto.CouponRequest;
import com.restaurant.ordering.dto.CouponResponse;
import com.restaurant.ordering.entity.Coupon;
import com.restaurant.ordering.entity.Restaurant;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.CouponRepository;
import com.restaurant.ordering.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final RestaurantRepository restaurantRepository;
    private final EntityMapper entityMapper;

    public CouponService(CouponRepository couponRepository,
                         RestaurantRepository restaurantRepository,
                         EntityMapper entityMapper) {
        this.couponRepository = couponRepository;
        this.restaurantRepository = restaurantRepository;
        this.entityMapper = entityMapper;
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons(Long restaurantId) {
        return couponRepository.findByRestaurantIdOrderByCodeAsc(restaurantId).stream()
                .map(entityMapper::toCouponResponse)
                .toList();
    }

    public CouponResponse createCoupon(Long restaurantId, CouponRequest request) {
        couponRepository.findByRestaurantIdAndCodeIgnoreCase(restaurantId, request.code())
                .ifPresent(existing -> {
                    throw new BadRequestException("Coupon code already exists");
                });
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        Coupon coupon = new Coupon();
        coupon.setRestaurant(restaurant);
        applyRequest(coupon, request);
        return entityMapper.toCouponResponse(couponRepository.save(coupon));
    }

    @Transactional(readOnly = true)
    public Coupon findActiveCouponOrNull(Long restaurantId, String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return null;
        }
        Coupon coupon = couponRepository.findByRestaurantIdAndCodeIgnoreCase(restaurantId, couponCode.trim())
                .orElseThrow(() -> new BadRequestException("Coupon not found"));
        if (!coupon.isActive()) {
            throw new BadRequestException("Coupon is inactive");
        }
        return coupon;
    }

    private void applyRequest(Coupon coupon, CouponRequest request) {
        coupon.setCode(request.code().trim().toUpperCase());
        coupon.setDescription(request.description());
        coupon.setDiscountValue(request.discountValue());
        coupon.setPercentage(request.percentage());
        coupon.setActive(request.active());
        coupon.setMinimumOrderAmount(request.minimumOrderAmount());
        coupon.setMaxDiscountAmount(request.maxDiscountAmount());
    }
}
