package com.swappy.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data // lombok dependency is not working so adding getters and setters manually
public class HotelSearchDto {

	
	@NotBlank(message = "City is required")
	private String city;
	
	@NotNull(message = "Start date is required")
	private LocalDate startDate;
	
	@NotNull(message = "End date is required")
	private LocalDate endDate;
	
	@NotNull(message = "Room count is required")
	@Positive(message = "Room count must be at least one")
	private Integer roomsCount;
	
	@Min(value = 0, message = "Page cannot be negative")
	private Integer page = 0;
	
	@Min(value = 1, message = "Page size must be at least one")
	@Max(value = 100, message = "Page size cannot exceed 100")
	private Integer size = 10;

	public HotelSearchDto() {

	}

	public HotelSearchDto(String city, LocalDate startDate, LocalDate endDate, Integer page, Integer size) {
		super();
		this.city = city;
		this.startDate = startDate;
		this.endDate = endDate;
		this.page = page;
		this.size = size;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Integer getRoomCount() {
		return roomsCount;
	}

	public void setRoomCount(Integer roomCount) {
		this.roomsCount = roomCount;
	}
	
	
	
	
	
}
