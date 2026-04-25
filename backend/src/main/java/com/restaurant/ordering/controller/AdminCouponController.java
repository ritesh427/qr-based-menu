package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.CouponRequest;
import com.restaurant.ordering.dto.CouponResponse;
import com.restaurant.ordering.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/coupons")
public class AdminCouponController {

    private final CouponService couponService;

    public AdminCouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public List<CouponResponse> getCoupons(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return couponService.getCoupons(restaurantId);
    }

    @PostMapping
    public CouponResponse createCoupon(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                       @Valid @RequestBody CouponRequest request) {
        return couponService.createCoupon(restaurantId, request);
    }
}
