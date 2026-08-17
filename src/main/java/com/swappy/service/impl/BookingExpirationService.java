package com.swappy.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.swappy.entities.Booking;
import com.swappy.entities.Inventory;
import com.swappy.entities.enums.BookingStatus;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingExpirationService {

    private static final Logger logger = LoggerFactory.getLogger(BookingExpirationService.class);
    private static final long HOLD_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;

    @Scheduled(fixedDelayString = "${booking.expiration-scan-ms:60000}")
    @Transactional
    public void expireStaleReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(HOLD_MINUTES);
        List<Booking> staleBookings = bookingRepository.findByBookingStatusInAndCreatedAtBefore(
                List.of(BookingStatus.RESERVED, BookingStatus.GUEST_ADDED),
                cutoff);

        for (Booking booking : staleBookings) {
            releaseInventory(booking);
            booking.setBookingStatus(BookingStatus.EXPIRED);
        }

        if (!staleBookings.isEmpty()) {
            bookingRepository.saveAll(staleBookings);
            logger.info("Expired {} stale booking reservations", staleBookings.size());
        }
    }

    private void releaseInventory(Booking booking) {
        List<Inventory> inventories = inventoryRepository.findAndLockInventoryForBooking(
                booking.getRoom().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate());

        for (Inventory inventory : inventories) {
            int currentReserved = inventory.getReservedCount() == null ? 0 : inventory.getReservedCount();
            inventory.setReservedCount(Math.max(0, currentReserved - booking.getRoomsCount()));
        }
        inventoryRepository.saveAll(inventories);
    }
}
