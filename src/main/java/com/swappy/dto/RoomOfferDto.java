package com.swappy.dto;

import java.math.BigDecimal;
import java.util.List;

public record RoomOfferDto(
        Long id,
        String type,
        List<String> amenities,
        List<String> photos,
        Integer capacity,
        Integer availableRooms,
        Integer nights,
        BigDecimal nightlyPrice,
        BigDecimal totalPrice) {
}
