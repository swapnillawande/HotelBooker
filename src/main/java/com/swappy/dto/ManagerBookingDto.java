package com.swappy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.swappy.entities.enums.BookingStatus;
import com.swappy.entities.enums.PaymentStatus;

public record ManagerBookingDto(
        Long id,
        Long hotelId,
        String hotelName,
        String roomType,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        LocalDateTime createdAt,
        Integer roomsCount,
        Integer guestCount,
        String leadGuest,
        BookingStatus bookingStatus,
        PaymentStatus paymentStatus,
        BigDecimal amount) {
}
