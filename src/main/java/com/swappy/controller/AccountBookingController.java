package com.swappy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.BookingDto;
import com.swappy.entities.User;
import com.swappy.service.BookingService;

@RestController
@RequestMapping("/account/bookings")
public class AccountBookingController {

    private final BookingService bookingService;

    public AccountBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getMyBookings(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getBookingsForUser(user.getId()));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingDto> cancelMyBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.cancelBookingForUser(bookingId, user.getId()));
    }
}
