package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.QrCodeResponse;
import com.restaurant.ordering.dto.TableCreateRequest;
import com.restaurant.ordering.service.TableService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tables")
public class AdminTableController {

    private final TableService tableService;

    public AdminTableController(TableService tableService) {
        this.tableService = tableService;
    }

    @PostMapping
    public List<QrCodeResponse> createTables(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                             @Valid @RequestBody TableCreateRequest request) {
        return tableService.createTables(restaurantId, request);
    }
}
