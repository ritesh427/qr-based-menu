package com.restaurant.ordering.service;

import com.restaurant.ordering.dto.OrderResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final KafkaTemplate<String, OrderResponse> kafkaTemplate;
    private final boolean kafkaEnabled;

    public OrderEventPublisher(SimpMessagingTemplate messagingTemplate,
                               KafkaTemplate<String, OrderResponse> kafkaTemplate,
                               @Value("${app.kafka.enabled:false}") boolean kafkaEnabled) {
        this.messagingTemplate = messagingTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = kafkaEnabled;
    }

    public void publishOrderUpdate(OrderResponse orderResponse) {
        messagingTemplate.convertAndSend("/topic/orders/" + orderResponse.restaurantId(), orderResponse);
        messagingTemplate.convertAndSend("/topic/orders/table/" + orderResponse.qrToken(), orderResponse);
        if (kafkaEnabled) {
            kafkaTemplate.send("restaurant-orders", orderResponse.id().toString(), orderResponse);
        }
    }
}
