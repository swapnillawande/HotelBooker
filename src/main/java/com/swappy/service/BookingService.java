package com.swappy.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.swappy.dto.BookingDto;
import com.swappy.dto.BookingRequest;
import com.swappy.dto.GuestDto;

public interface BookingService {

	
	public BookingDto initaliseBooking(BookingRequest bookingRequest);

	public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);

	
	
}
