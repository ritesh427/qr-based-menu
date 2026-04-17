package com.restaurant.ordering.controller;

import java.util.List;

import com.restaurant.ordering.dto.ImageUploadResponse;
import com.restaurant.ordering.dto.MenuItemRequest;
import com.restaurant.ordering.dto.MenuItemResponse;
import com.restaurant.ordering.dto.QrCodeResponse;
import com.restaurant.ordering.service.FileStorageService;
import com.restaurant.ordering.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/admin/menu-items")
public class AdminMenuController {

    private final MenuService menuService;
    private final FileStorageService fileStorageService;

    public AdminMenuController(MenuService menuService, FileStorageService fileStorageService) {
        this.menuService = menuService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public List<MenuItemResponse> getMenuItems(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return menuService.getMenuItems(restaurantId);
    }

    @PostMapping
    public MenuItemResponse createMenuItem(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                           @Valid @RequestBody MenuItemRequest request) {
        return menuService.createMenuItem(restaurantId, request);
    }

    @PutMapping("/{id}")
    public MenuItemResponse updateMenuItem(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                           @PathVariable Long id,
                                           @Valid @RequestBody MenuItemRequest request) {
        return menuService.updateMenuItem(restaurantId, id, request);
    }

    @PatchMapping("/{id}/availability")
    public MenuItemResponse toggleAvailability(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                                               @PathVariable Long id,
                                               @RequestParam boolean available) {
        return menuService.toggleAvailability(restaurantId, id, available);
    }

    @DeleteMapping("/{id}")
    public void deleteMenuItem(@RequestHeader("X-Restaurant-Id") Long restaurantId,
                               @PathVariable Long id) {
        menuService.deleteMenuItem(restaurantId, id);
    }

    @GetMapping("/qr-codes")
    public List<QrCodeResponse> getQrCodes(@RequestHeader("X-Restaurant-Id") Long restaurantId) {
        return menuService.getRestaurantQrs(restaurantId);
    }

    @PostMapping(value = "/image-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadMenuImage(@RequestParam("file") MultipartFile file) {
        String storedPath = fileStorageService.storeMenuImage(file);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(storedPath)
                .toUriString();
        return new ImageUploadResponse(storedPath, imageUrl);
    }
}
