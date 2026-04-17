package com.restaurant.ordering.service;

import java.util.ArrayList;
import java.util.List;

import com.restaurant.ordering.dto.QrCodeResponse;
import com.restaurant.ordering.dto.TableCreateRequest;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.Restaurant;
import com.restaurant.ordering.exception.BadRequestException;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TableService {

    private final DiningTableRepository diningTableRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuService menuService;

    public TableService(DiningTableRepository diningTableRepository,
                        RestaurantRepository restaurantRepository,
                        MenuService menuService) {
        this.diningTableRepository = diningTableRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuService = menuService;
    }

    public List<QrCodeResponse> createTables(Long restaurantId, TableCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        List<QrCodeResponse> created = new ArrayList<>();
        for (int index = 0; index < request.count(); index++) {
            int tableNumber = request.startTableNumber() + index;
            if (diningTableRepository.existsByRestaurantIdAndTableNumber(restaurantId, tableNumber)) {
                throw new BadRequestException("Table " + tableNumber + " already exists");
            }

            DiningTable table = new DiningTable();
            table.setRestaurant(restaurant);
            table.setTableNumber(tableNumber);
            table.setQrCodeToken(restaurant.getSlug() + "-t" + tableNumber);
            diningTableRepository.save(table);
            created.add(menuService.toQrCodeResponse(table));
        }
        return created;
    }
}
