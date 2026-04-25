package com.restaurant.ordering.service;

import java.time.LocalDateTime;
import java.util.List;

import com.restaurant.ordering.dto.AssistanceRequestCreateRequest;
import com.restaurant.ordering.dto.AssistanceRequestResponse;
import com.restaurant.ordering.dto.TableSessionResponse;
import com.restaurant.ordering.entity.CustomerOrder;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.TableAssistanceRequest;
import com.restaurant.ordering.enums.AssistanceRequestStatus;
import com.restaurant.ordering.enums.OrderStatus;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.OrderRepository;
import com.restaurant.ordering.repository.TableAssistanceRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssistanceRequestService {

    private final DiningTableRepository diningTableRepository;
    private final TableAssistanceRequestRepository assistanceRequestRepository;
    private final OrderRepository orderRepository;
    private final EntityMapper entityMapper;
    private final OrderEventPublisher eventPublisher;

    public AssistanceRequestService(DiningTableRepository diningTableRepository,
                                    TableAssistanceRequestRepository assistanceRequestRepository,
                                    OrderRepository orderRepository,
                                    EntityMapper entityMapper,
                                    OrderEventPublisher eventPublisher) {
        this.diningTableRepository = diningTableRepository;
        this.assistanceRequestRepository = assistanceRequestRepository;
        this.orderRepository = orderRepository;
        this.entityMapper = entityMapper;
        this.eventPublisher = eventPublisher;
    }

    public AssistanceRequestResponse createRequest(AssistanceRequestCreateRequest request) {
        DiningTable table = diningTableRepository.findByQrCodeToken(request.qrToken())
                .orElseThrow(() -> new ResourceNotFoundException("QR token not found"));

        TableAssistanceRequest assistanceRequest = new TableAssistanceRequest();
        assistanceRequest.setRestaurant(table.getRestaurant());
        assistanceRequest.setTable(table);
        assistanceRequest.setType(request.type());
        assistanceRequest.setNote(request.note());
        TableAssistanceRequest saved = assistanceRequestRepository.save(assistanceRequest);
        AssistanceRequestResponse response = entityMapper.toAssistanceRequestResponse(saved);
        eventPublisher.publishAssistanceUpdate(response);
        eventPublisher.publishTableSessionUpdate(saved.getRestaurant().getId(), buildTableSession(saved.getTable()));
        return response;
    }

    @Transactional(readOnly = true)
    public List<AssistanceRequestResponse> getRequestsForRestaurant(Long restaurantId) {
        return assistanceRequestRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(entityMapper::toAssistanceRequestResponse)
                .toList();
    }

    public AssistanceRequestResponse updateStatus(Long restaurantId, Long requestId, AssistanceRequestStatus status) {
        TableAssistanceRequest request = assistanceRequestRepository.findById(requestId)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Assistance request not found"));
        request.setStatus(status);
        request.setResolvedAt(status == AssistanceRequestStatus.RESOLVED ? LocalDateTime.now() : null);
        AssistanceRequestResponse response = entityMapper.toAssistanceRequestResponse(request);
        eventPublisher.publishAssistanceUpdate(response);
        eventPublisher.publishTableSessionUpdate(restaurantId, buildTableSession(request.getTable()));
        return response;
    }

    @Transactional(readOnly = true)
    public List<TableSessionResponse> getTableSessions(Long restaurantId) {
        return diningTableRepository.findByRestaurantId(restaurantId).stream()
                .map(this::buildTableSession)
                .sorted(java.util.Comparator.comparing(TableSessionResponse::tableNumber))
                .toList();
    }

    private TableSessionResponse buildTableSession(DiningTable table) {
        List<CustomerOrder> activeOrders = orderRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(table.getQrCodeToken())
                .stream()
                .filter(order -> order.getStatus() != OrderStatus.SERVED)
                .toList();
        java.math.BigDecimal total = activeOrders.stream()
                .map(CustomerOrder::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        OrderStatus latestStatus = activeOrders.isEmpty() ? null : activeOrders.get(0).getStatus();
        List<AssistanceRequestResponse> openRequests = assistanceRequestRepository.findByTableQrCodeTokenOrderByCreatedAtDesc(table.getQrCodeToken())
                .stream()
                .filter(request -> request.getStatus() != AssistanceRequestStatus.RESOLVED)
                .map(entityMapper::toAssistanceRequestResponse)
                .toList();

        return new TableSessionResponse(
                table.getTableNumber(),
                table.getQrCodeToken(),
                activeOrders.size(),
                total,
                latestStatus,
                openRequests
        );
    }
}
