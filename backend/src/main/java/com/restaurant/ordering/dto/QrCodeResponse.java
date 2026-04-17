package com.restaurant.ordering.dto;

public record QrCodeResponse(
        Integer tableNumber,
        String qrToken,
        String menuUrl,
        String imageBase64
) {
}
