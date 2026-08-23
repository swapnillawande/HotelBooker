package com.swappy.dto;

import java.math.BigDecimal;
import java.util.List;

public record ManagerBookingDashboardDto(
        long totalBookings,
        long confirmedBookings,
        long arrivalsNextSevenDays,
        BigDecimal confirmedRevenue,
        List<ManagerBookingDto> bookings) {
}
