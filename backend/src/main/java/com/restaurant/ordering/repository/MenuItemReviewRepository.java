package com.restaurant.ordering.repository;

import java.util.List;

import com.restaurant.ordering.entity.MenuItemReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemReviewRepository extends JpaRepository<MenuItemReview, Long> {
    List<MenuItemReview> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<MenuItemReview> findByTableQrCodeTokenOrderByCreatedAtDesc(String qrToken);
    long countByMenuItemId(Long menuItemId);

    @Query("select coalesce(avg(r.rating), 0) from MenuItemReview r where r.menuItem.id = :menuItemId")
    Double averageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);
}
