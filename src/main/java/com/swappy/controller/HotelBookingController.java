package com.swappy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.swappy.dto.BookingDto;
import com.swappy.dto.BookingRequest;
import com.swappy.dto.GuestDto;
import com.swappy.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class HotelBookingController {

	private final BookingService bookingService;
	
	@PostMapping("/init")
	public ResponseEntity<BookingDto> initialiseBooking(@Valid @RequestBody BookingRequest bookingRequest){
		
		return ResponseEntity.ok(bookingService.initialiseBooking(bookingRequest));
		
	}
	
	
	@PostMapping("/{bookingId}/addGuests")
	public ResponseEntity<BookingDto> addGuests(
	        @PathVariable("bookingId") Long bookingId,
	        @RequestHeader("X-Booking-Token") String managementToken,
	        @Valid @NotEmpty(message = "At least one guest is required") @RequestBody List<GuestDto> guestDtoList) {

	    return ResponseEntity.ok(
	        bookingService.addGuests(bookingId, managementToken, guestDtoList)
	    );
	}

	@PostMapping("/{bookingId}/confirm")
	public ResponseEntity<BookingDto> confirmBooking(
			@PathVariable("bookingId") Long bookingId,
			@RequestHeader("X-Booking-Token") String managementToken) {
		return ResponseEntity.ok(bookingService.confirmBooking(bookingId, managementToken));
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<BookingDto> getBooking(
			@PathVariable("bookingId") Long bookingId,
			@RequestHeader("X-Booking-Token") String managementToken) {
		return ResponseEntity.ok(bookingService.getBooking(bookingId, managementToken));
	}

	@PostMapping("/{bookingId}/cancel")
	public ResponseEntity<BookingDto> cancelBooking(
			@PathVariable("bookingId") Long bookingId,
			@RequestHeader("X-Booking-Token") String managementToken) {
		return ResponseEntity.ok(bookingService.cancelBooking(bookingId, managementToken));
	}
	
}
