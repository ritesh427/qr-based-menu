package com.restaurant.ordering.repository;

import java.util.List;

import com.restaurant.ordering.entity.TableAssistanceRequest;
import com.restaurant.ordering.enums.AssistanceRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableAssistanceRequestRepository extends JpaRepository<TableAssistanceRequest, Long> {
    List<TableAssistanceRequest> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    List<TableAssistanceRequest> findByRestaurantIdAndStatusOrderByCreatedAtDesc(Long restaurantId, AssistanceRequestStatus status);
    List<TableAssistanceRequest> findByTableQrCodeTokenOrderByCreatedAtDesc(String qrToken);
}
