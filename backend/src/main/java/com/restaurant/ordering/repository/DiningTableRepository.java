package com.restaurant.ordering.repository;

import java.util.List;
import java.util.Optional;

import com.restaurant.ordering.entity.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    Optional<DiningTable> findByQrCodeToken(String qrCodeToken);
    List<DiningTable> findByRestaurantId(Long restaurantId);
    boolean existsByRestaurantIdAndTableNumber(Long restaurantId, Integer tableNumber);
}
