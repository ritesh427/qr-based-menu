package com.restaurant.ordering.service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.restaurant.ordering.dto.MenuCategoryResponse;
import com.restaurant.ordering.dto.MenuItemRequest;
import com.restaurant.ordering.dto.MenuItemResponse;
import com.restaurant.ordering.dto.QrCodeResponse;
import com.restaurant.ordering.dto.QrMenuResponse;
import com.restaurant.ordering.entity.Category;
import com.restaurant.ordering.entity.DiningTable;
import com.restaurant.ordering.entity.MenuItem;
import com.restaurant.ordering.exception.ResourceNotFoundException;
import com.restaurant.ordering.mapper.EntityMapper;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.DiningTableRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

@Service
@Transactional
public class MenuService {

    private final DiningTableRepository diningTableRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final EntityMapper entityMapper;
    private final String publicBaseUrl;

    public MenuService(DiningTableRepository diningTableRepository,
                       CategoryRepository categoryRepository,
                       MenuItemRepository menuItemRepository,
                       EntityMapper entityMapper,
                       @Value("${app.public-base-url}") String publicBaseUrl) {
        this.diningTableRepository = diningTableRepository;
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
        this.entityMapper = entityMapper;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Cacheable(value = "menuByQr", key = "#qrToken")
    @Transactional(readOnly = true)
    public QrMenuResponse getMenuByQr(String qrToken) {
        DiningTable table = diningTableRepository.findByQrCodeToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Table QR token not found"));

        Map<Long, List<MenuItemResponse>> groupedItems = menuItemRepository.findByCategoryRestaurantIdOrderByCategoryNameAscNameAsc(
                        table.getRestaurant().getId()).stream()
                .map(entityMapper::toMenuItemResponse)
                .collect(java.util.stream.Collectors.groupingBy(
                        MenuItemResponse::categoryId,
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<MenuCategoryResponse> categories = categoryRepository.findByRestaurantIdOrderByNameAsc(table.getRestaurant().getId())
                .stream()
                .sorted(Comparator.comparing(Category::getName))
                .map(category -> new MenuCategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription(),
                        groupedItems.getOrDefault(category.getId(), List.of())
                ))
                .toList();

        return new QrMenuResponse(
                table.getRestaurant().getId(),
                table.getRestaurant().getName(),
                table.getRestaurant().getSlug(),
                table.getTableNumber(),
                table.getQrCodeToken(),
                categories
        );
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItems(Long restaurantId) {
        return menuItemRepository.findByCategoryRestaurantIdOrderByCategoryNameAscNameAsc(restaurantId).stream()
                .map(entityMapper::toMenuItemResponse)
                .toList();
    }

    @CacheEvict(value = "menuByQr", allEntries = true)
    public MenuItemResponse createMenuItem(Long restaurantId, MenuItemRequest request) {
        Category category = findCategory(restaurantId, request.categoryId());
        MenuItem item = new MenuItem();
        updateItemFields(item, request, category);
        return entityMapper.toMenuItemResponse(menuItemRepository.save(item));
    }

    @CacheEvict(value = "menuByQr", allEntries = true)
    public MenuItemResponse updateMenuItem(Long restaurantId, Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .filter(value -> value.getCategory().getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        Category category = findCategory(restaurantId, request.categoryId());
        updateItemFields(item, request, category);
        return entityMapper.toMenuItemResponse(item);
    }

    @CacheEvict(value = "menuByQr", allEntries = true)
    public MenuItemResponse toggleAvailability(Long restaurantId, Long id, boolean available) {
        MenuItem item = menuItemRepository.findById(id)
                .filter(value -> value.getCategory().getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        item.setAvailable(available);
        return entityMapper.toMenuItemResponse(item);
    }

    @CacheEvict(value = "menuByQr", allEntries = true)
    public void deleteMenuItem(Long restaurantId, Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .filter(value -> value.getCategory().getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        menuItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<QrCodeResponse> getRestaurantQrs(Long restaurantId) {
        return diningTableRepository.findByRestaurantId(restaurantId).stream()
                .map(this::toQrCodeResponse)
                .toList();
    }

    private void updateItemFields(MenuItem item, MenuItemRequest request, Category category) {
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setImageUrl(request.imageUrl());
        item.setAvailable(request.available());
        item.setVegetarian(request.vegetarian());
        item.setEstimatedPreparationTime(request.estimatedPreparationTime());
        item.setCategory(category);
    }

    private Category findCategory(Long restaurantId, Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(value -> value.getRestaurant().getId().equals(restaurantId))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public QrCodeResponse toQrCodeResponse(DiningTable table) {
        String menuUrl = publicBaseUrl + "/menu/" + table.getQrCodeToken();
        return new QrCodeResponse(
                table.getTableNumber(),
                table.getQrCodeToken(),
                menuUrl,
                generateQrBase64(menuUrl)
        );
    }

    private String generateQrBase64(String text) {
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(matrix), "png", outputStream);
            return Base64Utils.encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to generate QR code", ex);
        }
    }
}
