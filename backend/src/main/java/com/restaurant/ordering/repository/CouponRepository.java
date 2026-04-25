package com.restaurant.ordering.repository;

import java.util.List;
import java.util.Optional;

import com.restaurant.ordering.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByRestaurantIdOrderByCodeAsc(Long restaurantId);
    Optional<Coupon> findByRestaurantIdAndCodeIgnoreCase(Long restaurantId, String code);
}
