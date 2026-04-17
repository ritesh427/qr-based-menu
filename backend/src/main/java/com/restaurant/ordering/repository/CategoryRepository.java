package com.restaurant.ordering.repository;

import java.util.List;

import com.restaurant.ordering.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByRestaurantIdOrderByNameAsc(Long restaurantId);
}
