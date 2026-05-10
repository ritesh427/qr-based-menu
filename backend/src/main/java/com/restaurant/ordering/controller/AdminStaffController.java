package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.StaffCreateRequest;
import com.restaurant.ordering.dto.StaffResponse;
import com.restaurant.ordering.service.StaffService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/staff")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStaffController {

    private final StaffService staffService;

    public AdminStaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public List<StaffResponse> getStaff(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return staffService.getStaff(restaurantId);
    }

    @PostMapping
    public StaffResponse createStaff(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                     @Valid @RequestBody StaffCreateRequest request) {
        return staffService.createStaff(restaurantId, request);
    }
}
