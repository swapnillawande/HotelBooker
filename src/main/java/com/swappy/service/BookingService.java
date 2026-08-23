package com.swappy.service;

import java.util.List;

import com.swappy.dto.BookingDto;
import com.swappy.dto.BookingRequest;
import com.swappy.dto.DemoPaymentRequest;
import com.swappy.dto.GuestDto;
import com.swappy.dto.ManagerBookingDashboardDto;

public interface BookingService {

	
	public BookingDto initialiseBooking(BookingRequest bookingRequest);

	public BookingDto addGuests(Long bookingId, String managementToken, List<GuestDto> guestDtoList);

	public BookingDto confirmBooking(Long bookingId, String managementToken);

	public BookingDto payBooking(Long bookingId, String managementToken, String idempotencyKey, DemoPaymentRequest request);

	public BookingDto getBooking(Long bookingId, String managementToken);

	public BookingDto cancelBooking(Long bookingId, String managementToken);

	public List<BookingDto> getBookingsForUser(Long userId);

	public BookingDto cancelBookingForUser(Long bookingId, Long userId);

	public ManagerBookingDashboardDto getManagerBookingDashboard(Long ownerId);

	
	
}
