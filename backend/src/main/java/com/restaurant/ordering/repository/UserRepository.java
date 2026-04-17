package com.restaurant.ordering.repository;

import java.util.Optional;

import com.restaurant.ordering.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
}
