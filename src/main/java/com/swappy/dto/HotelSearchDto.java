package com.swappy.dto;

import java.time.LocalDate;

import lombok.Data;

@Data // lombok dependency is not working so adding getters and setters manually
public class HotelSearchDto {

	
	private String city;
	
	private LocalDate startDate;
	
	private LocalDate endDate;
	
	private Integer roomsCount;
	
	private Integer page = 0;
	
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
