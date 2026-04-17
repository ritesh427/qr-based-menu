package com.restaurant.ordering.repository;

import java.util.List;

import com.restaurant.ordering.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCategoryRestaurantIdOrderByCategoryNameAscNameAsc(Long restaurantId);
    List<MenuItem> findByCategoryIdOrderByNameAsc(Long categoryId);
}
