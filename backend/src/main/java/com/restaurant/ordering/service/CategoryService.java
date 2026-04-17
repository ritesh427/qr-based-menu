package com.restaurant.ordering.service;

import java.util.List;

import com.restaurant.ordering.dto.CategoryRequest;
import com.restaurant.ordering.dto.CategoryResponse;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.Restaurant;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final RestaurantRepository restaurantRepository;
    private final EntityMapper entityMapper;

    public CategoryService(CategoryRepository categoryRepository,
                           RestaurantRepository restaurantRepository,
                           EntityMapper entityMapper) {
        this.categoryRepository = categoryRepository;
        this.restaurantRepository = restaurantRepository;
        this.entityMapper = entityMapper;
    }

    public List<CategoryResponse> getAll(Long restaurantId) {
        return categoryRepository.findByRestaurantIdOrderByNameAsc(restaurantId).stream()
                .map(entityMapper::toCategoryResponse)
                .toList();
    }

    public CategoryResponse create(Long restaurantId, CategoryRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        Category category = new Category();
        category.setRestaurant(restaurant);
        category.setName(request.name());
        category.setDescription(request.description());
        return entityMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse update(Long restaurantId, Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.name());
        category.setDescription(request.description());
        return entityMapper.toCategoryResponse(category);
    }

    public void delete(Long restaurantId, Long id) {
        Category category = categoryRepository.findById(id)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        categoryRepository.delete(category);
    }
}
