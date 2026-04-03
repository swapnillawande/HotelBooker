package com.swappy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/bookings")
public class HotelBookingController {

	@Autowired
	private BookingService bookingService;
	
	@PostMapping("/init")
	public ResponseEntity<BookingDto> initaliseBooking(@RequestBody BookingRequest bookingRequest){
		
		return ResponseEntity.ok(bookingService.initaliseBooking(bookingRequest));
		
	}
	
	
	@PostMapping("/{bookingId}/addGuests")
	public ResponseEntity<BookingDto> addGuests(
	        @PathVariable("bookingId") Long bookingId,
	        @RequestBody List<GuestDto> guestDtoList) {

	    return ResponseEntity.ok(
	        bookingService.addGuests(bookingId, guestDtoList)
	    );
	}
	
}
