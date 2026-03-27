package com.swappy.dto;

import java.util.List;

import lombok.Data;

@Data
public class HotelInfoDto {

	private HotelDto hotel;
	
	private List<RoomDto> rooms;
	
	

	public HotelInfoDto() {

	}
	
	public HotelInfoDto(HotelDto hotel, List<RoomDto> rooms) {
		super();
		this.hotel = hotel;
		this.rooms = rooms;
	}

	public HotelDto getHotel() {
		return hotel;
	}

	public void setHotel(HotelDto hotel) {
		this.hotel = hotel;
	}

	public List<RoomDto> getRooms() {
		return rooms;
	}

	public void setRooms(List<RoomDto> rooms) {
		this.rooms = rooms;
	}
	
	
	
}
