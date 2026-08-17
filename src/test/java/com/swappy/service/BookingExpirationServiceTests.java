package com.swappy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.swappy.entities.Booking;
import com.swappy.entities.Inventory;
import com.swappy.entities.Room;
import com.swappy.entities.enums.BookingStatus;
import com.swappy.repository.BookingRepository;
import com.swappy.repository.InventoryRepository;
import com.swappy.service.impl.BookingExpirationService;

@ExtendWith(MockitoExtension.class)
class BookingExpirationServiceTests {

    @Mock private BookingRepository bookingRepository;
    @Mock private InventoryRepository inventoryRepository;

    private BookingExpirationService expirationService;

    @BeforeEach
    void setUp() {
        expirationService = new BookingExpirationService(bookingRepository, inventoryRepository);
    }

    @Test
    void expiresReservationAndReturnsHeldRoomsToInventory() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);
        Room room = new Room();
        room.setId(5L);
        Booking booking = new Booking();
        booking.setId(9L);
        booking.setRoom(room);
        booking.setRoomsCount(2);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setBookingStatus(BookingStatus.RESERVED);
        Inventory firstNight = inventory(4);
        Inventory secondNight = inventory(1);
        when(bookingRepository.findByBookingStatusAndCreatedAtBefore(eq(BookingStatus.RESERVED), any()))
                .thenReturn(List.of(booking));
        when(inventoryRepository.findAndLockInventoryForBooking(5L, checkIn, checkOut))
                .thenReturn(List.of(firstNight, secondNight));

        expirationService.expireStaleReservations();

        assertEquals(BookingStatus.EXPIRED, booking.getBookingStatus());
        assertEquals(2, firstNight.getReservedCount());
        assertEquals(0, secondNight.getReservedCount());
        verify(inventoryRepository).saveAll(List.of(firstNight, secondNight));
        verify(bookingRepository).saveAll(List.of(booking));
    }

    @Test
    void doesNotWriteWhenNoReservationsAreStale() {
        when(bookingRepository.findByBookingStatusAndCreatedAtBefore(eq(BookingStatus.RESERVED), any()))
                .thenReturn(List.of());

        expirationService.expireStaleReservations();

        verify(inventoryRepository, never()).saveAll(any());
        verify(bookingRepository, never()).saveAll(any());
    }

    private Inventory inventory(int reservedCount) {
        Inventory inventory = new Inventory();
        inventory.setReservedCount(reservedCount);
        return inventory;
    }
}
