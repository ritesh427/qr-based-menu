package com.restaurant.ordering.controller;

import com.restaurant.ordering.dto.AnalyticsSummaryResponse;
import com.restaurant.ordering.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public AnalyticsSummaryResponse getSummary(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return analyticsService.getSummary(restaurantId);
    }
}
