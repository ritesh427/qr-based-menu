package com.restaurant.ordering.repository;

import java.util.Optional;

import com.restaurant.ordering.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findBySlug(String slug);
}
