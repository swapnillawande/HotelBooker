package com.swappy.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
	        @Valid @NotEmpty(message = "At least one guest is required") @RequestBody List<GuestDto> guestDtoList) {

	    return ResponseEntity.ok(
	        bookingService.addGuests(bookingId, guestDtoList)
	    );
	}

	@PostMapping("/{bookingId}/confirm")
	public ResponseEntity<BookingDto> confirmBooking(@PathVariable("bookingId") Long bookingId) {
		return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
	}
	
}
