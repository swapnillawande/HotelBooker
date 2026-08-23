package com.swappy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swappy.dto.ManagerBookingDashboardDto;
import com.swappy.entities.User;
import com.swappy.service.BookingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class ManagerBookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<ManagerBookingDashboardDto> getDashboard(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bookingService.getManagerBookingDashboard(user.getId()));
    }
}
