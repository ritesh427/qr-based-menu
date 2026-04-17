package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.CategoryRequest;
import com.restaurant.ordering.dto.CategoryResponse;
import com.restaurant.ordering.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getCategories(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return categoryService.getAll(restaurantId);
    }

    @PostMapping
    public CategoryResponse createCategory(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                           @Valid @RequestBody CategoryRequest request) {
        return categoryService.create(restaurantId, request);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                           @PathVariable Long id,
                                           @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(restaurantId, id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                               @PathVariable Long id) {
        categoryService.delete(restaurantId, id);
    }
}
