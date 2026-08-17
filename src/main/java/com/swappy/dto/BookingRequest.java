package com.swappy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BookingRequest {

	@NotNull(message = "Hotel is required")
	@Positive(message = "Hotel ID must be positive")
	private Long hotelId;
	
	@NotNull(message = "Room is required")
	@Positive(message = "Room ID must be positive")
	private Long roomId;
	
	@NotNull(message = "Check-in date is required")
	@FutureOrPresent(message = "Check-in date cannot be in the past")
	private LocalDate checkInDate;
	
	@NotNull(message = "Check-out date is required")
	private LocalDate checkOutDate;
	
	@NotNull(message = "Room count is required")
	@Positive(message = "Room count must be at least one")
	private Integer roomsCount;

	public Long getHotelId() {
		return hotelId;
	}

	public void setHotelId(Long hotelId) {
		this.hotelId = hotelId;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public Integer getRoomsCount() {
		return roomsCount;
	}

	public void setRoomsCount(Integer roomsCount) {
		this.roomsCount = roomsCount;
	}
	
	
}
