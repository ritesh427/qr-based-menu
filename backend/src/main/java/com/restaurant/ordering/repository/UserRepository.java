package com.restaurant.ordering.repository;

import java.util.List;
import java.util.Optional;

import com.restaurant.ordering.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    List<AppUser> findByRestaurantIdOrderByUsernameAsc(Long restaurantId);
    boolean existsByUsername(String username);
}
