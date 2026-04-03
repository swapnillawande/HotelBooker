package com.swappy.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;


import com.swappy.entities.Guest;
import com.swappy.entities.Hotel;
import com.swappy.entities.Room;
import com.swappy.entities.User;
import com.swappy.entities.enums.BookingStatus;


import lombok.Data;

@Data
public class BookingDto {


	private Long id;
    
    private User user;
    
    private Integer roomsCount;
    
    private LocalDate checkInDate;
    
    private LocalDate checkOutDate;
    
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
    private BookingStatus bookingStatus;
	
	private Set<Guest> guests;

	public BookingDto() {

	}

	public BookingDto(Long id, User user, Integer roomsCount, LocalDate checkInDate,
			LocalDate checkOutDate, LocalDateTime createdAt, LocalDateTime updatedAt, BookingStatus bookingStatus,
			Set<Guest> guests) {
		super();
		this.id = id;
		this.user = user;
		this.roomsCount = roomsCount;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.bookingStatus = bookingStatus;
		this.guests = guests;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}



	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Integer getRoomsCount() {
		return roomsCount;
	}

	public void setRoomsCount(Integer roomsCount) {
		this.roomsCount = roomsCount;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public Set<Guest> getGuests() {
		return guests;
	}

	public void setGuests(Set<Guest> guests) {
		this.guests = guests;
	}
	
	
	
	
}
